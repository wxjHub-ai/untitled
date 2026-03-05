-- Fix for missing 'deleted' column
ALTER TABLE products ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE;
