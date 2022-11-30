package ru.yandex.practicum.filmorate.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.models.Film;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.models.User;
import ru.yandex.practicum.filmorate.storage.interf.EventStorage;
import ru.yandex.practicum.filmorate.storage.interf.FilmStorage;

import java.time.Year;
import java.util.*;


@Service
@Slf4j
@Qualifier("daoFilmStorage")
@RequiredArgsConstructor
public class FilmService {
    private final UserService userService;
    private final FilmStorage filmStorage;

    private final EventStorage eventStorage;

    public List<Film> getFilms(){
        return filmStorage.getFilms();
    }
    public Film getFilmById(Integer filmId) {
        return filmStorage.getFilmById(filmId);
    }

    public Film addFilm(Film film){
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film){
        return filmStorage.updateFilm(film);
    }
    public void removeFilm(Integer id){
        filmStorage.removeFilm(id);
    }
    public Film addRateFromUserById(Integer filmId, Integer userId, Integer rate){
        Film film = filmStorage.getFilmById(filmId);
        User user = userService.getUserById(userId);

        Map<String, Object> params = eventStorage.makeEvent(
                (long)userId,
                filmId,
                "rate",
                "add"
        );
        eventStorage.save(params);

        return filmStorage.addRateFromUserById(film.getId(), user.getId(), rate);
    }
    public Film updateRateFromUserById(Integer filmId, Integer userId, Integer rate){
        Film film = filmStorage.getFilmById(filmId);
        User user = userService.getUserById(userId);

        Map<String, Object> params = eventStorage.makeEvent(
                (long)userId,
                filmId,
                "rate",
                "update"
        );
        eventStorage.save(params);

        return filmStorage.updateRateFromUserById(film.getId(), user.getId(), rate);
    }
    public Film removeRateFromUserById(Integer filmId, Integer userId){
        Film film = filmStorage.getFilmById(filmId);
        User user = userService.getUserById(userId);

        Map<String, Object> params = eventStorage.makeEvent(
                (long)userId,
                filmId,
                "rate",
                "remove"
        );
        eventStorage.save(params);

        return filmStorage.removeRateFromUserById(film.getId(), user.getId());
    }
    public Double getRateFilmById(Integer filmId) {
        return filmStorage.getRateFilmById(filmId);
    }
    public List<Film> getMostPopularFilmByCountLikes(Integer count, Integer genreId, Year year){
        return filmStorage.getMostPopularFilmByRate(count, genreId, year);
    }
    public List<Film> getSortedFilmByDirector(Integer directorId, String sortBy) {
        return filmStorage.getSortedFilmByDirector(directorId, sortBy);
    }

    public List<Film> findCommon (int userId, int friendId){
        return filmStorage.findCommon(userId, friendId);
    }
    public List<Film> searchFilm(String substring, String by) throws IllegalArgumentException {
        return filmStorage.searchFilms(substring, by);
    }
    public UserService getUserService() {
        return userService;
    }

}