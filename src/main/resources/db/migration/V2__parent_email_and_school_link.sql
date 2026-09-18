ALTER TABLE parents ADD COLUMN email VARCHAR(255);

-- Backfill para linhas pré-existentes (anteriores ao email obrigatório).
UPDATE parents
SET email = 'unknown-' || id || '@placeholder.local'
WHERE email IS NULL;

ALTER TABLE parents ALTER COLUMN email SET NOT NULL;
ALTER TABLE parents ADD CONSTRAINT uq_parents_email UNIQUE (email);

CREATE TABLE parent_school (
    parent_id UUID REFERENCES parents(id),
    school_id UUID REFERENCES schools(id),
    PRIMARY KEY (parent_id, school_id)
);
