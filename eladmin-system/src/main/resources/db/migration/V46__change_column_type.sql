ALTER TABLE player_sport_rating
    ADD COLUMN sport_id BIGINT;

-- update sport_id
update player_sport_rating
set sport_id = (select id from sport where name = player_sport_rating.sport);

alter table player_sport_rating
    drop column sport;
