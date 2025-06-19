package com.srr.player.repository;

import com.srr.player.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
* @author Chanheng
* @date 2025-05-18
**/
public interface PlayerRepository extends JpaRepository<Player, Long>, JpaSpecificationExecutor<Player> {
    
    /**
     * Find player by user ID
     * @param userId the user ID
     * @return Player entity if found, null otherwise
     */
    Player findByUserId(Long userId);
}