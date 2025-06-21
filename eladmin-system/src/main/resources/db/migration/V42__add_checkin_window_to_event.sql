ALTER TABLE event
  ADD COLUMN check_in_start DATETIME NULL AFTER check_in_at,
  ADD COLUMN check_in_end DATETIME NULL AFTER check_in_start,
  ADD COLUMN allow_self_check_in TINYINT(1) DEFAULT 1 AFTER check_in_end;
