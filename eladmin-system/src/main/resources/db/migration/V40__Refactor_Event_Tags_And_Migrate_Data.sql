-- Create tag table
CREATE TABLE tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    INDEX idx_tag_name (name) -- Index for faster lookups by name
);
-- Create event_tag join table
CREATE TABLE event_tag (
    event_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (event_id, tag_id),
    CONSTRAINT fk_event_event_tag FOREIGN KEY (event_id) REFERENCES event (id) ON DELETE CASCADE,
    CONSTRAINT fk_tag_event_tag FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE CASCADE
);
-- Drop the old tags column from the event table
ALTER TABLE event DROP COLUMN tags;
