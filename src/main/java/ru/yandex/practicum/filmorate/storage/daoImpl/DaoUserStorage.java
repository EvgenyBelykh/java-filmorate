package ru.yandex.practicum.filmorate.storage.daoImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.SQLRequests;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.models.User;
import ru.yandex.practicum.filmorate.services.DirectorService;
import ru.yandex.practicum.filmorate.services.GenreService;
import ru.yandex.practicum.filmorate.services.MpaService;
import ru.yandex.practicum.filmorate.storage.interf.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
@Slf4j
@Primary
public class DaoUserStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final DirectorService directorService;
    public DaoUserStorage(JdbcTemplate jdbcTemplate, MpaService mpaService, GenreService genreService, DirectorService directorService) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaService = mpaService;
        this.genreService = genreService;
        this.directorService = directorService;
    }

    @Override
    public User getUserById(Integer userId) {
        try{
            String sqlQuery = "SELECT id, name, email, login, birthday " +
                    "FROM users " +
                    "WHERE id = ?";
            return jdbcTemplate.queryForObject(sqlQuery, new UserRowMapper(jdbcTemplate, mpaService, genreService, directorService), userId);
        } catch (Exception e) {
            log.info("Пользователь c id - {} не содержится в базе", userId);
            throw new ValidationException("Пользователь c id - " + userId + " не содержится в базе");
        }
    }

    @Override
    public User addUser(User user) {
        String sqlQuery = "INSERT INTO users(name, email, login, birthday)" +
                "VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            checkNullNameAndSetName(ps, user);
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getLogin());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        int id = Objects.requireNonNull(keyHolder.getKey()).intValue();

        return getUserById(id);
    }

    @Override
    public User updateUser(User user) {
        String sqlQuery = "UPDATE users SET " +
                "name = ?, email = ?, login = ?, birthday = ? " +
                "WHERE id = ?";

        jdbcTemplate.update(sqlQuery
                , user.getName()
                , user.getEmail()
                , user.getLogin()
                , user.getBirthday()
                , user.getId());
        return getUserById(user.getId());
    }

    @Override
    public List<User> getUsers() {
        String sqlQuery = "SELECT * " +
                "FROM users";

        return jdbcTemplate.query(sqlQuery, new UserRowMapper(jdbcTemplate, mpaService, genreService, directorService));
    }

    @Override
    public void removeUser (Integer id) {
        String sqlQuery = "DELETE " +
                "FROM users " +
                "WHERE id = ?";

        log.info("Удален пользователь под id: {}", id);
        jdbcTemplate.update(sqlQuery, id);
    }

    @Override
    public User addFriend(Integer userId, Integer friendId) {

        String sqlQueryInsertFriend = "MERGE INTO users_friends(id_user_one, id_user_two)" +
                "VALUES (?, ?)";

        jdbcTemplate.update(sqlQueryInsertFriend, userId, friendId);
        log.info("У пользоателя с id: {} добавлен новый друг с id: {} ", userId, friendId);
        return getUserById(userId);
    }

    @Override
    public Set<User> getFriendsById(Integer userId) {
        getUserById(userId);

        String sqlQuery = "SELECT * " +
                "FROM users " +
                "WHERE id IN" +
                "(" +
                "SELECT id_user_two " +
                "FROM users_friends " +
                "WHERE id_user_one = ?" +
                ")";


        return new HashSet<>(jdbcTemplate.query(sqlQuery, new UserRowMapper(jdbcTemplate, mpaService, genreService, directorService), userId));
    }

    @Override
    public User removeFriend(Integer userId, Integer friendId) {
        String sqlQuery = "DELETE " +
                "FROM users_friends " +
                "WHERE id_user_one = ? AND id_user_two = ?";

        jdbcTemplate.update(sqlQuery, userId, friendId);
        log.info("У пользователя с id: {} удален друг с id: {} ", userId, friendId);
        return getUserById(userId);
    }

    @Override
    public Set<User> getCommonFriends(Integer userId, Integer otherId) {
        String sqlQuery = "SELECT * " +
                "FROM users " +
                "WHERE id IN" +
                "(" +
                "SELECT DISTINCT uf1.id_user_two " +
                "FROM users_friends AS uf1, users_friends AS uf2 " +
                "WHERE uf1.id_user_two = uf2.id_user_two " +
                "AND uf1.id_user_one = ? " +
                "AND uf2.id_user_one = ? " +
                ")";

        return new HashSet<>(jdbcTemplate.query(sqlQuery, new UserRowMapper(jdbcTemplate, mpaService, genreService, directorService), userId, otherId));
    }

    private void checkNullNameAndSetName(PreparedStatement ps, User user) throws SQLException {
        if (user.getName() == null || user.getName().isBlank()) {
            ps.setString(1, user.getLogin());
        } else {
            ps.setString(1, user.getName());
        }
    }

    public List<Integer> getUserFriendsIds(Integer userId) {
        String sqlQuery = "SELECT id_user_two " +
                "FROM users_friends " +
                "WHERE id_user_one = ?";

        return jdbcTemplate.queryForList(sqlQuery, Integer.class, userId);
    }

    public List<Film> getRecommendations(Integer userId) {
        return jdbcTemplate.query(SQLRequests.GET_RECOMMENDATIONS,
                new FilmRowMapper(mpaService, genreService, directorService, jdbcTemplate),
                userId, userId, userId);
    }
}
