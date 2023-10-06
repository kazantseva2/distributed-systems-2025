package ru.msu.cs.nosql.nosqlapp.rest;

import org.springframework.web.bind.annotation.*;
import ru.msu.cs.nosql.nosqlapp.User;
import ru.msu.cs.nosql.nosqlapp.repository.UserRepository;

import java.util.List;

@RestController
public class UserController {

    private UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/user")
    public List<User> listAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/user/{id}")
    public User getUserById(@PathVariable("id") Long id) {
        return userRepository.findById(id);
    }

    @PostMapping("/user")
    public User saveUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    @DeleteMapping("/user/{id}")
    public void deleteUser(@PathVariable("id") Long id) {
        userRepository.deleteUser(id);
    }
}
