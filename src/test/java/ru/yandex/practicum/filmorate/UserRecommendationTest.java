package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.models.Mpa;
import ru.yandex.practicum.filmorate.models.User;
import ru.yandex.practicum.filmorate.storage.daoImpl.DaoFilmStorage;
import ru.yandex.practicum.filmorate.storage.daoImpl.DaoUserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserRecommendationTest {
    private final DaoUserStorage daoUserStorage;
    private final DaoFilmStorage daoFilmStorage;


    @Test
    public void testGetRecommendationsForUserById() {
        // создание тестовых данных
        Film testFilm = Film.builder()
                .name("Titanic")
                .description("It's a trap ")
                .releaseDate(LocalDate.of(1997, 12, 19))
                .duration(194)
                .mpa(Mpa.builder()
                        .id(1)
                        .name("G")
                        .build())
                .build();

        Film testFilm2 = Film.builder()
                .name("Titanic2")
                .description("It's a trap 2")
                .releaseDate(LocalDate.of(1997, 12, 19))
                .duration(194)
                .mpa(Mpa.builder()
                        .id(1)
                        .name("G")
                        .build())
        .build();

        Film testFilm3 = Film.builder()
                .name("Titanic3")
                .description("It's a trap 3")
                .releaseDate(LocalDate.of(1997, 12, 19))
                .duration(194)
                .mpa(Mpa.builder()
                        .id(1)
                        .name("G")
                        .build())
                .build();

        daoFilmStorage.addFilm(testFilm);
        daoFilmStorage.addFilm(testFilm2);
        daoFilmStorage.addFilm(testFilm3);


        Optional<User> user = Optional.of(new User(1, "email@mail.ru", "Lol", "Lola", LocalDate.of(1999, 1, 1), Set.of(2, 3)));
        daoUserStorage.addUser(user.get());

        Optional<User> user2 = Optional.of(new User(2, "email2@mail.ru", "Lol2", "Lola2", LocalDate.of(1999, 1, 2), Set.of(1, 3)));
        daoUserStorage.addUser(user2.get());

        Optional<User> user3 = Optional.of(new User(3, "email3@mail.ru", "Lol3", "Lola3", LocalDate.of(1999, 1, 3), Set.of(1, 2)));
        daoUserStorage.addUser(user3.get());

        Optional<User> userOptional = Optional.ofNullable(daoUserStorage.getUserById(1));
        Optional<User> userOptional2 = Optional.ofNullable(daoUserStorage.getUserById(2));
        Optional<User> userOptional3 = Optional.ofNullable(daoUserStorage.getUserById(3));

        assertThat(userOptional).isPresent()
                .hasValueSatisfying(user1 -> assertThat(user1.getLogin()).isEqualTo("Lol"));

        assertThat(userOptional2).isPresent()
                .hasValueSatisfying(user1 -> assertThat(user1.getLogin()).isEqualTo("Lol2"));

        assertThat(userOptional3).isPresent()
                .hasValueSatisfying(user1 -> assertThat(user1.getLogin()).isEqualTo("Lol3"));

        //Проверка рекомендаций

//        daoFilmStorage.addOrUpdateRateFromUserById(1, 1);
//        daoFilmStorage.addOrUpdateRateFromUserById(2, 2);

        Optional<List<Film>> emptyRecommendations = Optional.ofNullable(daoUserStorage.getRecommendations(1));

        assertThat(emptyRecommendations).isPresent()
                .hasValueSatisfying(films -> assertThat(films.size()).isEqualTo(0));

//        daoFilmStorage.addOrUpdateRateFromUserById(3, 1);
//        daoFilmStorage.addOrUpdateRateFromUserById(3, 2);


        Optional<List<Film>> recommendations = Optional.ofNullable(daoUserStorage.getRecommendations(1));

        assertThat(recommendations).isPresent()
                .hasValueSatisfying(films -> assertThat(films.size()).isEqualTo(1));

    }
}
