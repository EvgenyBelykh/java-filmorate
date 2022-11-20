package ru.yandex.practicum.filmorate.controllers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable("id") Integer filmId) {
        return filmService.getFilmById(filmId);
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @DeleteMapping
    public void removeFilm(@RequestBody Film film) {
        filmService.removeFilm(film);
    }


    @PutMapping("/{id}/like/{userId}")
    public Film addLikeFromUserById(@PathVariable("id") Integer filmId, @PathVariable("userId") Integer userId) {
        return filmService.addLikeFromUserById(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film removeLikeFromUserById(@PathVariable("id") Integer filmId, @PathVariable("userId") Integer userId) {
        return filmService.removeLikeFromUserById(filmId, userId);
    }
    @GetMapping("/popular")
    public List<Film> getMostPopularFilmByCountLikes(
            @RequestParam(value = "count", defaultValue = "10", required = false) Integer count,
            @RequestParam(value = "year", required = false) Year year,
            @RequestParam(value = "genreId", required = false) Integer genreId) {

        if (count < 0) {
            throw new IncorrectParameterException("count");
        }

        if (genreId != null && (genreId < 0 || genreId > 6)){
            throw new IncorrectParameterException("genreId");
        }

        if (year != null && (year.isBefore(Year.of(1985)) || year.isAfter(Year.now()))) {
            throw new IncorrectParameterException("year");
        }

        return filmService.getMostPopularFilmByCountLikes(count, genreId, year);
    }
}
