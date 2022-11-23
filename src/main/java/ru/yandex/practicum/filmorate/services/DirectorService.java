package ru.yandex.practicum.filmorate.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.EmptyResultFromDataBaseException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.models.Director;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.models.Genre;
import ru.yandex.practicum.filmorate.storage.daoImpl.DaoDirectorStorage;
import ru.yandex.practicum.filmorate.storage.daoImpl.DaoGenreStorage;

import java.util.List;

@Service
@Slf4j
public class DirectorService {

    private final DaoDirectorStorage daoDirectorStorage;

    @Autowired
    public DirectorService(DaoDirectorStorage daoDirectorStorage){
        this.daoDirectorStorage = daoDirectorStorage;
    }

    public List<Director> getAllDirector() {
        return daoDirectorStorage.getAllDirector();
    }

    public Director getDirectorById(Integer id) {
        return daoDirectorStorage.getDirectorById(id);
    }

    public Director addDirector(Director director) {
        return daoDirectorStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        return daoDirectorStorage.updateDirector(director);
    }

    public void removeDirector(Integer id) {
        daoDirectorStorage.removeDirector(id);
    }

    public List<Director> getDirectorsByIdFilm(int id) {
        return daoDirectorStorage.getDirectorsByIdFilm(id);
    }

    public void addOrUpdateFilmDirectors(Film film) {
        daoDirectorStorage.addOrUpdateFilmDirectors(film);
    }
 }