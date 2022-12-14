package ru.yandex.practicum.filmorate.controllers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.models.Mpa;
import ru.yandex.practicum.filmorate.services.MpaService;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/mpa")
public class MpaController {
    @Getter
    MpaService mpaService;

    @Autowired
    public MpaController(MpaService mpaService){
        this.mpaService = mpaService;
    }
    @GetMapping
    public List<Mpa> getAllMpa(){
        log.info("Запрос получения всех возрастных рейтигов фильмов");
        return mpaService.getAllMpa();
    }
    @GetMapping("/{id}")
    public Mpa getMpaById(@PathVariable("id") Integer id){
        log.info("Запрос получения возоастного рейтинга фильма с id: {} ", id);
        return mpaService.getMpaById(id);
    }


}
