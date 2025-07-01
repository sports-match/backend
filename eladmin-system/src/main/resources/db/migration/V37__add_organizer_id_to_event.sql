ALTER TABLE event ADD COLUMN organizer_id BIGINT;
ALTER TABLE event ADD CONSTRAINT fk_event_organizer FOREIGN KEY (organizer_id) REFERENCES event_organizer(id);