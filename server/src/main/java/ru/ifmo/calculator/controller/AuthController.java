package ru.ifmo.calculator.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.ifmo.calculator.data.UserInDto;
import ru.ifmo.calculator.entities.User;
import ru.ifmo.calculator.service.RoleService;
import ru.ifmo.calculator.service.UserService;

import java.security.Principal;
import java.util.Collections;

@RestController
public class AuthController {
    private final UserService userService;
    private final RoleService roleService;

    public AuthController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserInDto userInDto) {
        if (userService.findByUsername(userInDto.getUsername()) != null) {
            return new ResponseEntity<>(
                    "User with username " + userInDto.getUsername() + " already exist",
                    HttpStatus.CONFLICT
            );
        }

        User user = new User();
        user.setUsername(userInDto.getUsername());
        user.setPassword(userInDto.getPassword());
        user.setRoles(Collections.singletonList(roleService.findByName("ROLE_USER")));

        userService.save(user);

        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }

    @GetMapping("/login")
    public ResponseEntity<?> auth(Principal principal) {
        User user = userService.findByUsername(principal.getName());

        if (user == null)
            return new ResponseEntity<>("Username or password incorrect", HttpStatus.UNAUTHORIZED);

        return new ResponseEntity<>(null, HttpStatus.OK);
    }
}
