-- Add status column to event_match table
ALTER TABLE event_match ADD COLUMN status VARCHAR(20) DEFAULT 'SCHEDULED';
