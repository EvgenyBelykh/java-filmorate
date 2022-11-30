package ru.yandex.practicum.filmorate;

public class Constants {
    // REVIEWS
    public static final String GET_REVIEWS = "SELECT * FROM reviews ORDER BY useful ASC";

    public static final String GET_REVIEW_BY_ID = "SELECT * FROM reviews WHERE review_id = ?";

    public static final String GET_REVIEW_COUNT_ID = "SELECT COUNT(*) FROM reviews WHERE review_id = ?";

    public static final String GET_REVIEWS_BY_FILM_ID = "SELECT * FROM reviews";

    public static final String CREATE_REVIEW = "INSERT INTO reviews SET user_id = ?, film_id = ?, content = ?, is_positive = ?, useful = ?";

    public static final String UPDATE_REVIEW = "UPDATE reviews SET content = ?, is_positive = ?, useful = ? WHERE review_id = ?";

    public static final String GET_LAST_REVIEW = "SELECT * FROM reviews ORDER BY review_id DESC LIMIT 1";

    public static final String GET_LAST_REVIEW_AFTER_UPDATE = "SELECT * FROM reviews WHERE review_id = ?";

    public static final String GET_REVIEWS_BY_ID = "SELECT * FROM reviews WHERE review_id = ?";

    public static final String INSERT_LIKE_TO_REVIEW = "INSERT INTO review_like (review_id, user_id) VALUES (?, ?)";

    public static final String INSERT_DISLIKE_TO_REVIEW = "INSERT INTO review_dislike (review_id, user_id) VALUES (?, ?)";

    public static final String DELETE_DISLIKE_FROM_REVIEW = "DELETE FROM review_dislike WHERE review_id = ? AND user_id = ?";

    public static final String DELETE_LIKE_FROM_REVIEW = "DELETE FROM review_like WHERE review_id = ? AND user_id = ?";

    public static final String GET_FILM_BY_ID = "SELECT COUNT(*) FROM films WHERE id = ?";

    public static final String GET_USER_BY_ID = "SELECT COUNT(*) FROM users WHERE id = ?";

    public static final String UPDATE_REVIEW_AFTER_ADD_DISLIKE = "UPDATE reviews r1 SET useful = -1 WHERE review_id = ?";

    public static final String UPDATE_REVIEW_AFTER_ADD_LIKE = "UPDATE reviews r1 SET useful = ((SELECT r2.useful FROM reviews r2 WHERE r1.review_id = r2.review_id) + 1) WHERE review_id = ?";

    public static final String SQL_GET_RECOMMENDATIONS = "SELECT * FROM FILMS film " +
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
}
