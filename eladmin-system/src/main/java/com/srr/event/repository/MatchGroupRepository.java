/*
*  Copyright 2019-2025 Zheng Jie
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package com.srr.event.repository;

import com.srr.event.domain.MatchGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
* @author Chanheng
* @date 2025-05-25
**/
public interface MatchGroupRepository extends JpaRepository<MatchGroup, Long>, JpaSpecificationExecutor<MatchGroup> {
    /**
     * Find all match groups for a specific event
     * @param eventId The event ID
     * @return List of match groups
     */
    List<MatchGroup> findAllByEventId(Long eventId);

    /**
     * Find all match groups for a specific event and user
     * @param eventId The event ID
     * @param userId The current user ID
     * @return List of match groups filter by user ID type player
     */
    @Query("""
        SELECT mg FROM MatchGroup mg
        WHERE mg.event.id = :eventId
          AND mg.id IN (
            SELECT t.matchGroup.id FROM Team t
            WHERE t.id IN (
                SELECT tp.team.id FROM TeamPlayer tp
                WHERE tp.player.id IN (
                    SELECT p.id FROM Player p
                    WHERE p.user.id = :userId
                )
            )
        )
    """)
    List<MatchGroup> findAllByEventIdAndUserId(Long eventId, Long userId);

    /**
     * Delete all match groups for a specific event.
     * @param eventId The event ID.
     */
    void deleteByEventId(Long eventId);
}
