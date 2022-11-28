package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.models.Director;
import ru.yandex.practicum.filmorate.storage.daoImpl.DaoDirectorStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DaoDirectorControllerTest {
    private final DaoDirectorStorage directorStorage;
    private final JdbcTemplate jdbcTemplate;
    Director director1 = Director.builder()
            .id(1)
            .name("ДжеймсКамерун")
            .build();
    Director director2 = Director.builder()
            .id(2)
            .name("РидлиСкот")
            .build();
    Director director3 = Director.builder()
            .id(3)
            .name("ФедорБондарчук")
            .build();

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
    public void addDirectorTest(){
        directorStorage.addDirector(director1);
        assertEquals(directorStorage.getDirectorById(1), director1);
    }

    @Test
    public void getAllDirectorTest(){
        directorStorage.addDirector(director1);
        directorStorage.addDirector(director2);
        directorStorage.addDirector(director3);
        assertEquals(directorStorage.getAllDirector(), List.of(director1, director2, director3));
    }

    @Test
    public void getDirectorByIdTest(){
        directorStorage.addDirector(director1);
        directorStorage.addDirector(director2);
        directorStorage.addDirector(director3);
        assertEquals(directorStorage.getDirectorById(2), director2);
    }

    @Test
    public void updateDirectorTest(){
        directorStorage.addDirector(director1);
        director1.setName("АлександрРобокопов");
        directorStorage.updateDirector(director1);
        assertEquals(director1.getName(), "АлександрРобокопов");
    }

    @Test
    public void removeDirectorTest(){
        directorStorage.addDirector(director1);
        directorStorage.addDirector(director2);
        directorStorage.addDirector(director3);
        directorStorage.removeDirector(2);
        assertEquals(directorStorage.getAllDirector(), List.of(director1, director3));
    }
}
