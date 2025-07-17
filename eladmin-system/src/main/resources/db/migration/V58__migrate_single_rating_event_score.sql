INSERT INTO player_sport_rating (player_id, sport_id, format, rate_score, rate_band, provisional, create_time,
                                 update_time)
SELECT double_ratings.player_id,
       double_ratings.sport_id,
       'SINGLE' AS format,
       double_ratings.rate_score,
       double_ratings.rate_band,
       double_ratings.provisional,
       NOW()    AS create_time,
       NOW()    AS update_time
FROM player_sport_rating double_ratings
WHERE double_ratings.format = 'DOUBLE'
  AND NOT EXISTS (SELECT 1
                  FROM player_sport_rating single_check
                  WHERE single_check.player_id = double_ratings.player_id
                    AND single_check.sport_id = double_ratings.sport_id
                    AND single_check.format = 'SINGLE');
