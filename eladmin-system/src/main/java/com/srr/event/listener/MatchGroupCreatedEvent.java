package com.srr.event.listener;

import com.srr.event.domain.MatchGroup;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event that is fired when a match group is created
 */
@Getter
public class MatchGroupCreatedEvent extends ApplicationEvent {
    
    private final MatchGroup matchGroup;
    
    public MatchGroupCreatedEvent(Object source, MatchGroup matchGroup) {
        super(source);
        this.matchGroup = matchGroup;
    }
}
