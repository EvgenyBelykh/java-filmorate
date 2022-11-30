package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exceptions.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.models.*;
import ru.yandex.practicum.filmorate.storage.daoImpl.DaoDirectorStorage;
import ru.yandex.practicum.filmorate.storage.daoImpl.DaoFilmStorage;
import ru.yandex.practicum.filmorate.storage.daoImpl.DaoUserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmControllerWithDaoTest {
    private final JdbcTemplate jdbcTemplate;
    private final DaoFilmStorage filmStorage;
    private final DaoUserStorage userStorage;
    private final DaoDirectorStorage directorStorage;
    Film firstFilm;
    Film secondFilm;
    Film thirdFilm;
    private static final List<Mpa> listMpa = new ArrayList<>();
    private static final List<Genre> listGenre = new ArrayList<>();

    static {
        Mpa mpa1 = Mpa.builder()
                .id(1)
                .name("G")
                .build();
        Mpa mpa2 = Mpa.builder()
                .id(2)
                .name("PG")
                .build();
        Mpa mpa3 = Mpa.builder()
                .id(3)
                .name("PG-13")
                .build();
        Mpa mpa4 = Mpa.builder()
                .id(4)
                .name("R")
                .build();
        Mpa mpa5 = Mpa.builder()
                .id(5)
                .name("NC-17")
                .build();
        listMpa.add(mpa1);
        listMpa.add(mpa2);
        listMpa.add(mpa3);
        listMpa.add(mpa4);
        listMpa.add(mpa5);

        Genre genre1 = Genre.builder()
                .id(1)
                .name("Комедия")
                .build();
        Genre genre2 = Genre.builder()
                .id(2)
                .name("Драма")
                .build();
        Genre genre3 = Genre.builder()
                .id(3)
                .name("Мультфильм")
                .build();
        Genre genre4 = Genre.builder()
                .id(4)
                .name("Триллер")
                .build();
        Genre genre5 = Genre.builder()
                .id(5)
                .name("Документальный")
                .build();
        Genre genre6 = Genre.builder()
                .id(6)
                .name("Боевик")
                .build();
        listGenre.add(genre1);
        listGenre.add(genre2);
        listGenre.add(genre3);
        listGenre.add(genre4);
        listGenre.add(genre5);
        listGenre.add(genre6);
    }


    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM USERS");
        jdbcTemplate.update("DELETE FROM FILMS");
        jdbcTemplate.update("DELETE FROM RATE");
        jdbcTemplate.update("DELETE FROM DIRECTORS");
        jdbcTemplate.update("DELETE FROM USERS_FRIENDS");
        jdbcTemplate.update("DELETE FROM FILM_GENRES");
        jdbcTemplate.update("ALTER TABLE USERS ALTER COLUMN ID RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE FILMS ALTER COLUMN ID RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE DIRECTORS ALTER COLUMN ID RESTART WITH 1");
    }

    @Test
    public void addFilmTest() {
        firstFilm = Film.builder()
                .description("Описание")
                .releaseDate(LocalDate.of(1994, 12, 14))
                .duration(101)
                .name("Маска")
                .mpa(listMpa.get(1))
                .build();
        filmStorage.addFilm(firstFilm);

        assertEquals("Маска", filmStorage.getFilmById(1).getName());
    }

    @Test
    public void updateFilmTest() {
        firstFilm = Film.builder()
                .description("Фильм о двух тупых чувачков") //неверное описание
                .releaseDate(LocalDate.of(1994, 12, 6)) //неверная дата выхода
                .duration(107) //неверная длительность фильма
                .name("Маска")
                .mpa(listMpa.get(1))
                .build();

        secondFilm = Film.builder()
                .id(1)
                .description("Описание")
                .releaseDate(LocalDate.of(1994, 12, 6))
                .duration(107)
                .name("Тупой и еще тупее")
                .mpa(listMpa.get(1))
                .build();

        thirdFilm = Film.builder()
                .id(1)
                .description("Про маску")
                .releaseDate(LocalDate.of(1994, 12, 14))
                .duration(101)
                .name("Маска")
                .mpa(listMpa.get(1))
                .build();

        filmStorage.addFilm(firstFilm);

        assertThrows(ValidationException.class, () -> filmStorage.updateFilm(secondFilm));

        filmStorage.updateFilm(thirdFilm);
        assertEquals(thirdFilm.getName(), filmStorage.getFilms().get(0).getName());
        assertEquals(thirdFilm.getDescription(), filmStorage.getFilms().get(0).getDescription());
        assertEquals(thirdFilm.getReleaseDate(), filmStorage.getFilms().get(0).getReleaseDate());
        assertEquals(thirdFilm.getDuration(), filmStorage.getFilms().get(0).getDuration());
    }

    @Test
    public void addFilmWithWrongDateTest() {
        firstFilm = Film.builder()
                .description("Описание")
                .releaseDate(LocalDate.of(1825, 12, 14))
                .duration(101)
                .name("Маска")
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> filmStorage.addFilm(firstFilm));
    }

    @Test
    public void addFilmWithNullMpa() {
        firstFilm = Film.builder()
                .description("Описание")
                .releaseDate(LocalDate.of(1825, 12, 14))
                .duration(101)
                .name("Маска")
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> filmStorage.addFilm(firstFilm));
    }

    @Test
    public void removeFilmTest() {
        firstFilm = Film.builder()
                .description("Описание")
                .releaseDate(LocalDate.of(1994, 12, 14))
                .duration(101)
                .name("Маска")
                .mpa(listMpa.get(1))
                .build();
        filmStorage.addFilm(firstFilm);
        firstFilm.setId(1);
        filmStorage.removeFilm(1);
        assertEquals(new ArrayList<>(0), filmStorage.getFilms());
    }

    @Test
    public void getFilmTest() {
        firstFilm = Film.builder()
                .description("Описание")
                .releaseDate(LocalDate.of(1994, 12, 14))
                .duration(101)
                .name("Маска")
                .mpa(listMpa.get(1))
                .build();
        filmStorage.addFilm(firstFilm);

        secondFilm = Film.builder()
                .description("Описание")
                .releaseDate(LocalDate.of(1994, 12, 6))
                .duration(107)
                .name("Тупой и еще тупее")
                .mpa(listMpa.get(1))
                .build();
        filmStorage.addFilm(secondFilm);
        assertEquals(2, filmStorage.getFilms().size());
    }

    @Test
    public void getFilmByIdTest() {
        firstFilm = Film.builder()
                .description("Описание")
                .releaseDate(LocalDate.of(1994, 12, 14))
                .duration(101)
                .name("Маска")
                .mpa(listMpa.get(1))
                .build();
        filmStorage.addFilm(firstFilm);

        secondFilm = Film.builder()
                .description("Описание")
                .releaseDate(LocalDate.of(1994, 12, 6))
                .duration(107)
                .name("Тупой и еще тупее")
                .mpa(listMpa.get(1))
                .build();
        filmStorage.addFilm(secondFilm);

        assertEquals(secondFilm.getName(), filmStorage.getFilmById(2).getName());

        Throwable exception = assertThrows(
                ValidationException.class,
                () -> {
                    filmStorage.getFilmById(3);
                }
        );
        assertEquals("Фильм c id: 3 не содержится в базе"
                , exception.getMessage());
    }
    @Test
    public void addWrongRateFromUserByIdTest() {
        firstFilm = Film.builder()
                .description("Описание")
                .releaseDate(LocalDate.of(1994, 12, 14))
                .duration(101)
                .name("Маска")
                .mpa(listMpa.get(1))
                .build();
        filmStorage.addFilm(firstFilm);

        User firstUser = User.builder()
                .email("jim@email.com")
                .login("Jim")
                .name("Джим")
                .birthday(LocalDate.of(1962, 1, 17))
                .build();
        userStorage.addUser(firstUser);

        assertThrows(IncorrectParameterException.class, () -> filmStorage.addRateFromUserById(1,1,11));
        assertThrows(IncorrectParameterException.class, () -> filmStorage.addRateFromUserById(1,1,0));
    }
    @Test
    public void updateWrongRateFromUserByIdTest() {
        firstFilm = Film.builder()
                .description("Описание")
                .releaseDate(LocalDate.of(1994, 12, 14))
                .duration(101)
                .name("Маска")
                .mpa(listMpa.get(1))
                .build();
        filmStorage.addFilm(firstFilm);

        User firstUser = User.builder()
                .email("jim@email.com")
                .login("Jim")
                .name("Джим")
                .birthday(LocalDate.of(1962, 1, 17))
                .build();
        userStorage.addUser(firstUser);

        filmStorage.addRateFromUserById(1,1,5);

        assertThrows(IncorrectParameterException.class, () -> filmStorage.updateRateFromUserById(1,1,11));
        assertThrows(IncorrectParameterException.class, () -> filmStorage.updateRateFromUserById(1,1,0));
    }

    @Test
    public void addAndUpdateAndRemoveRateFromUserByIdTest() {
        firstFilm = Film.builder()
                .description("Описание")
                .releaseDate(LocalDate.of(1994, 12, 14))
                .duration(101)
                .name("Маска")
                .mpa(listMpa.get(1))
                .build();
        filmStorage.addFilm(firstFilm);

        User firstUser = User.builder()
                .email("jim@email.com")
                .login("Jim")
                .name("Джим")
                .birthday(LocalDate.of(1962, 1, 17))
                .build();
        userStorage.addUser(firstUser);

        User secondUser = User.builder()
                .email("jeff@email.com")
                .login("Jeff")
                .name("Джефф")
                .birthday(LocalDate.of(1955, 2, 19))
                .build();
        userStorage.addUser(secondUser);

        filmStorage.addRateFromUserById(1, 1, 10);
        assertEquals(10, filmStorage.getRateFilmById(1)
                , "Неверный рейтинг");

        filmStorage.addRateFromUserById(1, 2, 5);
        assertEquals(7.5, filmStorage.getRateFilmById(1)
                , "Неверный рейтинг");

        filmStorage.updateRateFromUserById(1, 2, 10);
        assertEquals(10, filmStorage.getRateFilmById(1)
                , "Неверный рейтинг");

        filmStorage.removeRateFromUserById(1, 1);
        assertEquals(10, filmStorage.getRateFilmById(1)
                , "Неверный рейтинг");

        filmStorage.removeRateFromUserById(1, 2);
        assertNull(filmStorage.getRateFilmById(1), "Неверный рейтинг");
    }

