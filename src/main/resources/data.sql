DELETE FROM USERS;
DELETE FROM FILMS;
DELETE FROM RATE;
DELETE FROM DIRECTORS;
DELETE FROM USERS_FRIENDS;
DELETE FROM FILM_GENRES;
DELETE FROM reviews;
DELETE FROM review_like;
DELETE FROM review_dislike;
DELETE FROM events;
DELETE FROM FILM_DIRECTORS;

ALTER TABLE USERS ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE FILMS ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE reviews ALTER COLUMN review_id RESTART WITH 1;
ALTER TABLE events ALTER COLUMN event_id RESTART WITH 1;
ALTER TABLE DIRECTORS ALTER COLUMN ID RESTART WITH 1;

MERGE INTO mpa KEY (id)
    VALUES (1, 'G' ),
           (2, 'PG'),
           (3, 'PG-13'),
           (4, 'R'),
           (5, 'NC-17');

MERGE INTO genres KEY (id)
    VALUES ( 1, 'Комедия' ),
           ( 2, 'Драма' ),
           ( 3, 'Мультфильм' ),
           ( 4, 'Триллер' ),
           ( 5, 'Документальный' ),
           ( 6, 'Боевик' );
