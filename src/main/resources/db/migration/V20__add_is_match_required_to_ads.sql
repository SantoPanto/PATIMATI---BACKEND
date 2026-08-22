ALTER TABLE ads ADD COLUMN is_match_required BOOLEAN DEFAULT TRUE;
UPDATE ads SET is_match_required = TRUE WHERE is_match_required IS NULL;
