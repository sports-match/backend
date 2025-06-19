package com.srr.player.repository;

import com.srr.player.domain.PlayerAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
* @description PlayerAnswer repository for database operations
* @author Chanheng
* @date 2025-05-31
**/
public interface PlayerAnswerRepository extends JpaRepository<PlayerAnswer, Long>, JpaSpecificationExecutor<PlayerAnswer> {
    
    /**
     * Find all answers for a player
     * @param playerId The player ID
     * @return List of player answers
     */
    List<PlayerAnswer> findByPlayerId(Long playerId);
    
    /**
     * Find answers by player ID and question category
     * @param playerId The player ID
     * @param category The question category
     * @return List of player answers for the given category
     */
    @Query("SELECT pa FROM PlayerAnswer pa JOIN pa.question q WHERE pa.playerId = :playerId AND q.category = :category")
    List<PlayerAnswer> findByPlayerIdAndQuestionCategory(@Param("playerId") Long playerId, @Param("category") String category);
    
    /**
     * Check if a player has completed self-assessment
     * @param playerId The player ID
     * @return Count of answers for the player
     */
    long countByPlayerId(Long playerId);

    /**
     * Count distinct questions answered by a player
     * @param playerId The player ID
     * @return Count of distinct questions answered
     */
    @Query("SELECT COUNT(DISTINCT pa.questionId) FROM PlayerAnswer pa WHERE pa.playerId = :playerId")
    long countDistinctQuestionByPlayerId(@Param("playerId") Long playerId);
}
