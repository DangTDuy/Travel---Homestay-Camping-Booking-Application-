-- Allow guide_id to be null in travels table
ALTER TABLE travels MODIFY COLUMN guide_id BIGINT NULL;
