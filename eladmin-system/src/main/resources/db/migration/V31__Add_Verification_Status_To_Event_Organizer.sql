ALTER TABLE `event_organizer`
ADD COLUMN `verification_status` VARCHAR(255) DEFAULT 'PENDING' NULL COMMENT 'Verification status of the event organizer (PENDING, VERIFIED, REJECTED)';

-- Update existing rows to have PENDING status if they are NULL, though default should handle new ones.
UPDATE `event_organizer` SET `verification_status` = 'PENDING' WHERE `verification_status` IS NULL;
