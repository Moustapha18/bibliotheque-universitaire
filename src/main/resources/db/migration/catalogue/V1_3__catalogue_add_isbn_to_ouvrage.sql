-- Évolution d'une table existante : ajout de l'ISBN (unique) sur ouvrage
ALTER TABLE ouvrage ADD COLUMN isbn VARCHAR(13);
CREATE UNIQUE INDEX uk_ouvrage_isbn ON ouvrage (isbn);
