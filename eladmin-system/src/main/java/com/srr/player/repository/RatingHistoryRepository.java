package com.srr.player.repository;

import com.srr.enumeration.Format;
import com.srr.player.domain.RatingHistory;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Chanheng
 * @date 2025-05-26
 **/
@Repository
public interface RatingHistoryRepository extends JpaRepository<RatingHistory, Long>, JpaSpecificationExecutor<RatingHistory> {

    @Query("SELECT r FROM RatingHistory r " +
            "WHERE r.player.id = :playerId " +
            "AND r.match.matchGroup.event.format = :format " +
            "AND r.createTime >= :searchDate " +
            "ORDER BY r.createTime DESC")
    List<RatingHistory> findByPlayerIdAndFormatWithDate(
            @Param("playerId") Long playerId,
            @Param("format") Format format,
            @Param("searchDate") LocalDateTime searchDate
    );

    @Query("SELECT r FROM RatingHistory r " +
            "WHERE r.player.id = :playerId " +
            "AND r.match.matchGroup.event.format = :format " +
            "AND r.createTime >= :startDate " +
            "AND r.createTime <= :endDate " +
            "ORDER BY r.createTime DESC")
    List<RatingHistory> findByPlayerIdAndFormatWithDateRange(
            @Param("playerId") Long playerId,
            @Param("format") Format format,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT r FROM RatingHistory r WHERE r.player.id = :playerId AND r.match.matchGroup.event.id = :eventId ORDER BY r.createTime DESC")
    List<RatingHistory> findByPlayerIdAndEventIdOrderByCreateTimeDesc(Long playerId, Long eventId);

    RatingHistory findByPlayerIdAndMatchId(Long playerId, Long matchId);
}