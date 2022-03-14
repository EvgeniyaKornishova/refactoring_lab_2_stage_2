package ru.ifmo.calculator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ifmo.calculator.entities.Privilege;

public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {
    Privilege findByName(String name);
}
