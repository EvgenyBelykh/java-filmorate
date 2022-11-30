package ru.yandex.practicum.filmorate;

public class SQLRequests {
    // REVIEWS
    public static final String GET_REVIEWS = "SELECT * FROM reviews ORDER BY useful ASC";

    public static final String GET_REVIEW_BY_ID = "SELECT * FROM reviews WHERE review_id = ?";

    public static final String GET_REVIEW_COUNT_ID = "SELECT COUNT(*) FROM reviews WHERE review_id = ?";

    public static final String GET_REVIEWS_BY_FILM_ID = "SELECT * FROM reviews";

    public static final String CREATE_REVIEW = "INSERT INTO reviews SET user_id = ?, film_id = ?, content = ?" +
            ", is_positive = ?, useful = ?";

    public static final String UPDATE_REVIEW = "UPDATE reviews SET content = ?, is_positive = ?" +
            ", useful = ? WHERE review_id = ?";

    public static final String GET_LAST_REVIEW = "SELECT * FROM reviews ORDER BY review_id DESC LIMIT 1";

    public static final String GET_LAST_REVIEW_AFTER_UPDATE = "SELECT * FROM reviews WHERE review_id = ?";

    public static final String GET_REVIEWS_BY_ID = "SELECT * FROM reviews WHERE review_id = ?";

    public static final String INSERT_LIKE_TO_REVIEW = "INSERT INTO review_like (review_id, user_id) VALUES (?, ?)";

    public static final String INSERT_DISLIKE_TO_REVIEW = "INSERT INTO review_dislike (review_id, user_id)" +
            " VALUES (?, ?)";

    public static final String DELETE_DISLIKE_FROM_REVIEW = "DELETE FROM review_dislike" +
            " WHERE review_id = ? AND user_id = ?";

    public static final String DELETE_LIKE_FROM_REVIEW = "DELETE FROM review_like WHERE review_id = ? AND user_id = ?";

    public static final String GET_FILM_BY_ID = "SELECT COUNT(*) FROM films WHERE id = ?";

    public static final String GET_USER_BY_ID = "SELECT COUNT(*) FROM users WHERE id = ?";

    public static final String UPDATE_REVIEW_AFTER_ADD_DISLIKE = "UPDATE reviews r1 SET useful = -1" +
            " WHERE review_id = ?";

    public static final String UPDATE_REVIEW_AFTER_ADD_LIKE = "UPDATE reviews r1 SET useful = ((SELECT r2.useful FROM " +
            "reviews r2 WHERE r1.review_id = r2.review_id) + 1) WHERE review_id = ?";

    public static final String GET_RECOMMENDATIONS = "SELECT * FROM FILMS film " +
            "WHERE film.rate > 5 AND film.ID IN " +
            "(SELECT rate.ID_FILM  FROM RATE rate " +
            "WHERE rate.ID_USER IN " +
            "(SELECT ID_USER FROM RATE l " +
            "WHERE l.ID_USER != ? " +
            "AND " +
            "l.ID_FILM IN " +
            "(SELECT l.ID_FILM FROM RATE l " +
            "WHERE l.ID_USER = ?)" +
            ") " +
            "GROUP BY rate.ID_FILM " +
            "HAVING rate.ID_FILM NOT IN " +
            "(SELECT l.ID_FILM FROM RATE l WHERE l.ID_USER = ?)" +
            ")";
    public static final String GET_FILMS = "SELECT * FROM films";
    public static final String GET_FILMS_BY_ID = "SELECT id, name, description, release_date, duration, rate, mpa " +
            "FROM films WHERE id = ?";

    public static final String ADD_FILM = "INSERT INTO films(name, description, release_date, duration, mpa)" +
            "VALUES (?, ?, ?, ?, ?)";

