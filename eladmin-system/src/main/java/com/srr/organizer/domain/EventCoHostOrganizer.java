package com.srr.organizer.domain;

import com.srr.event.domain.Event;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Cheyleap
 * @description Event co host organizer entity
 * @date 2025-07-03
 **/
@Entity
@Data
@Table(name = "event_co_host_organizer")
public class EventCoHostOrganizer implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    @ApiModelProperty(value = "id", hidden = true)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", referencedColumnName = "id", nullable = false)
    @ApiModelProperty(value = "Event that this co host is associated with")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "event_organizer_id", referencedColumnName = "id", nullable = false)
    @ApiModelProperty(value = "Event organizer that this co host is associated with")
    private EventOrganizer eventOrganizer;
}
