package com.meuprojeto.brasileirao.controller;

import com.meuprojeto.brasileirao.model.Time;
import com.meuprojeto.brasileirao.service.TimeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/times")
public class TimeController {

    private final TimeService timeService;

    public TimeController(TimeService timeService) {
        this.timeService = timeService;
    }

    @GetMapping
    public List<Time> listar() {
        return timeService.listarTimes();
    }
}
