package ru.yandex.practicum.filmorate.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.models.User;
import ru.yandex.practicum.filmorate.services.DirectorService;
import ru.yandex.practicum.filmorate.services.GenreService;
import ru.yandex.practicum.filmorate.services.MpaService;
import ru.yandex.practicum.filmorate.storage.daoImpl.DaoUserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

@RequiredArgsConstructor
public class UserRowMapper implements RowMapper<User> {
    private final JdbcTemplate jdbcTemplate;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final DirectorService directorService;

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .friends(new HashSet<>(new DaoUserStorage(jdbcTemplate, mpaService, genreService, directorService).getUserFriendsIds(rs.getInt("id"))))
                .build();

    }
}
