package com.srr.event.scheduler;

import com.srr.enumeration.EventStatus;
import com.srr.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnableCheckinScheduler {

    private final EventRepository eventRepository;

    // Not use Quartz for now
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void allowCheckin() {
        log.info("Checkin job");
        // find event that need to start in an hour later
        var events = eventRepository.findAllByStatusAndCheckInStartIsLessThan(EventStatus.PUBLISHED,
                Timestamp.valueOf(LocalDateTime.now()));
        if (!events.isEmpty()) {
            events.forEach(event -> {
                event.setStatus(EventStatus.CHECK_IN);
                eventRepository.save(event);
            });
        }

        var eventsCheckInClosed = eventRepository.findAllByStatusAndCheckInEndIsLessThan(EventStatus.CHECK_IN,
                Timestamp.valueOf(LocalDateTime.now()));
        if (!eventsCheckInClosed.isEmpty()) {
            eventsCheckInClosed.forEach(event -> {
                event.setStatus(EventStatus.IN_PROGRESS);
                eventRepository.save(event);
            });
        }
    }
}
