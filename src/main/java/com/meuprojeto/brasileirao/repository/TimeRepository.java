package com.meuprojeto.brasileirao.repository;

import com.meuprojeto.brasileirao.model.Time;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TimeRepository extends JpaRepository<Time, Long> {

    Optional<Time> findByIdExterno(Long idExterno);
}
