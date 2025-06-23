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

    private static final Long MINUTE_TO_CHECKIN = 60L;

    private final EventRepository eventRepository;

    // Not use Quartz for now
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void allowCheckin() {
        log.info("Checkin job");
        Timestamp anHourLater = Timestamp.valueOf(LocalDateTime.now().plusMinutes(MINUTE_TO_CHECKIN));
        // find event that need to start in an hour later
        var events = eventRepository.findAllByStatusAndEventTimeLessThan(EventStatus.PUBLISHED, anHourLater);
        if (!events.isEmpty()) {
            events.forEach(event -> {
                event.setStatus(EventStatus.CHECK_IN);
                event.setCheckInStart(Timestamp.valueOf(LocalDateTime.now()));
                event.setCheckInEnd(anHourLater);
                eventRepository.save(event);
            });
        }
    }
}
