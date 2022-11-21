package ru.yandex.practicum.filmorate.storage.daoImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.EmptyResultFromDataBaseException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.exceptions.ValidationFilmByIdException;
import ru.yandex.practicum.filmorate.models.Director;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.models.Genre;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class DaoDirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    public DaoDirectorStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Director> getAllDirector() {
        String sqlQuery = "SELECT * FROM DIRECTORS ";
        return jdbcTemplate.query(sqlQuery, this::mapRowToDirector);
    }

    public Director getDirectorById(Integer id) {
        String sqlQuery = "SELECT * FROM DIRECTORS WHERE id = ?";
        Director director;
        try {
            director = jdbcTemplate.queryForObject(sqlQuery, this::mapRowToDirector, id);
            return director;
        } catch (Exception e) {
            throw new EmptyResultFromDataBaseException("Режиссер c id: " + id + " не найден");
        }
    }

    public Director addDirector(Director director) {
        String sql = "SELECT * FROM DIRECTORS WHERE ID = ?";
        SqlRowSet directorRows = jdbcTemplate.queryForRowSet(sql, director.getId());
        if (directorRows.next()) {
            log.error("Такой режиссер уже существует! {}", director);
            throw new ValidationException("Такой режиссер уже существует!");
        }
        String sqlQuery = "INSERT INTO DIRECTORS(NAME) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);
        int id = Objects.requireNonNull(keyHolder.getKey()).intValue();
        director.setId(id);
        return director;
    }

    public Director updateDirector(Director director) {
        String sql = "SELECT * FROM DIRECTORS WHERE ID = ?";
        SqlRowSet directorRows = jdbcTemplate.queryForRowSet(sql, director.getId());
        if (!directorRows.next()) {
            log.error("Такого режиссера не существует! {}", director);
            throw new ValidationException("Такого режиссера не существует!");
        }
        String sqlQuery = "UPDATE DIRECTORS SET NAME = ? WHERE ID = ?";
        jdbcTemplate.update(sqlQuery,
                director.getName(),
                director.getId());
        return director;
    }

    public void removeDirector(Integer id) {
        String sqlQuery = "DELETE FROM FILM_DIRECTORS WHERE ID_DIRECTOR = ?";
        jdbcTemplate.update(sqlQuery, id);
        String sql = "DELETE FROM DIRECTORS WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public List<Director> getDirectorsByIdFilm(int id) {
        String sqlQuery = "SELECT * FROM DIRECTORS WHERE ID IN (SELECT ID_DIRECTOR FROM FILM_DIRECTORS WHERE ID_FILM = ?)";
        return jdbcTemplate.query(sqlQuery, this::mapRowToDirector, id);
    }

    public void addOrUpdateFilmDirectors(Film film) {
        if (film.getDirectors() != null) {
            String sqlDirectors = "DELETE FROM FILM_DIRECTORS WHERE ID_FILM = ?";
            jdbcTemplate.update(sqlDirectors, film.getId());
            for (Director director : film.getDirectors()) {
                sqlDirectors = "MERGE INTO FILM_DIRECTORS(ID_FILM, ID_DIRECTOR) VALUES (?, ?)";
                jdbcTemplate.update(sqlDirectors, film.getId(), director.getId());
            }
        } else {
            String sqlDirectors = "DELETE FROM FILM_DIRECTORS WHERE ID_FILM = ? ";
            jdbcTemplate.update(sqlDirectors, film.getId());
        }
    }

    private Director mapRowToDirector(ResultSet resultSet, int i) throws SQLException {
        return Director.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .build();
    }
}