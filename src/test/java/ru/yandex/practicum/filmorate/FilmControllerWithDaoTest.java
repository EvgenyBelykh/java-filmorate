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
import ru.yandex.practicum.filmorate.models.*;
import ru.yandex.practicum.filmorate.storage.daoImpl.DaoDirectorStorage;
import ru.yandex.practicum.filmorate.storage.daoImpl.DaoFilmStorage;
import ru.yandex.practicum.filmorate.storage.daoImpl.DaoUserStorage;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
                .name("??????????????")
                .build();
        Genre genre2 = Genre.builder()
                .id(2)
                .name("??????????")
                .build();
        Genre genre3 = Genre.builder()
                .id(3)
                .name("????????????????????")
                .build();
        Genre genre4 = Genre.builder()
                .id(4)
                .name("??????????????")
                .build();
        Genre genre5 = Genre.builder()
                .id(5)
                .name("????????????????????????????")
                .build();
        Genre genre6 = Genre.builder()
                .id(6)
                .name("????????????")
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
                .description("????????????????")
                .releaseDate(LocalDate.of(1994, 12, 14))
                .duration(101)
                .name("??????????")
                .mpa(listMpa.get(1))
                .build();
        filmStorage.addFilm(firstFilm);

        assertEquals("??????????", filmStorage.getFilmById(1).getName());
    }

    @Test
    public void updateFilmTest() {
        firstFilm = Film.builder()
                .description("????????????????")
                .releaseDate(LocalDate.of(1994, 12, 14))
                .duration(101)
                .name("??????????")
                .mpa(listMpa.get(1))
                .build();

        secondFilm = Film.builder()
                .id(1)
                .description("????????????????")
                .releaseDate(LocalDate.of(1994, 12, 6))
                .duration(107)
                .name("?????????? ?? ?????? ??????????")
                .mpa(listMpa.get(1))
                .build();
        filmStorage.addFilm(firstFilm);
        filmStorage.updateFilm(secondFilm);

        assertEquals("?????????? ?? ?????? ??????????", filmStorage.getFilmById(1).getName());
    }

    @Test
    public void addFilmWithWrongDateTest() {
        firstFilm = Film.builder()
                .description("????????????????")
                .releaseDate(LocalDate.of(1825, 12, 14))
                .duration(101)
                .name("??????????")
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> filmStorage.addFilm(firstFilm));
    }

    @Test
    public void addFilmWithNullMpa() {
        firstFilm = Film.builder()
                .description("????????????????")
                .releaseDate(LocalDate.of(1825, 12, 14))
                .duration(101)
                .name("??????????")
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> filmStorage.addFilm(firstFilm));
    }

    @Test
    public void removeFilmTest() {
        firstFilm = Film.builder()
                .description("????????????????")
                .releaseDate(LocalDate.of(1994, 12, 14))
                .duration(101)
                .name("??????????")
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
                .description("????????????????")
                .releaseDate(LocalDate.of(1994, 12, 14))
                .duration(101)
                .name("??????????")
                .mpa(listMpa.get(1))
                .build();
        filmStorage.addFilm(firstFilm);

        secondFilm = Film.builder()
                .description("????????????????")
                .releaseDate(LocalDate.of(1994, 12, 6))
                .duration(107)
                .name("?????????? ?? ?????? ??????????")
                .mpa(listMpa.get(1))
                .build();
        filmStorage.addFilm(secondFilm);
        assertEquals(2, filmStorage.getFilms().size());
    }

    @Test
    public void getFilmByIdTest() {
        firstFilm = Film.builder()
                .description("????????????????")
                .releaseDate(LocalDate.of(1994, 12, 14))
                .duration(101)
                .name("??????????")
                .mpa(listMpa.get(1))
                .build();
        filmStorage.addFilm(firstFilm);

        secondFilm = Film.builder()
                .description("????????????????")
                .releaseDate(LocalDate.of(1994, 12, 6))
                .duration(107)
                .name("?????????? ?? ?????? ??????????")
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
        assertEquals("?????????? c id: 3 ???? ???????????????????? ?? ????????"
                , exception.getMessage());
    }
    @Test
    public void addLikeFromUserByIdTest() {
        firstFilm = Film.builder()
                .description("????????????????")
                .releaseDate(LocalDate.of(1994, 12, 14))
                .duration(101)
                .name("??????????")
                .mpa(listMpa.get(1))
                .build();
        filmStorage.addFilm(firstFilm);

        User firstUser = User.builder()
                .email("jim@email.com")
                .login("Jim")
                .name("????????")
                .birthday(LocalDate.of(1962, 1, 17))
                .build();
        userStorage.addUser(firstUser);
        filmStorage.addLikeFromUserById(1,1);

        assertEquals(userStorage.getUserById(1).getLogin()
                , userStorage.getUserById(new ArrayList<>(filmStorage.getFilmById(1).getLikes()).get(0)).getLogin()
        , "???????????????? ???????????? ????????????" );
    }
    @Test
    public void removeLikeFromUserByIdTest() {
        firstFilm = Film.builder()
                .description("????????????????")
                .releaseDate(LocalDate.of(1994, 12, 14))
                .duration(101)
                .name("??????????")
                .mpa(listMpa.get(1))
                .build();
        filmStorage.addFilm(firstFilm);

        User firstUser = User.builder()
                .email("jim@email.com")
                .login("Jim")
                .name("????????")
                .birthday(LocalDate.of(1962, 1, 17))
                .build();
        userStorage.addUser(firstUser);
        filmStorage.addLikeFromUserById(1,1);
        filmStorage.removeLikeFromUserById(1,1);

        assertEquals(0
                , new ArrayList<>(filmStorage.getFilmById(1).getLikes()).size()
                , "???????????????????????? ???? ???????????????? ???? ???????????? ???????????????????????? ?????????????????????? ????????" );
    }
    @Test
    public void getMostPopularFilmByCountLikes() {
        User firstUser = User.builder()
                .email("jim@email.com")
                .login("Jim")
                .name("????????")
                .birthday(LocalDate.of(1962, 1, 17))
                .build();

        User secondUser = User.builder()
                .email("jeff@email.com")
                .login("Jeff")
                .name("??????????")
                .birthday(LocalDate.of(1955, 2, 19))
                .build();

        User thirdUser = User.builder()
                .email("Diaz@email.com")
                .login("Cameron")
                .name("??????????????")
                .birthday(LocalDate.of(1972, 8, 30))
                .build();

        firstFilm = Film.builder()
                .id(1)
                .description("????????????????")
                .releaseDate(LocalDate.of(1994, 1, 28))
                .duration(101)
                .name("??????????")
                .mpa(listMpa.get(1))
                .build();

        secondFilm = Film.builder()
                .description("????????????????")
                .releaseDate(LocalDate.of(1994, 12, 6))
                .duration(107)
                .name("?????????? ?? ?????? ??????????")
                .mpa(listMpa.get(1))
                .build();

        thirdFilm = Film.builder()
                .description("????????????????")
                .releaseDate(LocalDate.of(2004, 3, 9))
                .duration(108)
                .name("???????????? ???????????? ?????????????? ????????????")
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
                .name("????????")
                .birthday(LocalDate.of(1962, 1, 17))
                .build();

        User secondUser = User.builder()
                .email("jeff@email.com")
                .login("Jeff")
                .name("??????????")
                .birthday(LocalDate.of(1955, 2, 19))
                .build();

        User thirdUser = User.builder()
                .email("Diaz@email.com")
                .login("Cameron")
                .name("??????????????")
                .birthday(LocalDate.of(1972, 8, 30))
                .build();

        firstFilm = Film.builder()
                .id(1)
                .description("????????????????")
                .releaseDate(LocalDate.of(1994, 1, 28))
                .duration(101)
                .name("??????????")
                .mpa(listMpa.get(1))
                .genres(Collections.singletonList(listGenre.get(0)))
                .build();

        secondFilm = Film.builder()
                .description("????????????????")
                .releaseDate(LocalDate.of(1994, 12, 6))
                .duration(107)
                .name("?????????? ?? ?????? ??????????")
                .mpa(listMpa.get(1))
                .genres(Collections.singletonList(listGenre.get(0)))
                .build();

        thirdFilm = Film.builder()
                .description("????????????????")
                .releaseDate(LocalDate.of(2004, 3, 9))
                .duration(108)
                .name("???????????? ???????????? ?????????????? ????????????")
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
                , "???????????????? ???????????????? ???????????? ??????????????");
        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, 2, null).size(), 1
                , "???????????????? ???????????????? ???????????? ??????????????");
        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, 3, null).size(), 0
                , "???????????????? ???????????????? ???????????? ??????????????");
    }
    @Test
    public void getMostPopularFilmByYearTest() {
        User firstUser = User.builder()
                .email("jim@email.com")
                .login("Jim")
                .name("????????")
                .birthday(LocalDate.of(1962, 1, 17))
                .build();

        User secondUser = User.builder()
                .email("jeff@email.com")
                .login("Jeff")
                .name("??????????")
                .birthday(LocalDate.of(1955, 2, 19))
                .build();

        User thirdUser = User.builder()
                .email("Diaz@email.com")
                .login("Cameron")
                .name("??????????????")
                .birthday(LocalDate.of(1972, 8, 30))
                .build();

        firstFilm = Film.builder()
                .id(1)
                .description("????????????????")
                .releaseDate(LocalDate.of(1994, 1, 28))
                .duration(101)
                .name("??????????")
                .mpa(listMpa.get(1))
                .genres(Collections.singletonList(listGenre.get(0)))
                .build();

        secondFilm = Film.builder()
                .description("????????????????")
                .releaseDate(LocalDate.of(1994, 12, 6))
                .duration(107)
                .name("?????????? ?? ?????? ??????????")
                .mpa(listMpa.get(1))
                .genres(Collections.singletonList(listGenre.get(0)))
                .build();

        thirdFilm = Film.builder()
                .description("????????????????")
                .releaseDate(LocalDate.of(2004, 3, 9))
                .duration(108)
                .name("???????????? ???????????? ?????????????? ????????????")
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
                , "???????????????? ???????????????? ???????????? ??????????????");
        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, null, Year.of(2004)).size(), 1
                , "???????????????? ???????????????? ???????????? ??????????????");
        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, null, Year.of(2022)).size(), 0
                , "???????????????? ???????????????? ???????????? ??????????????");
    }
    @Test
    public void getMostPopularFilmByGenreAndYearTest() {
        User firstUser = User.builder()
                .email("jim@email.com")
                .login("Jim")
                .name("????????")
                .birthday(LocalDate.of(1962, 1, 17))
                .build();

        User secondUser = User.builder()
                .email("jeff@email.com")
                .login("Jeff")
                .name("??????????")
                .birthday(LocalDate.of(1955, 2, 19))
                .build();

        User thirdUser = User.builder()
                .email("Diaz@email.com")
                .login("Cameron")
                .name("??????????????")
                .birthday(LocalDate.of(1972, 8, 30))
                .build();

        firstFilm = Film.builder()
                .id(1)
                .description("????????????????")
                .releaseDate(LocalDate.of(1994, 1, 28))
                .duration(101)
                .name("??????????")
                .mpa(listMpa.get(1))
                .genres(Collections.singletonList(listGenre.get(0)))
                .build();

        secondFilm = Film.builder()
                .description("????????????????")
                .releaseDate(LocalDate.of(1994, 12, 6))
                .duration(107)
                .name("?????????? ?? ?????? ??????????")
                .mpa(listMpa.get(1))
                .genres(Collections.singletonList(listGenre.get(0)))
                .build();

        thirdFilm = Film.builder()
                .description("????????????????")
                .releaseDate(LocalDate.of(2004, 3, 9))
                .duration(108)
                .name("???????????? ???????????? ?????????????? ????????????")
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
                , "???????????????? ???????????????? ???????????? ??????????????");
        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, 2, Year.of(2004)).size(), 1
                , "???????????????? ???????????????? ???????????? ??????????????");
        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, 1, Year.of(2022)).size(), 0
                , "???????????????? ???????????????? ???????????? ??????????????");
        assertEquals(filmStorage.getMostPopularFilmByCountLikes(10, 2, Year.of(1994)).size(), 0
                , "???????????????? ???????????????? ???????????? ??????????????");
    }

   @Test
    public void getSortedFilmByDirectorTest() {
        Director director = Director.builder()
                .id(1)
                .name("??????????????????????????")
                .build();
        List<Director> listDirector = new ArrayList<>();
        listDirector.add(director);
        directorStorage.addDirector(director);

        User firstUser = User.builder()
                .email("jim@email.com")
                .login("Jim")
                .name("????????")
                .birthday(LocalDate.of(1962, 1, 17))
                .build();

        User secondUser = User.builder()
                .email("jeff@email.com")
                .login("Jeff")
                .name("??????????")
                .birthday(LocalDate.of(1955, 2, 19))
                .build();

        User thirdUser = User.builder()
                .email("Diaz@email.com")
                .login("Cameron")
                .name("??????????????")
                .birthday(LocalDate.of(1972, 8, 30))
                .build();

        firstFilm = Film.builder()
                .id(1)
                .description("????????????????")
                .releaseDate(LocalDate.of(1993, 1, 28))
                .duration(101)
                .name("??????????")
                .mpa(listMpa.get(1))
                .directors(listDirector)
                .build();

        secondFilm = Film.builder()
                .description("????????????????")
                .releaseDate(LocalDate.of(1994, 12, 6))
                .duration(107)
                .name("?????????? ?? ?????? ??????????")
                .mpa(listMpa.get(1))
                .directors(listDirector)
                .build();

        thirdFilm = Film.builder()
                .description("????????????????")
                .releaseDate(LocalDate.of(2004, 3, 9))
                .duration(108)
                .name("???????????? ???????????? ?????????????? ????????????")
                .mpa(listMpa.get(1))
                .directors(listDirector)
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

        assertEquals(filmStorage.getSortedFilmByDirector(1, "likes"), List.of(filmStorage.getFilmById(2),
                filmStorage.getFilmById(3), filmStorage.getFilmById(1)));
        assertEquals(filmStorage.getSortedFilmByDirector(1, "year"), List.of(filmStorage.getFilmById(1),
                filmStorage.getFilmById(2), filmStorage.getFilmById(3)));
    }
    
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
                .description("????????????????")
                .releaseDate(LocalDate.of(1993, 1, 28))
                .duration(101)
                .name("Halt and catch fire")
                .likes(new HashSet<>())
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
                .name("??????????????????????????")
                .build();
        List<Director> listDirector = new ArrayList<>();
        listDirector.add(director);
        directorStorage.addDirector(director);

        firstFilm = Film.builder()
                .id(1)
                .description("????????????????")
                .releaseDate(LocalDate.of(1993, 1, 28))
                .duration(101)
                .name("??????????")
                .mpa(listMpa.get(1))
                .likes(new HashSet<>())
                .genres(new ArrayList<>())
                .directors(listDirector)
                .build();

        filmStorage.addFilm(firstFilm);
        List<Film> result = filmStorage.searchFilms("??????????????????????????", "director");

        assertTrue(result.contains(firstFilm));
    }

    @Test
    public void returnFilmIfAtLeastOneFieldContainsSubstring() {
        Director director = Director.builder()
                .id(1)
                .name("??????????????????????????")
                .build();
        List<Director> listDirector = new ArrayList<>();
        listDirector.add(director);
        directorStorage.addDirector(director);

        firstFilm = Film.builder()
                .id(1)
                .description("????????????????")
                .releaseDate(LocalDate.of(1993, 1, 28))
                .duration(101)
                .name("??????????")
                .mpa(listMpa.get(1))
                .likes(new HashSet<>())
                .genres(new ArrayList<>())
                .directors(listDirector)
                .build();

        filmStorage.addFilm(firstFilm);
        List<Film> result1 = filmStorage.searchFilms("??????????????????????????", "director,title");
        List<Film> result2 = filmStorage.searchFilms("??????????", "title,director");

        assertEquals(result1, result2);
        assertTrue(result1.contains(firstFilm));
        assertTrue(result2.contains(firstFilm));
    }

    @Test
    public void returnFilmIfBothFieldsContainSubstring() {
        Director director = Director.builder()
                .id(1)
                .name("??????????????????????????")
                .build();
        List<Director> listDirector = new ArrayList<>();
        listDirector.add(director);
        directorStorage.addDirector(director);

        firstFilm = Film.builder()
                .id(1)
                .description("????????????????")
                .releaseDate(LocalDate.of(1993, 1, 28))
                .duration(101)
                .name("??????????????????????????")
                .mpa(listMpa.get(1))
                .likes(new HashSet<>())
                .genres(new ArrayList<>())
                .directors(listDirector)
                .build();

        filmStorage.addFilm(firstFilm);
        List<Film> result = filmStorage.searchFilms("????????????", "director,title");

        assertTrue(result.contains(firstFilm));
    }
}
