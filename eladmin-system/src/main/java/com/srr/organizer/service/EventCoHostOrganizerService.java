package com.srr.organizer.service;

import com.srr.enumeration.VerificationStatus;
import com.srr.event.domain.Event;
import com.srr.organizer.domain.EventCoHostOrganizer;
import com.srr.organizer.domain.EventOrganizer;
import com.srr.organizer.dto.EventCoHostOrganizerDto;
import com.srr.organizer.dto.EventCoHostOrganizerMapper;
import com.srr.organizer.repository.EventCoHostOrganizerRepository;
import com.srr.organizer.repository.EventOrganizerRepository;
import lombok.RequiredArgsConstructor;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventCoHostOrganizerService {
    private final EventCoHostOrganizerRepository eventCoHostOrganizerRepository;
    private final EventCoHostOrganizerMapper mapper;
    private final EventOrganizerRepository eventOrganizerRepository;


    /**
     * Function to create co_host organizers of an event
     *
     * @param coHostOrganizerIds Ids of co_host organizers
     * @param event              The event to which the co_host organizers are attached
     */
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public void createEventCoHostOrganizers(List<Long> coHostOrganizerIds, Event event) {
        if (!coHostOrganizerIds.isEmpty()) {
            for (Long coHostOrganizerId : coHostOrganizerIds) {
                Optional<EventOrganizer> coHostOrganizer = eventOrganizerRepository.findById(coHostOrganizerId);
                coHostOrganizer.ifPresentOrElse(coHost -> {
                    validateOrganizerAccount(coHost); // validate co_host organizer account
                    final EventCoHostOrganizerDto dto = new EventCoHostOrganizerDto();
                    dto.setEvent(event);
                    dto.setEventOrganizer(coHost);
                    create(dto);
                }, () -> {
                    throw new EntityNotFoundException(EventOrganizer.class, "id", coHostOrganizerId);
                });
            }
        }
    }

    public EventCoHostOrganizer create(EventCoHostOrganizerDto resource) {
        return eventCoHostOrganizerRepository.save(mapper.toEntity(resource));
    }

    public void validateOrganizerAccount(EventOrganizer organizer) {
        if (organizer.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new BadRequestException("Organizer account is not verified. Event creation is not allowed.");
        }
    }
}
