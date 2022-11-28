package ru.yandex.practicum.filmorate.controllers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.IncorrectParameterException;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.services.FilmService;

import javax.validation.Valid;
import java.time.*;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/films")
public class FilmController {
    @Getter
    FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> getFilms() {
        log.info("Запрос списка фильмов из базы");
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable("id") Integer filmId) {
        log.info("Запрос фильма с id: {} из базы", filmId);
        return filmService.getFilmById(filmId);
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Запрос добавления фильма в базу c названием: {}", film.getName());
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Запрос обновления фильма c id: {} в базе", film.getId());
        return filmService.updateFilm(film);
    }

    @DeleteMapping("/{id}")
    public void removeFilm(@PathVariable("id") Integer id) {
        log.info("Запрос удаления фильма c id: {} из базы", id);
        filmService.removeFilm(id);
    }


    @PostMapping("/{id}/rate/{userId}")
    public Film addRateFromUserById(@PathVariable("id") Integer filmId
            , @PathVariable("userId") Integer userId
            , @RequestParam(value = "rate") Integer rate) {
        log.info("Запрос добавления рейтинга: {} фильму c id: {} от пользователя с id: {}", rate, filmId, userId);
        return filmService.addRateFromUserById(filmId, userId, rate);
    }
    @PutMapping("/{id}/rate/{userId}")
    public Film updateRateFromUserById(@PathVariable("id") Integer filmId
            , @PathVariable("userId") Integer userId
            , @RequestParam(value = "rate") Integer rate) {
        log.info("Запрос обновления рейтинга: {} фильму c id: {} от пользователя с id: {}", rate, filmId, userId);
        return filmService.updateRateFromUserById(filmId, userId, rate);
    }

    @DeleteMapping("/{id}/rate/{userId}")
    public Film removeRateFromUserById(@PathVariable("id") Integer filmId
            , @PathVariable("userId") Integer userId) {
        log.info("Запрос удаления рейтинга фильму c id: {} от пользователя с id: {}", filmId, userId);

        return filmService.removeRateFromUserById(filmId, userId);
    }
    @GetMapping("/{id}/rate")
    public Double getRateFilmById(@PathVariable("id") Integer filmId){
        log.info("Запрос рейтинга фильма с id: {} ", filmId);
        return filmService.getRateFilmById(filmId);
    }
    @GetMapping("/popular")
    public List<Film> getMostPopularFilmByCountLikes(
            @RequestParam(value = "count", defaultValue = "10", required = false) Integer count,
            @RequestParam(value = "year", required = false) Year year,
            @RequestParam(value = "genreId", required = false) Integer genreId) {

        log.info("Запрос получения списка фильмов размером count: {}, отсортированных по жанру с id: {} и/или" +
                "году: {} ", count, genreId, year);

        if (count < 0) {
            log.info("Неверный параметр count: {}, count должен быть больше 0 ", count);
            throw new IncorrectParameterException("count");
        }

        if (genreId != null && (genreId < 0 || genreId > 6)){
            log.info("Неверный параметр genreId: {}, genreId должен быть в диапозоне от 1 до 6 ", genreId);
            throw new IncorrectParameterException("genreId");
        }

        if (year != null && (year.isBefore(Year.of(1895)) || year.isAfter(Year.now()))) {
            log.info("Неверный параметр year: {}, year должен быть больше 1895 ", count);
            throw new IncorrectParameterException("year");
        }

        return filmService.getMostPopularFilmByCountLikes(count, genreId, year);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getSortedFilmByDirector(
            @PathVariable("directorId") Integer directorId,
            @RequestParam(value = "sortBy") String sortBy) {
        log.info("Запрос отсортированного списка фильмов режиссера с id: {} по году или лайкам: {}", directorId,
                sortBy);
        return filmService.getSortedFilmByDirector(directorId, sortBy);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam(value = "userId") int userId, @RequestParam(value = "friendId")
        int friendId){
        log.info("Запрос списка общих фильмов пользователей с id: {} и {} ", userId, friendId);
        return filmService.findCommon(userId, friendId);
    }

    @GetMapping("/search")
    public List<Film> searchFilms(@RequestParam(name = "query") String query,
                                  @RequestParam(name = "by") String by)
                                  throws IllegalArgumentException {
        log.info("Запрос поиска фильмов по тексту: {} и параметрам: {}", query, by);
        return filmService.searchFilm(query, by);
    }
}
