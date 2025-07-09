ALTER TABLE match_group RENAME COLUMN is_finalized TO finalized;
ALTER TABLE match_group MODIFY COLUMN finalized BOOL DEFAULT 0 NOT NULL;
