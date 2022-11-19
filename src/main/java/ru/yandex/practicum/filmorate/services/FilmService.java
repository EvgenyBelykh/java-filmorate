package ru.yandex.practicum.filmorate.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.models.Film;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.models.User;
import ru.yandex.practicum.filmorate.storage.interf.FilmStorage;

import java.util.*;


@Service
@Slf4j
public class FilmService {
    private final UserService userService;
    private final FilmStorage filmStorage;


    @Autowired
    public FilmService(UserService userService, @Qualifier("daoFilmStorage") FilmStorage filmStorage){
        this.userService = userService;
        this.filmStorage = filmStorage;
    }

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
    public void removeFilm(Film film){
        filmStorage.removeFilm(film);
    }
    public Film addLikeFromUserById(Integer filmId, Integer userId){
        Film film = filmStorage.getFilmById(filmId);
        User user = userService.getUserById(userId);

        return filmStorage.addLikeFromUserById(film.getId(), user.getId());
    }
    public Film removeLikeFromUserById(Integer filmId, Integer userId){
        Film film = filmStorage.getFilmById(filmId);
        User user = userService.getUserById(userId);

        return filmStorage.removeLikeFromUserById(film.getId(), user.getId());
    }
    public List<Film> getMostPopularFilmByCountLikes(Integer count){
        return filmStorage.getMostPopularFilmByCountLikes(count);
    }
    public UserService getUserService() {
        return userService;
    }

    public List<Film> findCommon (int userId, int friendId){
        List<Film> common = filmStorage.findCommon(userId, friendId);
        common.sort((o1, o2) -> o2.getLikes().size() - o1.getLikes().size());

                return common;
    }

}