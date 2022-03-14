package ru.ifmo.calculator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ifmo.calculator.entities.Result;

public interface ResultRepository  extends JpaRepository<Result, Long> {
}
