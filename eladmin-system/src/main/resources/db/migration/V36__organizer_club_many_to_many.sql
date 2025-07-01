-- Migration for organizer-club many-to-many relationship
CREATE TABLE IF NOT EXISTS organizer_club (
    organizer_id BIGINT NOT NULL,
    club_id BIGINT NOT NULL,
    PRIMARY KEY (organizer_id, club_id),
    CONSTRAINT fk_organizer FOREIGN KEY (organizer_id) REFERENCES event_organizer(id) ON DELETE CASCADE,
    CONSTRAINT fk_club FOREIGN KEY (club_id) REFERENCES club(id) ON DELETE CASCADE
);
