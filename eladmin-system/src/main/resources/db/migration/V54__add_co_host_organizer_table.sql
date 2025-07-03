create table event_co_host_organizer
(
    id         bigint auto_increment primary key,
    event_organizer_id    bigint not null,
    event_id    bigint not null,
    CONSTRAINT fk_event_organizer_id_event_organizer_id FOREIGN KEY (event_organizer_id) REFERENCES event_organizer (id),
    CONSTRAINT fk_event_id_event_id FOREIGN KEY (event_id) REFERENCES event (id)
);
