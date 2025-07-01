package me.zhengjie.modules.system.service.impl;

import com.srr.organizer.domain.EventOrganizer;
import com.srr.organizer.service.EventOrganizerService;
import com.srr.player.domain.Player;
import com.srr.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.domain.EmailConfig;
import me.zhengjie.domain.vo.EmailVo;
import me.zhengjie.modules.security.service.dto.UserRegisterDto;
import me.zhengjie.modules.security.service.enums.UserType;
import me.zhengjie.modules.system.domain.Role;
import me.zhengjie.modules.system.domain.User;
import me.zhengjie.modules.system.repository.RoleRepository;
import me.zhengjie.modules.system.service.UserService;
import me.zhengjie.modules.system.service.VerifyService;
import me.zhengjie.service.EmailService;
import me.zhengjie.utils.ExecutionResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFacade {
    private static final String REGISTER_KEY_PREFIX = "register:email:";

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final PlayerService playerService;
    private final EventOrganizerService eventOrganizerService;
    private final EmailService emailService;
    private final VerifyService verifyService;


    /**
     * Create user in a single transaction
     *
     * @param registerDto The registration information
     * @return ExecutionResult
     */
    @Transactional
    public ExecutionResult createUserTransactional(UserRegisterDto registerDto) {
        final var createdUser = userService.create(registerDto);
        createUserTypeEntity(createdUser);
        return ExecutionResult.of(createdUser.getId(), null);
    }

    /**
     * Send email to user
     *
     * @param email The email of the user
     * @return EmailConfig
     */
    public EmailConfig sendEmail(String email) {
        try {
            userService.findByEmail(email);
            log.info("Sending verification email to {}", email);
            EmailVo emailVo = verifyService.sendEmail(email, REGISTER_KEY_PREFIX);
            final var config = emailService.find();
            emailService.send(emailVo, config);
            log.info("Send verification email completed successfully.");
            return config;
        } catch (Exception e) {
            log.error("Send verification email failed.", e);
            throw e;
        }
    }

    /**
     * Assigns a role to a user by role name
     *
     * @param user     The user to assign the role to
     * @param roleName The name of the role to assign
     */
    private void assignRoleToUser(User user, String roleName) {
        try {
            final Role role = roleRepository.findByName(roleName);
            if (role != null) {
                // Add the role to user's roles
                Set<Role> roles = user.getRoles();
                if (roles == null) {
                    roles = new HashSet<>();
                }

                // Check if user already has this role
                boolean alreadyHasRole = roles.stream()
                        .anyMatch(r -> r.getId().equals(role.getId()));

                if (!alreadyHasRole) {
                    roles.add(role);
                    user.setRoles(roles);
                    userService.update(user);
                    log.info("Assigned role '{}' to user: {}", role.getName(), user.getUsername());
                } else {
                    log.debug("User '{}' already has role '{}'", user.getUsername(), role.getName());
                }
            } else {
                log.warn("Role '{}' not found", roleName);
            }
        } catch (Exception e) {
            log.error("Failed to assign role '{}' to user: {}", roleName, user.getUsername(), e);
        }
    }

    /**
     * Creates a Player entity for the given user
     *
     * @param user The user to create a Player for
     */
    private void createPlayerEntity(User user) {
        // Create player entity
        Player player = new Player();
        player.setName(user.getNickName());
        player.setUserId(user.getId());
        player.setDescription("Player created upon registration");

        // Save player - this will trigger role assignment via UserRoleSyncService
        playerService.create(player);
        log.info("Created player for user: {}", user.getUsername());
    }

    /**
     * Creates an EventOrganizer entity for the given user
     *
     * @param user The user to create an EventOrganizer for
     */
    private void createEventOrganizerEntity(User user) {
        // Create new event organizer
        EventOrganizer organizer = new EventOrganizer();
        organizer.setUserId(user.getId());
        organizer.setDescription("Event organizer created upon registration");

        // Save organizer - this will trigger role assignment via UserRoleSyncService
        eventOrganizerService.create(organizer);
        log.info("Created event organizer for user: {}", user.getUsername());
    }

    /**
     * Creates the appropriate entity based on the user's type
     *
     * @param user The user to create entity for
     */
    private void createUserTypeEntity(User user) {
        // Get user type from user entity
        UserType userType = user.getUserType();

        try {
            switch (userType) {
                case PLAYER:
                    // Assign Player role to the user
                    assignRoleToUser(user, "Player");
                    createPlayerEntity(user);
                    break;
                case ORGANIZER:
                    // Assign Organizer role to the user
                    assignRoleToUser(user, "Organizer");
                    createEventOrganizerEntity(user);
                    break;
                case ADMIN:
                    // No entity to create for ADMIN
                    break;
            }
        } catch (IllegalArgumentException e) {
            // Invalid user type, log and ignore
            log.error("Invalid user type: {}", userType, e);
        }
    }

}
