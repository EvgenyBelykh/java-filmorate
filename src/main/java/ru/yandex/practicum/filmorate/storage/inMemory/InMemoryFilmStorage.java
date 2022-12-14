package ru.yandex.practicum.filmorate.storage.inMemory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ValidationConditionException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.exceptions.ValidationFilmByIdException;
import ru.yandex.practicum.filmorate.models.Director;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.storage.interf.FilmStorage;

import java.time.LocalDate;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Calendar.DECEMBER;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Integer, Film> films = new HashMap<>();
    private Integer id = 1;
    public static final LocalDate DATE_FIRST_FILM = LocalDate.of(1895, DECEMBER, 28);

    @Override
    public List<Film> getFilms() {
        log.info("Вернули список фильмов {}", films);
        return new ArrayList<>(films.values());
    }

    @Override
    public Film addFilm(Film film) {
        CheckOnTheFirstFilm(film);

        if (films.containsKey(film.getId())) {
            throw new ValidationFilmByIdException("Фильм - " + film.getName() + " c id - " + film.getId() + " уже есть в базе");
        }

        film.setId(getNextId());
        films.put(film.getId(), film);

        log.info("В библиотеку добавлен фильм {}", film);
        return film;
    }

    @Override
    public void removeFilm(Integer id) {
        Film film = films.get(id);
        validFilmById(film.getId());
        films.remove(film.getId());

        log.info("Из библиотеки удален фильм {}", film);
    }

    @Override
    public Film updateFilm(Film film) {
        CheckOnTheFirstFilm(film);

        validFilmById(film.getId());
        films.put(film.getId(), film);

        log.info("В библиотеке обновлен фильм {}", film);
        return film;
    }

    @Override
    public Film getFilmById(Integer filmId) {
        validFilmById(filmId);
        log.info("Вернули фильм {}", films);
        return films.get(filmId);
    }

    @Override
    public Film addLikeFromUserById(Integer filmId, Integer userId) {
        Film film = films.get(filmId);
        if(film.getLikes() == null){
            film.setLikes(new HashSet<>());
        }
        film.getLikes().add(userId);
        films.put(filmId, film);

        log.info("Пользователь с id: {} поставил like фильму: {}", userId, filmId);
        return film;
    }

    @Override
    public Film removeLikeFromUserById(Integer filmId, Integer userId) {
        Film film = films.get(filmId);
        film.getLikes().remove(userId);

        log.info("Пользователь с id: {} убрал like фильму: {}", userId, filmId);
        return film;
    }

    @Override
    public List<Film> getMostPopularFilmByCountLikes(Integer count, Integer genreId, Year year) {
        return getFilms().stream()
                .sorted(this::compare)
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public List<Film> getSortedFilmByDirector(Integer id, String sortBy) {
        return null;
    }


    @Override
    public List<Film> findCommon (int userId, int friendId){
        return null;
    }

    private int compare(Film p0, Film p1) {
        return p1.getLikes().size() - p0.getLikes().size();
    }

    private void CheckOnTheFirstFilm(Film film) {
        if (film.getReleaseDate().isBefore(DATE_FIRST_FILM)) {
            throw new ValidationConditionException("Фильм не может быть выпущен ранее: " +
                    DATE_FIRST_FILM);
        }
    }
    private void validFilmById(Integer filmId) {
        if (!films.containsKey(filmId)) {
            throw new ValidationException("Фильм  c id - " + filmId + " не содержится в базе");
        }
    }
    private Integer getNextId() {
        return id++;
    }

    private List<Film> getFilmsByDirectors(Collection<Film> films, String substring) {
        List<Film> result = new ArrayList<>();

        for (Film film : films) {
            for (Director director : film.getDirectors()) {
                if (director.getName().contains(substring)) {
                    result.add(film);
                    break;
                }
            }
        }

        return result;
    }

    private List<Film> getFilmsByName(Collection<Film> films, String substring) {
        List<Film> result = new ArrayList<>();

        for (Film film : films) {
            if (film.getName().contains(substring)) {
                result.add(film);
            }
        }

        return result;
    }

    @Override
    public List<Film> searchFilms(String substring, String by) {
        Set<Film> result = new HashSet<>();

        switch (by) {
            case "director":
                result.addAll(getFilmsByDirectors(films.values(), substring));
                break;
            case "title":
                result.addAll(getFilmsByName(films.values(), substring));
                break;
            case "director,title":
            case "title,director":
                result.addAll(getFilmsByDirectors(films.values(), substring));
                result.addAll(getFilmsByName(films.values(), substring));
                break;
            default:
                throw new IllegalArgumentException("Wrong request param.");
        }

        List<Film> toReturn = new ArrayList<>(result);
        toReturn.sort(Comparator.comparingInt(film -> film.getLikes().size()));
        return toReturn;
    }
}
