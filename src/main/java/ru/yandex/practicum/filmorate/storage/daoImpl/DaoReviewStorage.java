package ru.yandex.practicum.filmorate.storage.daoImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.SQLRequests;
import ru.yandex.practicum.filmorate.exceptions.EmptyResultFromDataBaseException;
import ru.yandex.practicum.filmorate.mapper.ReviewRowMapper;
import ru.yandex.practicum.filmorate.models.Review;
import ru.yandex.practicum.filmorate.storage.interf.ReviewStorage;

import java.util.List;
import java.util.Map;

@Component
@Primary
@RequiredArgsConstructor
public class DaoReviewStorage implements ReviewStorage<Review> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Review> getAll() {
        return jdbcTemplate.query(
                SQLRequests.GET_REVIEWS,
                new ReviewRowMapper()
        );
    }

    @Override
    public List<Review> getByParams(Map<String, Integer> params) {
        StringBuilder queryString = new StringBuilder(SQLRequests.GET_REVIEWS_BY_FILM_ID);
        int limit = 10;
        if(!params.isEmpty()) {
            if (params.containsKey("filmId")) {
                queryString.append(" WHERE ")
                        .append("film_id = ")
                        .append(params.get("filmId"));
            }

            queryString.append(" ORDER BY useful DESC");

            if (params.containsKey("count")) {
                limit = Integer.parseInt(String.valueOf(params.get("count")));
            }
        } else {
            queryString.append(" ORDER BY useful DESC");
        }

        queryString.append(" LIMIT ").append(limit);

        return jdbcTemplate.query(
                queryString.toString(),
                new ReviewRowMapper()
        );
    }

    @Override
    public Review create(Review review) {
        createOrUpdate(
                SQLRequests.CREATE_REVIEW,
                new Object[]{
                        review.getUserId(),
                        review.getFilmId(),
                        review.getContent(),
                        review.getIsPositive(),
                        0
                }
        );

        return jdbcTemplate.query(
                        SQLRequests.GET_LAST_REVIEW,
                        new ReviewRowMapper()
                )
                .stream()
                .findAny()
                .orElseThrow(() -> new EmptyResultFromDataBaseException("Ничего не найдено!"));
    }

    @Override
    public Review update(Review review) {
        createOrUpdate(
                SQLRequests.UPDATE_REVIEW,
                new Object[]{
                        review.getContent(),
                        review.getIsPositive(),
                        0,
                        review.getReviewId()
                }
        );

        return jdbcTemplate.query(
                        SQLRequests.GET_LAST_REVIEW_AFTER_UPDATE,
                        new ReviewRowMapper(),
                        review.getReviewId()
                )
                .stream()
                .findAny()
                .orElseThrow(() -> new EmptyResultFromDataBaseException("Ничего не найдено!"));
    }

    @Override
    public Review getById(Long reviewId) {
        return jdbcTemplate.query(
            SQLRequests.GET_REVIEWS_BY_ID,
            new ReviewRowMapper(),
            reviewId
        )
                .stream()
                .findAny()
                .orElseThrow(() -> new EmptyResultFromDataBaseException("Отзыв не найден"));
    }

    @Override
    public void addLikeToReview(Integer reviewId, Integer userId) {
        jdbcTemplate.update(
                SQLRequests.INSERT_LIKE_TO_REVIEW,
                reviewId,
                userId
        );

        jdbcTemplate.update(
                SQLRequests.UPDATE_REVIEW_AFTER_ADD_LIKE,
                reviewId
        );

        jdbcTemplate.update(
                SQLRequests.DELETE_DISLIKE_FROM_REVIEW,
                reviewId,
                userId
        );
    }

    @Override
    public void addDisLikeToReview(Integer reviewId, Integer userId) {
        jdbcTemplate.update(
                SQLRequests.INSERT_DISLIKE_TO_REVIEW,
                reviewId,
                userId
        );

        jdbcTemplate.update(
                SQLRequests.UPDATE_REVIEW_AFTER_ADD_DISLIKE,
                reviewId
        );

        jdbcTemplate.update(
                SQLRequests.DELETE_LIKE_FROM_REVIEW,
                reviewId,
                userId
        );
    }

    @Override
    public void delete(Long reviewId) {
        jdbcTemplate.update(
                "DELETE FROM reviews WHERE review_id = ?",
                reviewId
        );
    }

    @Override
    public Boolean checkReviewExists(Integer reviewId) {
        Integer reviewCount = jdbcTemplate.queryForObject(
                SQLRequests.GET_REVIEW_COUNT_ID,
                Integer.class,
                reviewId
        );
        return reviewCount != null && reviewCount > 0;
    }

    @Override
    public Boolean checkFilmExists(Integer filmId) {
        Integer filmsCount = jdbcTemplate.queryForObject(
                SQLRequests.GET_FILM_BY_ID,
                Integer.class,
                filmId
        );

        return filmsCount != null && filmsCount > 0;
    }

    @Override
    public Boolean checkUserExists(Integer userId) {
        Integer usersCount = jdbcTemplate.queryForObject(
                SQLRequests.GET_USER_BY_ID,
                Integer.class,
                userId
        );

        return usersCount != null && usersCount > 0;
    }

    private void createOrUpdate(String query, Object[] obj) {
        jdbcTemplate.update(
                query,
                obj

        );
    }
}
