package ru.msu.cs.nosql.nosqlapp.rest;

import org.springframework.web.bind.annotation.*;
import ru.msu.cs.nosql.nosqlapp.model.User;
import ru.msu.cs.nosql.nosqlapp.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<User> listAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable("id") String id) {
        return userRepository.findById(id);
    }

    @PostMapping
    public User saveUser(@RequestBody User user) {
        User savedUser = userRepository.save(user);
       // elasticUserRepository.save(savedUser);
        return savedUser;
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable("id") String id) {
        userRepository.deleteUser(id);
    }
}
