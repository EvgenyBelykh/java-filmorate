package ru.yandex.practicum.filmorate.storage.daoImpl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.*;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.services.DirectorService;
import ru.yandex.practicum.filmorate.services.GenreService;
import ru.yandex.practicum.filmorate.services.MpaService;
import ru.yandex.practicum.filmorate.storage.interf.FilmStorage;

import java.sql.*;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.Year;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class DaoFilmStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final DirectorService directorService;

    public DaoFilmStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaService = new MpaService(new DaoMpaStorage(jdbcTemplate));
        this.genreService = new GenreService(new DaoGenreStorage(jdbcTemplate));
        this.directorService = new DirectorService(new DaoDirectorStorage(jdbcTemplate));
    }

    @Override
    public List<Film> getFilms() {
        String sqlQuery = "SELECT * " +
                "FROM films";

        return jdbcTemplate.query(sqlQuery, this::mapRowToFilms);
    }

    @Override
    public Film getFilmById(Integer filmId) {
        try {
            String sqlQuery = "SELECT id, name, description, release_date, duration, rate, mpa " +
                    "FROM films " +
                    "WHERE id = ?";

            return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToFilms, filmId);
        } catch (Exception e) {
            throw new ValidationException("Фильм c id: " + filmId + " не содержится в базе");
        }
    }

    @Override
    public Film addFilm(Film film) {
        String sqlQuery = "INSERT INTO films(name, description, release_date, duration, rate, mpa)" +
                "VALUES (?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getRate());
            checkMpaIsNull(ps, film);
            return ps;
        }, keyHolder);

        int id = Objects.requireNonNull(keyHolder.getKey()).intValue();
        film.setId(id);

        genreService.addOrUpdateFilmGenres(film);
        directorService.addOrUpdateFilmDirectors(film);

        return getFilmById(id);
    }

    @Override
    public Film updateFilm(Film film) {
        getFilmById(film.getId());
        String sqlQuery = "UPDATE films SET " +
                "name = ?, description = ?, release_date = ?, duration = ?, rate = ?, mpa = ? " +
                "WHERE id = ?";

        jdbcTemplate.update(sqlQuery
                , film.getName()
                , film.getDescription()
                , film.getReleaseDate()
                , film.getDuration()
                , film.getRate()
                , film.getMpa().getId()
                , film.getId());

        genreService.addOrUpdateFilmGenres(film);
        directorService.addOrUpdateFilmDirectors(film);

        return getFilmById(film.getId());
    }

    @Override
    public void removeFilm(Film film) {

        //удаляем жанры в связанной таблице film_genres
        String sqlQueryGenre = "DELETE " +
                "FROM film_genres " +
                "WHERE id_film = ? ";

        jdbcTemplate.update(sqlQueryGenre, film.getId());

        //удаляем режиссеров в связанной таблице film_directors
        String sqlQueryDirector = "DELETE " +
                "FROM FILM_DIRECTORS " +
                "WHERE ID_FILM = ? ";

        jdbcTemplate.update(sqlQueryDirector, film.getId());

        String sqlQuery = "DELETE " +
                "FROM films " +
                "WHERE id = ?";

        jdbcTemplate.update(sqlQuery, film.getId());
    }

    @Override
    public Film addLikeFromUserById(Integer filmId, Integer userId) {
        String sqlQuery = "INSERT INTO likes(id_user, id_film) " +
                "VALUES(?, ?)";

        jdbcTemplate.update(sqlQuery, userId, filmId);
        return getFilmById(filmId);
    }

    @Override
    public Film removeLikeFromUserById(Integer filmId, Integer userId) {
        String sqlQuery = "DELETE " +
                "FROM likes " +
                "WHERE id_user = ? AND id_film = ? ";

        jdbcTemplate.update(sqlQuery, userId, filmId);
        return getFilmById(filmId);
    }

    @Override
    public List<Film> getMostPopularFilmByCountLikes(Integer cnt, Integer genreId, Year year) {
        if(genreId == null && year == null){
            //запрос популярных фильмов по лайкам все годов и жанров
            String sqlQuery = "SELECT films.* " +
                    "FROM films " +
                    "LEFT JOIN likes ON likes.id_film = films.id " +
                    "GROUP BY films.id " +
                    "ORDER BY COUNT(likes.id_user) DESC " +
                    "LIMIT ?;";
            return jdbcTemplate.query(sqlQuery, this::mapRowToFilms, cnt);

        } else if(genreId != null && year != null){
            //запрос популярных фильмов по лайкам конкретного года и жанра
            String sqlQuery = "SELECT films.* " +
                    "FROM films " +
                    "LEFT JOIN likes ON likes.id_film = films.id " +
                    "LEFT JOIN film_genres ON film_genres.id_film = films.id " +
                    "WHERE EXTRACT (YEAR FROM films.release_date ) = ? " +
                    "AND film_genres.id_genre = ? " +
                    "GROUP BY films.id " +
                    "ORDER BY COUNT(likes.id_user) DESC " +
                    "LIMIT ?;";
            return jdbcTemplate.query(sqlQuery, this::mapRowToFilms, String.valueOf(year), genreId, cnt);

        } else if (genreId == null){
            //запрос популярных фильмов по лайкам конкретного года
            String sqlQuery = "SELECT films.* " +
                    "FROM films " +
                    "LEFT JOIN likes ON likes.id_film = films.id " +
                    "WHERE EXTRACT (YEAR FROM films.release_date ) = ? " +
                    "GROUP BY films.id " +
                    "ORDER BY COUNT(likes.id_user) DESC " +
                    "LIMIT ?;";
            return jdbcTemplate.query(sqlQuery,this::mapRowToFilms, String.valueOf(year), cnt);

        } else {
            //запрос популярных фильмов по лайкам конкретного жанра
            String sqlQuery = "SELECT films.* " +
                    "FROM films " +
                    "LEFT JOIN likes ON likes.id_film = films.id " +
                    "LEFT JOIN film_genres ON film_genres.id_film = films.id " +
                    "WHERE film_genres.id_genre = ? " +
                    "GROUP BY films.id " +
                    "ORDER BY COUNT(likes.id_user) DESC " +
                    "LIMIT ?;";
            return jdbcTemplate.query(sqlQuery,this::mapRowToFilms, genreId, cnt);

        }
    }

    @Override
    public List<Film> findCommon(int userId, int friendsId){
        String sqlQuery = " SELECT films.* " +
                "FROM films " +
                "WHERE films.id IN (SELECT DISTINCT id_film FROM likes WHERE id_user = ? AND ?)";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilms, userId, friendsId);
    }


    @Override
    public List<Film> getSortedFilmByDirector(Integer directorId, String sortBy) {
        String sql = "SELECT * FROM DIRECTORS WHERE ID = ?";
        SqlRowSet directorRows = jdbcTemplate.queryForRowSet(sql, directorId);
        if (!directorRows.next()) {
            log.error("Такого режиссера не существует!");
            throw new ValidationException("Такого режиссера не существует!");
        }
        List<Film> films = new ArrayList<>();
        if (sortBy.equals("likes")) {
            String sqlQuery = "SELECT FILMS.* " +
                    "FROM FILMS " +
                    "LEFT JOIN LIKES ON LIKES.ID_FILM = FILMS.ID " +
                    "LEFT JOIN FILM_DIRECTORS ON FILM_DIRECTORS.ID_FILM = films.ID " +
                    "WHERE ID_DIRECTOR = ? " +
                    "GROUP BY films.id " +
                    "ORDER BY COUNT(likes.id_user) DESC ";
            films = jdbcTemplate.query(sqlQuery, this::mapRowToFilms, directorId);
        } else if (sortBy.equals("year")) {
            String sqlQuery = "SELECT FILMS.* " +
                    "FROM FILMS " +
                    "LEFT JOIN FILM_DIRECTORS ON FILM_DIRECTORS.ID_FILM = films.ID " +
                    "WHERE ID_DIRECTOR = ? " +
                    "ORDER BY FILMS.RELEASE_DATE ";
            films = jdbcTemplate.query(sqlQuery, this::mapRowToFilms, directorId);
        }
        return films;
    }

    private Film mapRowToFilms(ResultSet resultSet, int i) throws SQLException {
        return Film.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getDate("release_date").toLocalDate())
                .duration(resultSet.getInt("duration"))
                .likes(new HashSet<>(getLikesFromUserByFilmId(resultSet.getInt("id"))))
                .rate(resultSet.getInt("rate"))
                .mpa(mpaService.getMpaById(Integer.valueOf(resultSet.getString("mpa"))))
                .genres(genreService.getGenresByIdFilm(resultSet.getInt("id")))
                .directors(directorService.getDirectorsByIdFilm(resultSet.getInt("id")))
                .build();
    }
    private List<Integer> getLikesFromUserByFilmId(int id) {
        String sqlQuery = "SELECT id " +
                "FROM users " +
                "WHERE id IN" +
                "( " +
                "SELECT id_user " +
                "FROM likes " +
                "WHERE id_film IN" +
                "(" +
                "SELECT id_film " +
                "FROM films " +
                "WHERE id_film = ?" +
                ")" +
                ")";

        return jdbcTemplate.queryForList(sqlQuery, Integer.class, id);
    }

    private void checkMpaIsNull(PreparedStatement ps, Film film) throws SQLException {
        if(film.getMpa() != null){
            ps.setInt(6, film.getMpa().getId());
        } else if (film.getMpa() == null)  {
            throw new DataIntegrityViolationException("MPA не может быть null");
        } else if (film.getMpa().getId() < 1 || film.getMpa().getId() > 5) {
            throw new ValidationException("Данного рейтинга еще не существует");
        }
    }
}