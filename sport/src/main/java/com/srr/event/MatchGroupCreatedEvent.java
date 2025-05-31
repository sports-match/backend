package com.srr.event;

import com.srr.domain.MatchGroup;
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
