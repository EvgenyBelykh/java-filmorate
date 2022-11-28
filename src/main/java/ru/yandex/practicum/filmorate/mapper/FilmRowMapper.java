package ru.yandex.practicum.filmorate.mapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.services.DirectorService;
import ru.yandex.practicum.filmorate.services.GenreService;
import ru.yandex.practicum.filmorate.services.MpaService;
import ru.yandex.practicum.filmorate.storage.daoImpl.DaoFilmStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

@Data
@AllArgsConstructor
public class FilmRowMapper implements RowMapper<Film> {
    private final MpaService mpaService;
    private final GenreService genreService;
    private final DirectorService directorService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Film.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .likes(new HashSet<>(new DaoFilmStorage(jdbcTemplate).getLikesFromUserByFilmId(rs.getInt("id"))))
                .rate(rs.getInt("rate"))
                .mpa(mpaService.getMpaById(Integer.valueOf(rs.getString("mpa"))))
                .genres(genreService.getGenresByIdFilm(rs.getInt("id")))
                .directors(directorService.getDirectorsByIdFilm(rs.getInt("id")))
                .build();
    }
}
