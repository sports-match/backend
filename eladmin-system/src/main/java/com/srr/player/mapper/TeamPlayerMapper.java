package com.srr.player.mapper;

import com.srr.enumeration.TeamStatus;
import com.srr.player.domain.Team;
import com.srr.player.domain.TeamPlayer;
import com.srr.player.dto.PlayerDto;
import com.srr.player.dto.PlayerSportRatingDto;
import com.srr.player.dto.TeamPlayerDto;
import me.zhengjie.base.BaseMapper;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Objects;

/**
 * @author Chanheng
 * @date 2025-05-25
 **/
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = PlayerMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TeamPlayerMapper extends BaseMapper<TeamPlayerDto, TeamPlayer> {
    @Mapping(target = "id", source = "teamPlayer.id")
    @Mapping(target = "score", expression = "java(calculateIndividualScore(player, sportId))")
    TeamPlayerDto toTeamPlayerDto(TeamPlayer teamPlayer, PlayerDto player, String email, Long sportId);

    default Double calculateIndividualScore(PlayerDto player, Long sportId) {
        if (player == null) {
            return 0.0;
        }
        return getPlayerRatingBySport(player, sportId);
    }

    @Mapping(target = "id", source = "player.id")
    @Mapping(target = "player", source = "player")
    @Mapping(target = "partner", source = "partner")
    @Mapping(target = "status", source = "teamStatus")
    @Mapping(target = "teamId", source = "team.id")
    @Mapping(target = "combinedScore", expression = "java(calculateCombinedScore(player, partner, sportId))")
    TeamPlayerDto toDto(PlayerDto player, PlayerDto partner, TeamStatus teamStatus, Long sportId, Team team);

    /**
     * Calculate the combined score known as the average score for the team
     *
     * @param player  The main player (required)
     * @param partner The partner (can be null)
     * @param sportId The sport id
     * @return The calculated score
     */
    default Double calculateCombinedScore(PlayerDto player, PlayerDto partner, Long sportId) {
        if (player == null) {
            return 0.0;
        }

        Double playerRating = getPlayerRatingBySport(player, sportId);

        // If no partner, return just the player's rating
        if (partner == null) {
            return playerRating;
        }

        // If double format with partner, return the average
        return (playerRating + getPlayerRatingBySport(partner, sportId)) / 2;
    }

    /**
     * Helper method to get the doubles rating from a player
     */
    private Double getPlayerRatingBySport(PlayerDto player, Long sportId) {
        if (player == null || player.getPlayerSportRating() == null) {
            return 0.0;
        }

        return player.getPlayerSportRating().stream()
                .filter(rate -> rate != null && rate.getRateScore() != null && Objects.equals(rate.getSportId(), sportId))
                .findFirst()
                .map(PlayerSportRatingDto::getRateScore)
                .orElse(0.0);
    }
}
