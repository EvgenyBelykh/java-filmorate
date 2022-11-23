package ru.yandex.practicum.filmorate.storage.daoImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.EmptyResultFromDataBaseException;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Slf4j
public class DaoGenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public DaoGenreStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Genre> getAllGenre() {
        String sqlQuery = "SELECT * " +
                "FROM genres ";

        return jdbcTemplate.query(sqlQuery, this::mapRowToGenre);
    }
    public Genre getGenreById(Integer id) {
        String sqlQuery = "SELECT * " +
                "FROM genres " +
                "WHERE id = ?";
        Genre genre;

        try {
            genre = jdbcTemplate.queryForObject(sqlQuery, this::mapRowToGenre, id);
            return genre;
        } catch (Exception e) {
            throw new EmptyResultFromDataBaseException("Genre c id: " + id + " не найден");
        }
    }

    public void addOrUpdateFilmGenres(Film film) {
        int id = film.getId();

        if (film.getGenres() != null) {
            String sqlGenre = "DELETE " +
                    "FROM film_genres " +
                    "WHERE id_film = ? ";
            jdbcTemplate.update(sqlGenre, film.getId());

            for (Genre genre : film.getGenres()) {
                sqlGenre = "MERGE INTO film_genres(id_film, id_genre) " +
                        "VALUES (?, ?)";

                jdbcTemplate.update(sqlGenre, id, genre.getId());
            }
        } else {
            String sqlGenre = "DELETE " +
                    "FROM film_genres " +
                    "WHERE id_film = ? ";

            jdbcTemplate.update(sqlGenre, film.getId());
        }
    }

    public List<Genre> getGenresByIdFilm(int id) {
        String sqlQuery = "SELECT * " +
                "FROM genres " +
                "WHERE id IN" +
                "(" +
                "SELECT id_genre " +
                "FROM film_genres " +
                "WHERE id_film = ?" +
                ")";

        return jdbcTemplate.query(sqlQuery, this::mapRowToGenre, id);
    }
    private Genre mapRowToGenre(ResultSet resultSet, int i) throws SQLException {
        return Genre.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .build();
    }
}