    public static final String UPDATE_FILM = "UPDATE films SET description = ?, release_date = ?, duration = ?" +
            ", rate = ?, mpa = ? WHERE id = ?";
    public static final String CHECK_NAME_BY_ID_FROM_DB = "SELECT name FROM films WHERE id = ? ";
    public static final String DELETE_GENRE_FROM_FILM_GENRES_TABLE_BY_ID_FILM = "DELETE " +
            "FROM film_genres WHERE id_film = ? ";

    public static final String DELETE_FROM_FILM_DIRECTORS_TABLE_BY_ID_FILM = "DELETE " +
            "FROM FILM_DIRECTORS WHERE ID_FILM = ? ";

    public static final String DELETE_FILM = "DELETE FROM films WHERE id = ?";

    public static final String ADD_RATE_FROM_USER_BY_ID_FILM = "INSERT INTO rate(id_user, id_film, rate) VALUES(?, ?, ?)";
    public static final String UPDATE_RATE_FROM_USER_BY_ID_FILM = "UPDATE rate SET rate = ? " +
            "WHERE id_user = ? AND id_film = ? ";

    public static final String DELETE_RATE_FROM_USER_BY_ID_FILM = "DELETE FROM rate WHERE id_user = ? AND id_film = ? ";

    public static final String GET_RATE_FROM_USER_BY_ID_FILM = "SELECT rate FROM FILMS WHERE ID = ? ";

    public static final String UPDATE_RATE_FILM = "UPDATE films SET rate = ? WHERE id = ?";

    public static final String GET_RATE_FROM_RATE_TABLE_BY_ID_FILM = "SELECT AVG(rate) FROM rate WHERE id_film = ? ";

    public static final String GET_MOST_POPULAR_FILM_BY_RATE = "SELECT films.* FROM films " +
            "ORDER BY FILMS.RATE DESC LIMIT ?";

    public static final String GET_MOST_POPULAR_FILM_BY_RATE_AND_GENRE_AND_YEAR = "SELECT films.* " +
            "FROM films " +
            "LEFT JOIN film_genres ON film_genres.id_film = films.id " +
            "WHERE EXTRACT (YEAR FROM films.release_date ) = ? " +
            "AND film_genres.id_genre = ? " +
            "ORDER BY FILMS.RATE DESC " +
            "LIMIT ?";

    public static final String GET_MOST_POPULAR_FILM_BY_RATE_AND_YEAR = "SELECT films.* FROM films " +
            "WHERE EXTRACT (YEAR FROM films.release_date ) = ? " +
            "ORDER BY FILMS.RATE DESC " +
            "LIMIT ?";

    public static final String GET_MOST_POPULAR_FILM_BY_RATE_AND_GENRE = "SELECT films.* FROM films " +
            "LEFT JOIN film_genres ON film_genres.id_film = films.id " +
            "WHERE film_genres.id_genre = ? " +
            "ORDER BY FILMS.RATE DESC " +
            "LIMIT ?";

    public static final String GET_COMMON_FILM = "SELECT films.* FROM films " +
            "WHERE films.id IN (SELECT DISTINCT id_film FROM rate WHERE id_user = ? AND ?) " +
            "ORDER BY FILMS.RATE DESC";

    public static final String GET_DIRECTORS_BY_ID = "SELECT * FROM DIRECTORS WHERE ID = ?";

    public static final String GET_SORTED_FILMS_BY_RATE_BY_ID_DIRECTORS = "SELECT FILMS.* FROM FILMS " +
            "LEFT JOIN FILM_DIRECTORS ON FILM_DIRECTORS.ID_FILM = films.ID " +
            "WHERE ID_DIRECTOR = ? " +
            "ORDER BY FILMS.RATE DESC";

    public static final String GET_SORTED_FILMS_BY_YEAR_BY_ID_DIRECTORS = "SELECT FILMS.* FROM FILMS " +
            "LEFT JOIN FILM_DIRECTORS ON FILM_DIRECTORS.ID_FILM = films.ID " +
            "WHERE ID_DIRECTOR = ? " +
            "ORDER BY FILMS.RELEASE_DATE";
}
