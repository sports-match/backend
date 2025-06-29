ALTER TABLE player_sport_rating
    ADD COLUMN sport_id BIGINT;

-- update sport_id
UPDATE player_sport_rating psr
    JOIN sport s
ON s.name like psr.sport
SET psr.sport_id = s.id;

ALTER TABLE `sport`
    ADD CONSTRAINT `uk_sport_name` UNIQUE (`name`);

-- Ensure 'Badminton' sport exists with explicit fields
INSERT INTO sport
    (name, description, create_time, update_time, icon, sort, enabled)
SELECT 'Badminton',
       'Auto-added by V46',
       NOW(), NOW(),
       NULL,            -- icon
       0,               -- sort
       b'1'             -- enabled (bit 1)
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sport WHERE name = 'Badminton');

alter table player_sport_rating
    drop column sport;
