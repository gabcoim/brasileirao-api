package com.meuprojeto.brasileirao.service;

import com.meuprojeto.brasileirao.model.Time;
import com.meuprojeto.brasileirao.repository.TimeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TimeService {

    private final TimeRepository timeRepository;

    public TimeService(TimeRepository timeRepository) {
        this.timeRepository = timeRepository;
    }

    public List<Time> listarTimes() {
        return timeRepository.findAll();
    }
}
