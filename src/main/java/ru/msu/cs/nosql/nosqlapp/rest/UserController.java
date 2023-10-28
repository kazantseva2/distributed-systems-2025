package ru.msu.cs.nosql.nosqlapp.rest;

import org.springframework.web.bind.annotation.*;
import ru.msu.cs.nosql.nosqlapp.User;
import ru.msu.cs.nosql.nosqlapp.repository.ElasticUserRepository;
import ru.msu.cs.nosql.nosqlapp.repository.UserRepository;

import java.util.List;

@RestController
public class UserController {

    private UserRepository userRepository;

    private ElasticUserRepository elasticUserRepository;

    public UserController(UserRepository userRepository, ElasticUserRepository elasticUserRepository) {
        this.userRepository = userRepository;
        this.elasticUserRepository = elasticUserRepository;
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
        User savedUser = userRepository.save(user);
        elasticUserRepository.save(savedUser);
        return savedUser;
    }

    @DeleteMapping("/user/{id}")
    public void deleteUser(@PathVariable("id") Long id) {
        userRepository.deleteUser(id);
    }

    @GetMapping("/user/search")
    public List<User> findByName(@RequestParam("name") String name) {
        return elasticUserRepository.findByName(name);
    }
}
