package com.srr.event.listener;

import com.srr.service.MatchGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listener for match group related events
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MatchGroupEventListener {

    private final MatchGenerationService matchGenerationService;
    
    /**
     * Listen for match group creation events and generate matches
     * 
     * @param event The match group created event
     */
    @EventListener
    @Transactional
    public void handleMatchGroupCreatedEvent(MatchGroupCreatedEvent event) {
        log.info("Handling match group created event for group: {}", event.getMatchGroup().getId());
        int matchesGenerated = matchGenerationService.generateMatchesForGroup(event.getMatchGroup());
        log.info("Generated {} matches for match group {}", matchesGenerated, event.getMatchGroup().getId());
    }
}
