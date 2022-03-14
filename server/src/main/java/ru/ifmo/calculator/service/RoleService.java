package ru.ifmo.calculator.service;

import org.springframework.stereotype.Service;
import ru.ifmo.calculator.entities.Role;
import ru.ifmo.calculator.repository.RoleRepository;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role findByName(String name) {
        return roleRepository.findByName(name);
    }
}
