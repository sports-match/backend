-- Add status column to team_player table
ALTER TABLE team_player ADD COLUMN status VARCHAR(32) DEFAULT 'REGISTERED';
ALTER TABLE team_player ADD COLUMN check_in_time TIMESTAMP NULL;

-- Add status column to team table
ALTER TABLE team ADD COLUMN status VARCHAR(32) DEFAULT 'REGISTERED';
