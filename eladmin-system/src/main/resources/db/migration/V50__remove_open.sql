-- Add court_numbers column to match_group table
UPDATE event set status = 'PUBLISHED' where status = 'OPEN';