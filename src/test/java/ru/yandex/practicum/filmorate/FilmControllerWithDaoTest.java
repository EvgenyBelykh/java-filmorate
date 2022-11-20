package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.models.Genre;
import ru.yandex.practicum.filmorate.models.Mpa;
import ru.yandex.practicum.filmorate.models.User;
import ru.yandex.practicum.filmorate.storage.daoImpl.DaoFilmStorage;
import ru.yandex.practicum.filmorate.storage.daoImpl.DaoUserStorage;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmControllerWithDaoTest {
    private final JdbcTemplate jdbcTemplate;
    private final DaoFilmStorage filmStorage;
    private final DaoUserStorage userStorage;
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
        jdbcTemplate.update("DELETE FROM LIKES");
        jdbcTemplate.update("DELETE FROM USERS_FRIENDS");
        jdbcTemplate.update("DELETE FROM FILM_GENRES");
        jdbcTemplate.update("ALTER TABLE USERS ALTER COLUMN ID RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE FILMS ALTER COLUMN ID RESTART WITH 1");
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
                .description("Описание")
                .releaseDate(LocalDate.of(1994, 12, 14))
                .duration(101)
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
        filmStorage.addFilm(firstFilm);
        filmStorage.updateFilm(secondFilm);

        assertEquals("Тупой и еще тупее", filmStorage.getFilmById(1).getName());
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
        filmStorage.removeFilm(firstFilm);
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
    public void addLikeFromUserByIdTest() {
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
        filmStorage.addLikeFromUserById(1,1);

        assertEquals(userStorage.getUserById(1).getLogin()
                , userStorage.getUserById(new ArrayList<>(filmStorage.getFilmById(1).getLikes()).get(0)).getLogin()
        , "Неверный список лайков" );
    }
    @Test
    public void removeLikeFromUserByIdTest() {
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
        filmStorage.addLikeFromUserById(1,1);
        filmStorage.removeLikeFromUserById(1,1);

        assertEquals(0
                , new ArrayList<>(filmStorage.getFilmById(1).getLikes()).size()
                , "Пользователь не удалился из списка пользовтелей поставившиз лайк" );
    }
    @Test
    public void getMostPopularFilmByCountLikes() {
        User firstUser = User.builder()
                .email("jim@email.com")
                .login("Jim")
                .name("Джим")
                .birthday(LocalDate.of(1962, 1, 17))
                .build();

        User secondUser = User.builder()
                .email("jeff@email.com")
                .login("Jeff")
                .name("Джефф")
                .birthday(LocalDate.of(1955, 2, 19))
                .build();

        User thirdUser = User.builder()
                .email("Diaz@email.com")
                .login("Cameron")
                .name("Кэмерон")
                .birthday(LocalDate.of(1972, 8, 30))
                .build();

        firstFilm = Film.builder()
                .id(1)
                .description("Описание")
                .releaseDate(LocalDate.of(1994, 1, 28))
                .duration(101)
                .name("Маска")
                .mpa(listMpa.get(1))
                .build();

        secondFilm = Film.builder()
                .description("Описание")
                .releaseDate(LocalDate.of(1994, 12, 6))
                .duration(107)
                .name("Тупой и еще тупее")
                .mpa(listMpa.get(1))
                .build();

        thirdFilm = Film.builder()
                .description("Описание")
                .releaseDate(LocalDate.of(2004, 3, 9))
                .duration(108)
                .name("Вечное сияние чистого разума")
                .mpa(listMpa.get(1))
                .build();

        filmStorage.addFilm(firstFilm);
        filmStorage.addFilm(secondFilm);
        filmStorage.addFilm(thirdFilm);

        userStorage.addUser(firstUser);
        userStorage.addUser(secondUser);
        userStorage.addUser(thirdUser);

        filmStorage.addLikeFromUserById(2, 1);
        filmStorage.addLikeFromUserById(2, 2);
        filmStorage.addLikeFromUserById(2, 3);

        filmStorage.addLikeFromUserById(3, 1);
        filmStorage.addLikeFromUserById(3, 2);

        filmStorage.addLikeFromUserById(1, 1);

        assertEquals(filmStorage.getMostPopularFilmByCountLikes(1, null, null).size(), 1);
        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, null, null).size(), 3);
        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, null, null).size(), 3);
    }
    @Test
    public void getMostPopularFilmByGenreTest() {
        User firstUser = User.builder()
                .email("jim@email.com")
                .login("Jim")
                .name("Джим")
                .birthday(LocalDate.of(1962, 1, 17))
                .build();

        User secondUser = User.builder()
                .email("jeff@email.com")
                .login("Jeff")
                .name("Джефф")
                .birthday(LocalDate.of(1955, 2, 19))
                .build();

        User thirdUser = User.builder()
                .email("Diaz@email.com")
                .login("Cameron")
                .name("Кэмерон")
                .birthday(LocalDate.of(1972, 8, 30))
                .build();

        firstFilm = Film.builder()
                .id(1)
                .description("Описание")
                .releaseDate(LocalDate.of(1994, 1, 28))
                .duration(101)
                .name("Маска")
                .mpa(listMpa.get(1))
                .genres(Collections.singletonList(listGenre.get(0)))
                .build();

        secondFilm = Film.builder()
                .description("Описание")
                .releaseDate(LocalDate.of(1994, 12, 6))
                .duration(107)
                .name("Тупой и еще тупее")
                .mpa(listMpa.get(1))
                .genres(Collections.singletonList(listGenre.get(0)))
                .build();

        thirdFilm = Film.builder()
                .description("Описание")
                .releaseDate(LocalDate.of(2004, 3, 9))
                .duration(108)
                .name("Вечное сияние чистого разума")
                .mpa(listMpa.get(1))
                .genres(Collections.singletonList(listGenre.get(1)))
                .build();

        filmStorage.addFilm(firstFilm);
        filmStorage.addFilm(secondFilm);
        filmStorage.addFilm(thirdFilm);

        userStorage.addUser(firstUser);
        userStorage.addUser(secondUser);
        userStorage.addUser(thirdUser);

        filmStorage.addLikeFromUserById(2, 1);
        filmStorage.addLikeFromUserById(2, 2);
        filmStorage.addLikeFromUserById(2, 3);

        filmStorage.addLikeFromUserById(3, 1);
        filmStorage.addLikeFromUserById(3, 2);

        filmStorage.addLikeFromUserById(1, 1);

        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, 1, null).size(), 2
                , "Вернулся неверный список фильмов");
        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, 2, null).size(), 1
                , "Вернулся неверный список фильмов");
        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, 3, null).size(), 0
                , "Вернулся неверный список фильмов");
    }
    @Test
    public void getMostPopularFilmByYearTest() {
        User firstUser = User.builder()
                .email("jim@email.com")
                .login("Jim")
                .name("Джим")
                .birthday(LocalDate.of(1962, 1, 17))
                .build();

        User secondUser = User.builder()
                .email("jeff@email.com")
                .login("Jeff")
                .name("Джефф")
                .birthday(LocalDate.of(1955, 2, 19))
                .build();

        User thirdUser = User.builder()
                .email("Diaz@email.com")
                .login("Cameron")
                .name("Кэмерон")
                .birthday(LocalDate.of(1972, 8, 30))
                .build();

        firstFilm = Film.builder()
                .id(1)
                .description("Описание")
                .releaseDate(LocalDate.of(1994, 1, 28))
                .duration(101)
                .name("Маска")
                .mpa(listMpa.get(1))
                .genres(Collections.singletonList(listGenre.get(0)))
                .build();

        secondFilm = Film.builder()
                .description("Описание")
                .releaseDate(LocalDate.of(1994, 12, 6))
                .duration(107)
                .name("Тупой и еще тупее")
                .mpa(listMpa.get(1))
                .genres(Collections.singletonList(listGenre.get(0)))
                .build();

        thirdFilm = Film.builder()
                .description("Описание")
                .releaseDate(LocalDate.of(2004, 3, 9))
                .duration(108)
                .name("Вечное сияние чистого разума")
                .mpa(listMpa.get(1))
                .genres(Collections.singletonList(listGenre.get(1)))
                .build();

        filmStorage.addFilm(firstFilm);
        filmStorage.addFilm(secondFilm);
        filmStorage.addFilm(thirdFilm);

        userStorage.addUser(firstUser);
        userStorage.addUser(secondUser);
        userStorage.addUser(thirdUser);

        filmStorage.addLikeFromUserById(2, 1);
        filmStorage.addLikeFromUserById(2, 2);
        filmStorage.addLikeFromUserById(2, 3);

        filmStorage.addLikeFromUserById(3, 1);
        filmStorage.addLikeFromUserById(3, 2);

        filmStorage.addLikeFromUserById(1, 1);

        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, null, Year.of(1994)).size(), 2
                , "Вернулся неверный список фильмов");
        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, null, Year.of(2004)).size(), 1
                , "Вернулся неверный список фильмов");
        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, null, Year.of(2022)).size(), 0
                , "Вернулся неверный список фильмов");
    }
    @Test
    public void getMostPopularFilmByGenreAndYearTest() {
        User firstUser = User.builder()
                .email("jim@email.com")
                .login("Jim")
                .name("Джим")
                .birthday(LocalDate.of(1962, 1, 17))
                .build();

        User secondUser = User.builder()
                .email("jeff@email.com")
                .login("Jeff")
                .name("Джефф")
                .birthday(LocalDate.of(1955, 2, 19))
                .build();

        User thirdUser = User.builder()
                .email("Diaz@email.com")
                .login("Cameron")
                .name("Кэмерон")
                .birthday(LocalDate.of(1972, 8, 30))
                .build();

        firstFilm = Film.builder()
                .id(1)
                .description("Описание")
                .releaseDate(LocalDate.of(1994, 1, 28))
                .duration(101)
                .name("Маска")
                .mpa(listMpa.get(1))
                .genres(Collections.singletonList(listGenre.get(0)))
                .build();

        secondFilm = Film.builder()
                .description("Описание")
                .releaseDate(LocalDate.of(1994, 12, 6))
                .duration(107)
                .name("Тупой и еще тупее")
                .mpa(listMpa.get(1))
                .genres(Collections.singletonList(listGenre.get(0)))
                .build();

        thirdFilm = Film.builder()
                .description("Описание")
                .releaseDate(LocalDate.of(2004, 3, 9))
                .duration(108)
                .name("Вечное сияние чистого разума")
                .mpa(listMpa.get(1))
                .genres(Collections.singletonList(listGenre.get(1)))
                .build();

        filmStorage.addFilm(firstFilm);
        filmStorage.addFilm(secondFilm);
        filmStorage.addFilm(thirdFilm);

        userStorage.addUser(firstUser);
        userStorage.addUser(secondUser);
        userStorage.addUser(thirdUser);

        filmStorage.addLikeFromUserById(2, 1);
        filmStorage.addLikeFromUserById(2, 2);
        filmStorage.addLikeFromUserById(2, 3);

        filmStorage.addLikeFromUserById(3, 1);
        filmStorage.addLikeFromUserById(3, 2);

        filmStorage.addLikeFromUserById(1, 1);

        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, 1, Year.of(1994)).size(), 2
                , "Вернулся неверный список фильмов");
        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, 2, Year.of(2004)).size(), 1
                , "Вернулся неверный список фильмов");
        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, 1, Year.of(2022)).size(), 0
                , "Вернулся неверный список фильмов");
        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, 2, Year.of(1994)).size(), 0
                , "Вернулся неверный список фильмов");
    }
}
