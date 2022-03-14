package ru.ifmo.calculator.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.ifmo.calculator.entities.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}
