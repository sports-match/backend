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
        // 1. Transition from PUBLISHED to CHECK_IN when check-in window opens
        var eventsToOpenCheckIn = eventRepository.findAllByStatusAndCheckInStartIsNotNullAndCheckInStartLessThan(
                EventStatus.PUBLISHED, Timestamp.valueOf(LocalDateTime.now())
        );
        if (!eventsToOpenCheckIn.isEmpty()) {
            eventsToOpenCheckIn.forEach(event -> {
                event.setStatus(EventStatus.CHECK_IN);
                eventRepository.save(event);
            });
        }

        // 2. Transition from CHECK_IN to IN_PROGRESS when event starts
//        var eventsToStart = eventRepository.findAllByStatusAndEventTimeLessThan(
//                EventStatus.CHECK_IN, Timestamp.valueOf(LocalDateTime.now())
//        );
//        if (!eventsToStart.isEmpty()) {
//            eventsToStart.forEach(event -> {
//                event.setStatus(EventStatus.IN_PROGRESS);
//                eventRepository.save(event);
//            });
//        }
    }
}
