package com.srr.player.repository;

import com.srr.player.domain.RatingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
* @author Chanheng
* @date 2025-05-26
**/
@Repository
public interface RatingHistoryRepository extends JpaRepository<RatingHistory, Long>, JpaSpecificationExecutor<RatingHistory> {

    List<RatingHistory> findByPlayerIdOrderByCreateTimeDesc(Long playerId);
}