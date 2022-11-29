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
import ru.yandex.practicum.filmorate.models.User;
import ru.yandex.practicum.filmorate.storage.daoImpl.DaoUserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserControllerWithDaoTest {

    private final JdbcTemplate jdbcTemplate;
    private final DaoUserStorage userStorage;
    User firstUser;
    User secondUser;
    User thirdUser;

    @AfterEach
    void tearDown(){
        jdbcTemplate.update("DELETE FROM USERS");
        jdbcTemplate.update("DELETE FROM FILMS");
        jdbcTemplate.update("DELETE FROM RATE");
        jdbcTemplate.update("DELETE FROM USERS_FRIENDS");
        jdbcTemplate.update("DELETE FROM FILM_GENRES");
        jdbcTemplate.update("ALTER TABLE USERS ALTER COLUMN ID RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE FILMS ALTER COLUMN ID RESTART WITH 1");
    }

    @Test
    public void addUserTest() {
        firstUser = User.builder()
                .email("jim@email.com")
                .login("Jim")
                .name("Джим")
                .birthday(LocalDate.of(1962, 12, 17))
                .build();
        userStorage.addUser(firstUser);
        assertEquals("Джим", userStorage.getUserById(1).getName());
    }
    @Test
    public void updateUserTest() {
        firstUser = User.builder()
                .email("jim@email.com")
                .login("Jim")
                .name("Джим")
                .birthday(LocalDate.of(1962, 2, 17))
                .build();
        userStorage.addUser(firstUser);

        secondUser = User.builder()
                .id(1)
                .email("jeff@email.com")
                .login("Jeff")
                .name("Джефф")
                .birthday(LocalDate.of(1955, 2, 19))
                .build();
        userStorage.updateUser(secondUser);

        assertEquals("Джефф", userStorage.getUserById(1).getName());
    }
    @Test
    public void removeUserTest() {
        firstUser = User.builder()
                .email("jim@email.com")
                .login("Jim")
                .name("Джим")
                .birthday(LocalDate.of(1962, 12, 17))
                .build();
        userStorage.addUser(firstUser);
        firstUser.setId(1);
        userStorage.removeUser(1);
        assertEquals(new ArrayList<>(0),userStorage.getUsers());
    }
    @Test
    public void getUserByIdTest() {
        firstUser = User.builder()
                .email("jim@email.com")
                .login("Jim")
                .name("Джим")
                .birthday(LocalDate.of(1962, 2, 17))
                .build();
        userStorage.addUser(firstUser);

        secondUser = User.builder()
                .email("jeff@email.com")
                .login("Jeff")
                .name("Джефф")
                .birthday(LocalDate.of(1955, 2, 19))
                .build();
        userStorage.addUser(secondUser);

        assertEquals(secondUser.getName(), userStorage.getUserById(2).getName());

        Throwable exception = assertThrows(
                ValidationException.class,
                () -> {
                    userStorage.getUserById(3);
                }
        );
        assertEquals("Пользователь c id - 3 не содержится в базе"
                , exception.getMessage());
    }
    @Test
    public void getUsersTest() {
        firstUser = User.builder()
                .email("jim@email.com")
                .login("Jim")
                .name("Джим")
                .birthday(LocalDate.of(1962, 1, 17))
                .build();
        userStorage.addUser(firstUser);

        secondUser = User.builder()
                .email("jeff@email.com")
                .login("Jeff")
                .name("Джефф")
                .birthday(LocalDate.of(1955, 2, 19))
                .build();
        userStorage.addUser(secondUser);
        assertEquals(2,userStorage.getUsers().size());
    }

    @Test
    public void addUserWithNullNameTest() {
        firstUser = User.builder()
                .email("jim@email.com")
                .login("Jim")
                .name("")
                .birthday(LocalDate.of(1962, 1, 17))
                .build();
        userStorage.addUser(firstUser);
        assertEquals("Jim", userStorage.getUserById(1).getName());
    }
    @Test
    public void addUserWithNullLoginTest() {
        firstUser = User.builder()
                .email("jim@email.com")
                .login("")
                .name("Джим")
                .birthday(LocalDate.of(1962, 1, 17))
                .build();
        assertThrows(DataIntegrityViolationException.class, () -> userStorage.addUser(firstUser));
    }
    @Test
    public void addUserWithFutureBirthdayTest() {
        firstUser = User.builder()
                .email("jim@email.com")
                .login("Jim")
                .name("")
                .birthday(LocalDate.of(2062, 1, 17))
                .build();
        assertThrows(DataIntegrityViolationException.class, () -> userStorage.addUser(firstUser));
    }

    @Test
    public void addFriendsTest() {
        firstUser = User.builder()
                .email("jim@email.com")
                .login("Jim")
                .name("Джим")
                .birthday(LocalDate.of(1962, 1, 17))
                .build();

        secondUser = User.builder()
                .email("jeff@email.com")
                .login("Jeff")
                .name("Джефф")
                .birthday(LocalDate.of(1955, 2, 19))
                .build();

        userStorage.addUser(firstUser);
        userStorage.addUser(secondUser);
        userStorage.addFriend(1, 2);
                assertEquals(1, userStorage.getUserById(1).getFriends().size());
                assertEquals(0, userStorage.getUserById(2).getFriends().size());
    }
    @Test
    public void getFriendsByIdTest() {
        firstUser = User.builder()
                .email("jim@email.com")
                .login("Jim")
                .name("Джим")
                .birthday(LocalDate.of(1962, 1, 17))
                .build();

        secondUser = User.builder()
                .email("jeff@email.com")
                .login("Jeff")
                .name("Джефф")
                .birthday(LocalDate.of(1955, 2, 19))
                .build();

        userStorage.addUser(firstUser);
        userStorage.addUser(secondUser);
        userStorage.addFriend(1, 2);
        List<User> listUser = new ArrayList<>(Collections.singleton(userStorage.getUserById(2)));

        assertEquals(new HashSet<>(listUser), userStorage.getFriendsById(1));
    }
    @Test
    public void getCommonFriendsTest() {
        firstUser = User.builder()
                .email("jim@email.com")
                .login("Jim")
                .name("Джим")
                .birthday(LocalDate.of(1962, 1, 17))
                .build();

        secondUser = User.builder()
                .email("jeff@email.com")
                .login("Jeff")
                .name("Джефф")
                .birthday(LocalDate.of(1955, 2, 19))
                .build();

        thirdUser = User.builder()
                .email("Diaz@email.com")
                .login("Cameron")
                .name("Кэмерон")
                .birthday(LocalDate.of(1972, 8, 30))
                .build();

        userStorage.addUser(firstUser);
        userStorage.addUser(secondUser);
        userStorage.addUser(thirdUser);
        userStorage.addFriend(1, 3);
        userStorage.addFriend(3, 1);
        userStorage.addFriend(3, 2);
        userStorage.addFriend(1, 2);

        assertEquals(Stream.of(userStorage.getUserById(2)).collect(Collectors.toSet()),
                userStorage.getCommonFriends(1,3));
    }
    @Test
    public void removeFriendTest() {
        firstUser = User.builder()
                .email("jim@email.com")
                .login("Jim")
                .name("Джим")
                .birthday(LocalDate.of(1962, 1, 17))
                .build();

        secondUser = User.builder()
                .email("jeff@email.com")
                .login("Jeff")
                .name("Джефф")
                .birthday(LocalDate.of(1955, 2, 19))
                .build();

        userStorage.addUser(firstUser);
        userStorage.addUser(secondUser);
        userStorage.addFriend(1, 2);
        userStorage.removeFriend(1, 2);

        assertEquals(0, userStorage.getUserById(1).getFriends().size());
    }
}
