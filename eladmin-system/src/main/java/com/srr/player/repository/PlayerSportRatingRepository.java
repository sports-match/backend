package com.srr.player.repository;

import com.srr.enumeration.Format;
import com.srr.player.domain.PlayerSportRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PlayerSportRatingRepository extends JpaRepository<PlayerSportRating, Long>, JpaSpecificationExecutor<PlayerSportRating> {

    Optional<PlayerSportRating> findByPlayerIdAndSportNameAndFormat(Long playerId, String sport, Format format);

    List<PlayerSportRating> findByPlayerId(Long playerId);
}
