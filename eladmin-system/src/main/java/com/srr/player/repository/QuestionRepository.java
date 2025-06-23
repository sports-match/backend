package com.srr.player.repository;

import com.srr.enumeration.Format;
import com.srr.player.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
* @description Question repository for database operations
* @author Chanheng
* @date 2025-05-31
**/
public interface QuestionRepository extends JpaRepository<Question, Long>, JpaSpecificationExecutor<Question> {
    
    /**
     * Find all questions ordered by category and order index
     * @return List of questions
     */
    @Query("SELECT q FROM Question q ORDER BY q.category, q.orderIndex")
    List<Question> findAllOrdered();
    
    /**
     * Find questions by category
     * @param category The category name
     * @return List of questions in the category
     */
    List<Question> findByCategoryOrderByOrderIndex(String category);
    
    /**
     * Find questions by sport and format
     * @param sportId The sport ID
     * @param format The match format
     * @return List of questions in the sport and format
     */
    @Query("SELECT q FROM Question q WHERE q.sport.id = :sportId AND q.format = :format ORDER BY q.category, q.orderIndex")
    List<Question> findBySportIdAndFormatOrderByCategoryAndOrderIndex(Long sportId, Format format);
}
