package ru.yandex.practicum.filmorate.storage.daoImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.services.DirectorService;
import ru.yandex.practicum.filmorate.services.GenreService;
import ru.yandex.practicum.filmorate.services.MpaService;
import ru.yandex.practicum.filmorate.storage.interf.FilmStorage;
import ru.yandex.practicum.filmorate.SQLRequests;

import java.sql.*;
import java.sql.Date;
import java.time.Year;
import java.util.*;

@Component
@Slf4j
@Primary
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
        return jdbcTemplate.query(SQLRequests.GET_FILMS
                , new FilmRowMapper(mpaService, genreService, directorService, jdbcTemplate));
    }

    @Override
    public Film getFilmById(Integer filmId) {
        try {
            return jdbcTemplate.queryForObject(SQLRequests.GET_FILMS_BY_ID
                    , new FilmRowMapper(mpaService, genreService, directorService, jdbcTemplate), filmId);
        } catch (Exception e) {
            log.info("Фильм c id {} не содержится в базе ", filmId);
            throw new ValidationException("Фильм c id: " + filmId + " не содержится в базе");
        }
    }

    @Override
    public Film addFilm(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQLRequests.ADD_FILM
                    , Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            
            // Ставить рейтинг фильму, который только внесен в базу неверно, рейтинг должны ставить пользователи
            // ресурса.
            // Если пользователь хочет поставить фильму рейтинг, то он должен послать запрос на добавление рейтинга

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
        if(film.getName().equals(checkNameByIdFromDB(film.getId()))){

            //Тут сложный момент, если поменять название фильма на совершеннно другой фильм, то по-хорошему надо высчитывать
            //rate заново, иначе совершенно другому фильму можно поставить рейтинг, который он не заслуживает.
            //Поэтому менять название фильма лучше запретить. Менять название фильма через поддержку (хотя это несет
            //бО'льшую нагрузку на техподдержку. Пока оставил так, незнаю как правильно). Я бы вообще запретил обновлять
            //фильмы. На кинопоиске вроде нельзя менять фильмы (как и добавлять)

            getFilmById(film.getId());
            jdbcTemplate.update(SQLRequests.UPDATE_FILM
                    , film.getDescription()
                    , film.getReleaseDate()
                    , film.getDuration()
                    , getRateFromTableRate(film.getId())
                    , film.getMpa().getId()
                    , film.getId());

            genreService.addOrUpdateFilmGenres(film);
            directorService.addOrUpdateFilmDirectors(film);
            return getFilmById(film.getId());
        } else {
            throw new ValidationException("Не допускается менять название фильма! Обратитесь в техподдержку :)");
        }

    }
    private String checkNameByIdFromDB(int filmId) {
        return jdbcTemplate.queryForObject(SQLRequests.CHECK_NAME_BY_ID_FROM_DB, String.class, filmId);
    }

    @Override
    public void removeFilm(Integer id) {

        //удаляем жанры в связанной таблице film_genres
        jdbcTemplate.update(SQLRequests.DELETE_GENRE_FROM_FILM_GENRES_TABLE_BY_ID_FILM, id);

        //удаляем режиссеров в связанной таблице film_directors
        jdbcTemplate.update(SQLRequests.DELETE_FROM_FILM_DIRECTORS_TABLE_BY_ID_FILM, id);

        jdbcTemplate.update(SQLRequests.DELETE_FILM, id);
    }

    @Override
    public Film addRateFromUserById(Integer filmId, Integer userId, Integer rate) {
        if (rate < 1 || rate > 10){
            log.info("Неверный параметр rate: {}, rate должен быть от 1 до 10 ", rate);
            throw new IncorrectParameterException("rate");
        } else {
            jdbcTemplate.update(SQLRequests.ADD_RATE_FROM_USER_BY_ID_FILM, userId, filmId, rate);
            updateRateFilm(filmId);

            return getFilmById(filmId);
        }
    }
    @Override
    public Film updateRateFromUserById(Integer filmId, Integer userId, Integer rate) {
        if (rate < 1 || rate > 10){
            log.info("Неверный параметр rate: {}, rate должен быть от 1 до 10 ", rate);
            throw new IncorrectParameterException("rate");
        } else {
            jdbcTemplate.update(SQLRequests.UPDATE_RATE_FROM_USER_BY_ID_FILM, rate, userId, filmId);
            updateRateFilm(filmId);

            return getFilmById(filmId);
        }
    }
    @Override
    public Film removeRateFromUserById(Integer filmId, Integer userId) {
        jdbcTemplate.update(SQLRequests.DELETE_RATE_FROM_USER_BY_ID_FILM, userId, filmId);
        updateRateFilm(filmId);

        return getFilmById(filmId);
    }
    public Double getRateFilmById(Integer filmId) {
        return jdbcTemplate.queryForObject(SQLRequests.GET_RATE_FROM_USER_BY_ID_FILM
                , Double.class, filmId);
    }

    private void updateRateFilm(Integer filmId){
        jdbcTemplate.update(SQLRequests.UPDATE_RATE_FILM
                , getRateFromTableRate(filmId)
                , filmId);
    }
    private Double getRateFromTableRate(Integer filmId) {
            return jdbcTemplate.queryForObject(SQLRequests.GET_RATE_FROM_RATE_TABLE_BY_ID_FILM
                    , Double.class, filmId);
    }

    @Override
    public List<Film> getMostPopularFilmByRate(Integer cnt, Integer genreId, Year year) {
        if (genreId == null && year == null) {
            //запрос популярных фильмов по рейтингу все годов и жанров
            return jdbcTemplate.query(SQLRequests.GET_MOST_POPULAR_FILM_BY_RATE
                    , new FilmRowMapper(mpaService, genreService, directorService, jdbcTemplate), cnt);

        } else if (genreId != null && year != null) {
            //запрос популярных фильмов по рейтингу конкретного года и жанра
            return jdbcTemplate.query(SQLRequests.GET_MOST_POPULAR_FILM_BY_RATE_AND_GENRE_AND_YEAR
                    , new FilmRowMapper(mpaService, genreService, directorService, jdbcTemplate)
                    , String.valueOf(year), genreId, cnt);

        } else if (genreId == null) {
            //запрос популярных фильмов по рейтингу конкретного года
            return jdbcTemplate.query(SQLRequests.GET_MOST_POPULAR_FILM_BY_RATE_AND_YEAR
                    , new FilmRowMapper(mpaService, genreService, directorService, jdbcTemplate)
                    , String.valueOf(year), cnt);

        } else {
            //запрос популярных фильмов по рейтингу конкретного жанра
            return jdbcTemplate.query(SQLRequests.GET_MOST_POPULAR_FILM_BY_RATE_AND_GENRE
                    , new FilmRowMapper(mpaService, genreService, directorService, jdbcTemplate)
                    , genreId, cnt);
        }
    }

    @Override
    public List<Film> findCommon(int userId, int friendsId) {
        return jdbcTemplate.query(SQLRequests.GET_COMMON_FILM
                , new FilmRowMapper(mpaService, genreService, directorService, jdbcTemplate)
                , userId, friendsId);
    }


    @Override
    public List<Film> getSortedFilmByDirector(Integer directorId, String sortBy) {

        SqlRowSet directorRows = jdbcTemplate.queryForRowSet(SQLRequests.GET_DIRECTORS_BY_ID, directorId);

        if (!directorRows.next()) {
            log.error("Такого режиссера не существует!");
            throw new ValidationException("Такого режиссера не существует!");
        }

        List<Film> films = new ArrayList<>();
        if (sortBy.equals("rates")) {
            films = jdbcTemplate.query(SQLRequests.GET_SORTED_FILMS_BY_RATE_BY_ID_DIRECTORS
                    , new FilmRowMapper(mpaService, genreService, directorService, jdbcTemplate), directorId);

        } else if (sortBy.equals("year")) {
            films = jdbcTemplate.query(SQLRequests.GET_SORTED_FILMS_BY_YEAR_BY_ID_DIRECTORS
                    , new FilmRowMapper(mpaService, genreService, directorService, jdbcTemplate), directorId);

        }
        return films;
    }

    private void checkMpaIsNull(PreparedStatement ps, Film film) throws SQLException {
        if (film.getMpa() != null) {
            ps.setInt(5, film.getMpa().getId());
        } else if (film.getMpa() == null) {
            throw new DataIntegrityViolationException("MPA не может быть null");
        } else if (film.getMpa().getId() < 1 || film.getMpa().getId() > 5) {
            throw new ValidationException("Данного рейтинга еще не существует");
        }
    }

    private String getInsertString(String substring, String by) throws IllegalArgumentException {
        substring = substring.toLowerCase(Locale.ROOT);
        switch (by) {
            case "director":
                return "(LOWER(d.name) LIKE '%" + substring + "%')";
            case "title":
                return "(LOWER(f.name) LIKE '%" + substring + "%')";
            case "director,title":
            case "title,director":
                return "(LOWER(d.name) LIKE '%" + substring + "%') OR (LOWER(f.name) LIKE '%" + substring + "%')";
            default:
                throw new IllegalArgumentException("Wrong request param.");
        }
    }

    @Override
    public List<Film> searchFilms(String substring, String by) throws IllegalArgumentException {
        String sql = "SELECT *" +
                "FROM films AS f " +
                "LEFT OUTER JOIN film_directors AS fd ON f.id = fd.id_film " +
                "LEFT OUTER JOIN directors AS d ON fd.id_director = d.id " +
                "WHERE " + getInsertString(substring, by) + " " +
                "GROUP BY f.rate " +
                "ORDER BY f.rate DESC;";
        Set<Film> films = new HashSet<>(jdbcTemplate.query(sql, new FilmRowMapper(mpaService, genreService, directorService, jdbcTemplate)));
        List<Film> result = new ArrayList<>(films);
        result.sort(Comparator.comparingDouble(Film::getRate));
        Collections.reverse(result);
        return result;
    }
}