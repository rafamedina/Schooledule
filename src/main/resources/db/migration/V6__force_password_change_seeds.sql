-- Add must_change_password column (default false for all existing users)
ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS must_change_password BOOLEAN NOT NULL DEFAULT FALSE;

-- Force password change on first login for all seed users created in V1
UPDATE usuarios
SET must_change_password = TRUE
WHERE id IN (1, 2, 3, 4);
