package com.ztiproject.shoppingassistant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

import static java.util.Comparator.comparing;

@RestController
public class UserController {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users/{username}")
    public Mono<User> get(@PathVariable() String username) {
        return this.users.findByUsername(username);
    }

    @PostMapping("/users/{username}/{password}")
    public Mono<User> create(@PathVariable() String username, @PathVariable() String password) {

    List<String> roles = Arrays.asList("ROLE_USER");

    User user = User.builder()
        .roles(roles)
        .username(username)
        .password(passwordEncoder.encode(password))
        .email(username + "@example.com")
        .build();

    return this.users.save(user);
    }
}