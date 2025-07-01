-- Create the question table
CREATE TABLE question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    text VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    order_index INT,
    min_value INT,
    max_value INT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create the player_answer table
CREATE TABLE player_answer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_value INT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (player_id) REFERENCES player(id),
    FOREIGN KEY (question_id) REFERENCES question(id)
);

-- Insert default questions for player self-assessment
INSERT INTO question (text, category, order_index, min_value, max_value) VALUES
-- Court Coverage questions
('How would you rate your ability to cover the entire court?', 'Court Coverage', 1, 1, 10),
('How well can you reach shots in the corners of the court?', 'Court Coverage', 2, 1, 10),
('How would you rate your lateral movement speed?', 'Court Coverage', 3, 1, 10),

-- Shot Quality questions
('How consistent is your forehand?', 'Shot Quality', 1, 1, 10),
('How consistent is your backhand?', 'Shot Quality', 2, 1, 10),
('How would you rate the power of your shots?', 'Shot Quality', 3, 1, 10),
('How accurate are your shots when aiming for specific targets?', 'Shot Quality', 4, 1, 10),

-- Consistency/Control questions
('How well can you maintain rallies without making errors?', 'Consistency/Control', 1, 1, 10),
('How well can you control the pace of the game?', 'Consistency/Control', 2, 1, 10),
('How consistent is your serve?', 'Consistency/Control', 3, 1, 10),

-- Power questions
('How powerful is your serve?', 'Power', 1, 1, 10),
('How well can you hit winners under pressure?', 'Power', 2, 1, 10),
('How would you rate your ability to smash/overhead?', 'Power', 3, 1, 10),

-- Anticipation & Strategy questions
('How well can you read your opponent\'s shots?', 'Anticipation & Strategy', 1, 1, 10),
('How would you rate your ability to construct points tactically?', 'Anticipation & Strategy', 2, 1, 10),
('How well do you adapt your strategy during a match?', 'Anticipation & Strategy', 3, 1, 10);