//    @Test
//    public void getMostPopularFilmByCountLikes() {
//        User firstUser = User.builder()
//                .email("jim@email.com")
//                .login("Jim")
//                .name("Джим")
//                .birthday(LocalDate.of(1962, 1, 17))
//                .build();
//
//        User secondUser = User.builder()
//                .email("jeff@email.com")
//                .login("Jeff")
//                .name("Джефф")
//                .birthday(LocalDate.of(1955, 2, 19))
//                .build();
//
//        User thirdUser = User.builder()
//                .email("Diaz@email.com")
//                .login("Cameron")
//                .name("Кэмерон")
//                .birthday(LocalDate.of(1972, 8, 30))
//                .build();
//
//        firstFilm = Film.builder()
//                .id(1)
//                .description("Описание")
//                .releaseDate(LocalDate.of(1994, 1, 28))
//                .duration(101)
//                .name("Маска")
//                .mpa(listMpa.get(1))
//                .build();
//
//        secondFilm = Film.builder()
//                .description("Описание")
//                .releaseDate(LocalDate.of(1994, 12, 6))
//                .duration(107)
//                .name("Тупой и еще тупее")
//                .mpa(listMpa.get(1))
//                .build();
//
//        thirdFilm = Film.builder()
//                .description("Описание")
//                .releaseDate(LocalDate.of(2004, 3, 9))
//                .duration(108)
//                .name("Вечное сияние чистого разума")
//                .mpa(listMpa.get(1))
//                .build();
//
//        filmStorage.addFilm(firstFilm);
//        filmStorage.addFilm(secondFilm);
//        filmStorage.addFilm(thirdFilm);
//
//        userStorage.addUser(firstUser);
//        userStorage.addUser(secondUser);
//        userStorage.addUser(thirdUser);
//
//        filmStorage.addOrUpdateRateFromUserById(2, 1);
//        filmStorage.addOrUpdateRateFromUserById(2, 2);
//        filmStorage.addOrUpdateRateFromUserById(2, 3);
//
//        filmStorage.addOrUpdateRateFromUserById(3, 1);
//        filmStorage.addOrUpdateRateFromUserById(3, 2);
//
//        filmStorage.addOrUpdateRateFromUserById(1, 1);
//
//        assertEquals(filmStorage.getMostPopularFilmByCountLikes(1, null, null).size(), 1);
//        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, null, null).size(), 3);
//        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, null, null).size(), 3);
//    }
//    @Test
//    public void getMostPopularFilmByGenreTest() {
//        User firstUser = User.builder()
//                .email("jim@email.com")
//                .login("Jim")
//                .name("Джим")
//                .birthday(LocalDate.of(1962, 1, 17))
//                .build();
//
//        User secondUser = User.builder()
//                .email("jeff@email.com")
//                .login("Jeff")
//                .name("Джефф")
//                .birthday(LocalDate.of(1955, 2, 19))
//                .build();
//
//        User thirdUser = User.builder()
//                .email("Diaz@email.com")
//                .login("Cameron")
//                .name("Кэмерон")
//                .birthday(LocalDate.of(1972, 8, 30))
//                .build();
//
//        firstFilm = Film.builder()
//                .id(1)
//                .description("Описание")
//                .releaseDate(LocalDate.of(1994, 1, 28))
//                .duration(101)
//                .name("Маска")
//                .mpa(listMpa.get(1))
//                .genres(Collections.singletonList(listGenre.get(0)))
//                .build();
//
//        secondFilm = Film.builder()
//                .description("Описание")
//                .releaseDate(LocalDate.of(1994, 12, 6))
//                .duration(107)
//                .name("Тупой и еще тупее")
//                .mpa(listMpa.get(1))
//                .genres(Collections.singletonList(listGenre.get(0)))
//                .build();
//
//        thirdFilm = Film.builder()
//                .description("Описание")
//                .releaseDate(LocalDate.of(2004, 3, 9))
//                .duration(108)
//                .name("Вечное сияние чистого разума")
//                .mpa(listMpa.get(1))
//                .genres(Collections.singletonList(listGenre.get(1)))
//                .build();
//
//        filmStorage.addFilm(firstFilm);
//        filmStorage.addFilm(secondFilm);
//        filmStorage.addFilm(thirdFilm);
//
//        userStorage.addUser(firstUser);
//        userStorage.addUser(secondUser);
//        userStorage.addUser(thirdUser);
//
//        filmStorage.addOrUpdateRateFromUserById(2, 1);
//        filmStorage.addOrUpdateRateFromUserById(2, 2);
//        filmStorage.addOrUpdateRateFromUserById(2, 3);
//
//        filmStorage.addOrUpdateRateFromUserById(3, 1);
//        filmStorage.addOrUpdateRateFromUserById(3, 2);
//
//        filmStorage.addOrUpdateRateFromUserById(1, 1);
//
//        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, 1, null).size(), 2
//                , "Вернулся неверный список фильмов");
//        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, 2, null).size(), 1
//                , "Вернулся неверный список фильмов");
//        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, 3, null).size(), 0
//                , "Вернулся неверный список фильмов");
//    }
//    @Test
//    public void getMostPopularFilmByYearTest() {
//        User firstUser = User.builder()
//                .email("jim@email.com")
//                .login("Jim")
//                .name("Джим")
//                .birthday(LocalDate.of(1962, 1, 17))
//                .build();
//
//        User secondUser = User.builder()
//                .email("jeff@email.com")
//                .login("Jeff")
//                .name("Джефф")
//                .birthday(LocalDate.of(1955, 2, 19))
//                .build();
//
//        User thirdUser = User.builder()
//                .email("Diaz@email.com")
//                .login("Cameron")
//                .name("Кэмерон")
//                .birthday(LocalDate.of(1972, 8, 30))
//                .build();
//
//        firstFilm = Film.builder()
//                .id(1)
//                .description("Описание")
//                .releaseDate(LocalDate.of(1994, 1, 28))
//                .duration(101)
//                .name("Маска")
//                .mpa(listMpa.get(1))
//                .genres(Collections.singletonList(listGenre.get(0)))
//                .build();
//
//        secondFilm = Film.builder()
//                .description("Описание")
//                .releaseDate(LocalDate.of(1994, 12, 6))
//                .duration(107)
//                .name("Тупой и еще тупее")
//                .mpa(listMpa.get(1))
//                .genres(Collections.singletonList(listGenre.get(0)))
//                .build();
//
//        thirdFilm = Film.builder()
//                .description("Описание")
//                .releaseDate(LocalDate.of(2004, 3, 9))
//                .duration(108)
//                .name("Вечное сияние чистого разума")
//                .mpa(listMpa.get(1))
//                .genres(Collections.singletonList(listGenre.get(1)))
//                .build();
//
//        filmStorage.addFilm(firstFilm);
//        filmStorage.addFilm(secondFilm);
//        filmStorage.addFilm(thirdFilm);
//
//        userStorage.addUser(firstUser);
//        userStorage.addUser(secondUser);
//        userStorage.addUser(thirdUser);
//
//        filmStorage.addOrUpdateRateFromUserById(2, 1);
//        filmStorage.addOrUpdateRateFromUserById(2, 2);
//        filmStorage.addOrUpdateRateFromUserById(2, 3);
//
//        filmStorage.addOrUpdateRateFromUserById(3, 1);
//        filmStorage.addOrUpdateRateFromUserById(3, 2);
//
//        filmStorage.addOrUpdateRateFromUserById(1, 1);
//
//        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, null, Year.of(1994)).size(), 2
//                , "Вернулся неверный список фильмов");
//        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, null, Year.of(2004)).size(), 1
//                , "Вернулся неверный список фильмов");
//        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, null, Year.of(2022)).size(), 0
//                , "Вернулся неверный список фильмов");
//    }
//    @Test
//    public void getMostPopularFilmByGenreAndYearTest() {
//        User firstUser = User.builder()
//                .email("jim@email.com")
//                .login("Jim")
//                .name("Джим")
//                .birthday(LocalDate.of(1962, 1, 17))
//                .build();
//
//        User secondUser = User.builder()
//                .email("jeff@email.com")
//                .login("Jeff")
//                .name("Джефф")
//                .birthday(LocalDate.of(1955, 2, 19))
//                .build();
//
//        User thirdUser = User.builder()
//                .email("Diaz@email.com")
//                .login("Cameron")
//                .name("Кэмерон")
//                .birthday(LocalDate.of(1972, 8, 30))
//                .build();
//
//        firstFilm = Film.builder()
//                .id(1)
//                .description("Описание")
//                .releaseDate(LocalDate.of(1994, 1, 28))
//                .duration(101)
//                .name("Маска")
//                .mpa(listMpa.get(1))
//                .genres(Collections.singletonList(listGenre.get(0)))
//                .build();
//
//        secondFilm = Film.builder()
//                .description("Описание")
//                .releaseDate(LocalDate.of(1994, 12, 6))
//                .duration(107)
//                .name("Тупой и еще тупее")
//                .mpa(listMpa.get(1))
//                .genres(Collections.singletonList(listGenre.get(0)))
//                .build();
//
//        thirdFilm = Film.builder()
//                .description("Описание")
//                .releaseDate(LocalDate.of(2004, 3, 9))
//                .duration(108)
//                .name("Вечное сияние чистого разума")
//                .mpa(listMpa.get(1))
//                .genres(Collections.singletonList(listGenre.get(1)))
//                .build();
//
//        filmStorage.addFilm(firstFilm);
//        filmStorage.addFilm(secondFilm);
//        filmStorage.addFilm(thirdFilm);
//
//        userStorage.addUser(firstUser);
//        userStorage.addUser(secondUser);
//        userStorage.addUser(thirdUser);
//
//        filmStorage.addOrUpdateRateFromUserById(2, 1);
//        filmStorage.addOrUpdateRateFromUserById(2, 2);
//        filmStorage.addOrUpdateRateFromUserById(2, 3);
//
//        filmStorage.addOrUpdateRateFromUserById(3, 1);
//        filmStorage.addOrUpdateRateFromUserById(3, 2);
//
//        filmStorage.addOrUpdateRateFromUserById(1, 1);
//
//        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, 1, Year.of(1994)).size(), 2
//                , "Вернулся неверный список фильмов");
//        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, 2, Year.of(2004)).size(), 1
//                , "Вернулся неверный список фильмов");
//        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, 1, Year.of(2022)).size(), 0
//                , "Вернулся неверный список фильмов");
//        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, 2, Year.of(1994)).size(), 0
//                , "Вернулся неверный список фильмов");
//    }
//
//   @Test
//    public void getSortedFilmByDirectorTest() {
//        Director director = Director.builder()
//                .id(1)
//                .name("ДжеймсКамерун")
//                .build();
//        List<Director> listDirector = new ArrayList<>();
//        listDirector.add(director);
//        directorStorage.addDirector(director);
//
//        User firstUser = User.builder()
//                .email("jim@email.com")
//                .login("Jim")
//                .name("Джим")
//                .birthday(LocalDate.of(1962, 1, 17))
//                .build();
//
//        User secondUser = User.builder()
//                .email("jeff@email.com")
//                .login("Jeff")
//                .name("Джефф")
//                .birthday(LocalDate.of(1955, 2, 19))
//                .build();
//
//        User thirdUser = User.builder()
//                .email("Diaz@email.com")
//                .login("Cameron")
//                .name("Кэмерон")
//                .birthday(LocalDate.of(1972, 8, 30))
//                .build();
//
//        firstFilm = Film.builder()
//                .id(1)
//                .description("Описание")
//                .releaseDate(LocalDate.of(1993, 1, 28))
//                .duration(101)
//                .name("Маска")
//                .mpa(listMpa.get(1))
//                .directors(listDirector)
//                .build();
//
//        secondFilm = Film.builder()
//                .description("Описание")
//                .releaseDate(LocalDate.of(1994, 12, 6))
//                .duration(107)
//                .name("Тупой и еще тупее")
//                .mpa(listMpa.get(1))
//                .directors(listDirector)
//                .build();
//
//        thirdFilm = Film.builder()
//                .description("Описание")
//                .releaseDate(LocalDate.of(2004, 3, 9))
//                .duration(108)
//                .name("Вечное сияние чистого разума")
//                .mpa(listMpa.get(1))
//                .directors(listDirector)
//                .build();
//
//        filmStorage.addFilm(firstFilm);
//        filmStorage.addFilm(secondFilm);
//        filmStorage.addFilm(thirdFilm);
//
//        userStorage.addUser(firstUser);
//        userStorage.addUser(secondUser);
//        userStorage.addUser(thirdUser);
//
//        filmStorage.addOrUpdateRateFromUserById(2, 1);
//        filmStorage.addOrUpdateRateFromUserById(2, 2);
//        filmStorage.addOrUpdateRateFromUserById(2, 3);
//
//        filmStorage.addOrUpdateRateFromUserById(3, 1);
//        filmStorage.addOrUpdateRateFromUserById(3, 2);
//
//        filmStorage.addOrUpdateRateFromUserById(1, 1);
//
//        assertEquals(filmStorage.getSortedFilmByDirector(1, "likes"), List.of(filmStorage.getFilmById(2),
//                filmStorage.getFilmById(3), filmStorage.getFilmById(1)));
//        assertEquals(filmStorage.getSortedFilmByDirector(1, "year"), List.of(filmStorage.getFilmById(1),
//                filmStorage.getFilmById(2), filmStorage.getFilmById(3)));
//    }

    @Test
    public void returnEmptyListIfNoSuchFilms() {
        List<Film> list1 = filmStorage.searchFilms("string", "director");
        List<Film> list2 = filmStorage.searchFilms("string", "title");
        List<Film> list3 = filmStorage.searchFilms("string", "director,title");
        List<Film> list4 = filmStorage.searchFilms("string", "title,director");

        assertTrue(list1.isEmpty());
        assertTrue(list2.isEmpty());
        assertTrue(list3.isEmpty());
        assertTrue(list4.isEmpty());
    }

    @Test
    public void throwExceptionIfWrongRequestParam() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            filmStorage.searchFilms("string", "param");
        });

        assertEquals("Wrong request param.", exception.getMessage());
    }

    @Test
    public void returnFilmIfFilmContainSubstringInName() {
        firstFilm = Film.builder()
                .id(1)
                .description("Описание")
                .releaseDate(LocalDate.of(1993, 1, 28))
                .duration(101)
                .name("Halt and catch fire")
                .directors(new ArrayList<>())
                .genres(new ArrayList<>())
                .mpa(listMpa.get(1))
                .build();

        filmStorage.addFilm(firstFilm);
        List<Film> result = filmStorage.searchFilms("catch", "title");

        assertTrue(result.contains(firstFilm));
    }

    @Test
    public void returnFilmIfDirectorNameContainSubstring() {
        Director director = Director.builder()
                .id(1)
                .name("ДжеймсКамерун")
                .build();
        List<Director> listDirector = new ArrayList<>();
        listDirector.add(director);
        directorStorage.addDirector(director);

        firstFilm = Film.builder()
                .id(1)
                .description("Описание")
                .releaseDate(LocalDate.of(1993, 1, 28))
                .duration(101)
                .name("Маска")
                .mpa(listMpa.get(1))
                .genres(new ArrayList<>())
                .directors(listDirector)
                .build();

        filmStorage.addFilm(firstFilm);
        List<Film> result = filmStorage.searchFilms("ДжеймсКамерун", "director");

        assertTrue(result.contains(firstFilm));
    }

    @Test
    public void returnFilmIfAtLeastOneFieldContainsSubstring() {
        Director director = Director.builder()
                .id(1)
                .name("ДжеймсКамерун")
                .build();
        List<Director> listDirector = new ArrayList<>();
        listDirector.add(director);
        directorStorage.addDirector(director);

        firstFilm = Film.builder()
                .id(1)
                .description("Описание")
                .releaseDate(LocalDate.of(1993, 1, 28))
                .duration(101)
                .name("Маска")
                .mpa(listMpa.get(1))
                .rate(3.14)
                .genres(new ArrayList<>())
                .directors(listDirector)
                .build();

        filmStorage.addFilm(firstFilm);
        List<Film> result1 = filmStorage.searchFilms("ДжеймсКамерун", "director,title");
        List<Film> result2 = filmStorage.searchFilms("Маска", "title,director");

        assertEquals(result1, result2);
        assertTrue(result1.contains(firstFilm));
        assertTrue(result2.contains(firstFilm));
    }

    @Test
    public void returnFilmIfBothFieldsContainSubstring() {
        Director director = Director.builder()
                .id(1)
                .name("ДжеймсКамерун")
                .build();
        List<Director> listDirector = new ArrayList<>();
        listDirector.add(director);
        directorStorage.addDirector(director);

        firstFilm = Film.builder()
                .id(1)
                .description("Описание")
                .releaseDate(LocalDate.of(1993, 1, 28))
                .duration(101)
                .name("ДжеймсКамерун")
                .mpa(listMpa.get(1))
                .genres(new ArrayList<>())
                .directors(listDirector)
                .build();

        filmStorage.addFilm(firstFilm);
        List<Film> result = filmStorage.searchFilms("Джеймс", "director,title");

        assertTrue(result.contains(firstFilm));
    }

    @Test
    public void sortFilmsByRates() {
        User user = User.builder()
                .id(1)
                .email("mail@mail.ru")
                .login("login")
                .name("name")
                .birthday(LocalDate.of(2003, 1, 28))
                .build();

        userStorage.addUser(user);

        firstFilm = Film.builder()
                .id(1)
                .description("Описание")
                .releaseDate(LocalDate.of(1993, 1, 28))
                .duration(101)
                .name("Halt and catch fire")
                .rate(3.0)
                .directors(new ArrayList<>())
                .genres(new ArrayList<>())
                .mpa(listMpa.get(1))
                .build();

        filmStorage.addFilm(firstFilm);
        filmStorage.addRateFromUserById(firstFilm.getId(), 1, 3);

        secondFilm = Film.builder()
                .id(2)
                .description("Описание")
                .releaseDate(LocalDate.of(1993, 1, 28))
                .duration(101)
                .rate(4.0)
                .name("Halt and catch fire 2")
                .directors(new ArrayList<>())
                .genres(new ArrayList<>())
                .mpa(listMpa.get(1))
                .build();

        filmStorage.addFilm(secondFilm);
        filmStorage.addRateFromUserById(secondFilm.getId(), 1, 4);

        List<Film> films = filmStorage.searchFilms("catch", "title");

        assertEquals(secondFilm, films.get(0));
        assertEquals(firstFilm, films.get(1));
        assertEquals(2, films.size());
    }
}
