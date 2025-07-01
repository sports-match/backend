-- Add sport_id and format columns to question table
-- Ensure 'Badminton' exists in sport table
INSERT INTO sport (name) SELECT 'Badminton' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sport WHERE name = 'Badminton');

ALTER TABLE question ADD COLUMN sport_id BIGINT;
ALTER TABLE question ADD COLUMN format VARCHAR(16) NOT NULL;
ALTER TABLE question ADD CONSTRAINT fk_question_sport FOREIGN KEY (sport_id) REFERENCES sport(id);
DELETE FROM question;
INSERT INTO question (text, category, order_index, min_value, max_value, sport_id, format)
VALUES
('How confident are you in your ability to serve accurately in doubles?', 'serving', 1, 1, 5, (SELECT id FROM sport WHERE name = 'Badminton'), 'DOUBLES'),
('How well do you coordinate with your partner during rallies?', 'teamwork', 2, 1, 5, (SELECT id FROM sport WHERE name = 'Badminton'), 'DOUBLES'),
('How effective are your net interceptions and front-court play in doubles?', 'net_play', 3, 1, 5, (SELECT id FROM sport WHERE name = 'Badminton'), 'DOUBLES'),
('How comfortable are you with defensive formations and quick transitions in doubles?', 'defense', 4, 1, 5, (SELECT id FROM sport WHERE name = 'Badminton'), 'DOUBLES'),
('How strong is your smash and attacking play from the back-court in doubles?', 'attack', 5, 1, 5, (SELECT id FROM sport WHERE name = 'Badminton'), 'DOUBLES'),
('How well do you communicate and strategize with your partner during a match?', 'communication', 6, 1, 5, (SELECT id FROM sport WHERE name = 'Badminton'), 'DOUBLES');
