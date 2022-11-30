package ru.yandex.practicum.filmorate.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import ru.yandex.practicum.filmorate.validators.DateReleaseConstraint;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Film {

    private int id;

    @NotBlank
    @NotNull(message = "Неверные данные: имя пустое или содержит только пробелы")
    private String name;

    @Size(max=200,
            message = "Неверные данные: Описание больше 200 символов")
    private String description;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @DateReleaseConstraint
    private LocalDate releaseDate;

    @Min(value = 0
            , message = "Неверные данные: Длительность меньше 0")
    private int duration;

    private Double rate;
    private Mpa mpa;
    private List<Genre> genres;
    private List<Director> directors;
}
