-- Données de démonstration (utilisées dans le README et le fichier requests/library.http)
INSERT INTO ouvrage (titre, auteur, annee_publication, isbn, categorie_id)
SELECT 'Clean Code', 'Robert C. Martin', 2008, '9780132350884', id FROM categorie WHERE nom = 'Informatique';
INSERT INTO ouvrage (titre, auteur, annee_publication, isbn, categorie_id)
SELECT 'Domain-Driven Design', 'Eric Evans', 2003, '9780321125217', id FROM categorie WHERE nom = 'Informatique';
INSERT INTO ouvrage (titre, auteur, annee_publication, isbn, categorie_id)
SELECT 'Les Misérables', 'Victor Hugo', 1862, '9782070409228', id FROM categorie WHERE nom = 'Littérature';

INSERT INTO exemplaire (ouvrage_id, code_barre) SELECT id, 'EX-CC-001'  FROM ouvrage WHERE isbn = '9780132350884';
INSERT INTO exemplaire (ouvrage_id, code_barre) SELECT id, 'EX-CC-002'  FROM ouvrage WHERE isbn = '9780132350884';
INSERT INTO exemplaire (ouvrage_id, code_barre) SELECT id, 'EX-DDD-001' FROM ouvrage WHERE isbn = '9780321125217';
INSERT INTO exemplaire (ouvrage_id, code_barre) SELECT id, 'EX-LM-001'  FROM ouvrage WHERE isbn = '9782070409228';
