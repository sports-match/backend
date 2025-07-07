-- Remove duplicate records from player_sport_rating table
DELETE p1 FROM player_sport_rating p1 INNER JOIN player_sport_rating p2
WHERE p1.id > p2.id
  AND p1.player_id = p2.player_id
  AND p1.sport_id = p2.sport_id
  AND p1.format = p2.format;

-- Add unique constraint on player_sport_rating table for columns (player_id, sport, format)
ALTER TABLE player_sport_rating 
ADD CONSTRAINT uk_player_sport_rating_player_sport_format 
UNIQUE (player_id, sport, format);