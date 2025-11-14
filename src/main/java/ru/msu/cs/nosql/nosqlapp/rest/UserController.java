package ru.msu.cs.nosql.nosqlapp.rest;

import org.springframework.web.bind.annotation.*;
import ru.msu.cs.nosql.nosqlapp.model.User;
import ru.msu.cs.nosql.nosqlapp.repository.UserRepository;

import java.util.List;

@RestController
public class UserController {

    private UserRepository userRepository;

    //private ElasticUserRepository elasticUserRepository;


//    public UserController(UserRepository userRepository, ElasticUserRepository elasticUserRepository) {
//        this.userRepository = userRepository;
//        this.elasticUserRepository = elasticUserRepository;
//    }

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/user")
    public List<User> listAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/user/{id}")
    public User getUserById(@PathVariable("id") String id) {
        return userRepository.findById(id);
    }

    @PostMapping("/user")
    public User saveUser(@RequestBody User user) {
        User savedUser = userRepository.save(user);
       // elasticUserRepository.save(savedUser);
        return savedUser;
    }

    @DeleteMapping("/user/{id}")
    public void deleteUser(@PathVariable("id") String id) {
        userRepository.deleteUser(id);
    }

//    @GetMapping("/user/search")
//    public List<User> findByName(@RequestParam("name") String name) {
//        return elasticUserRepository.findByName(name);
//    }
}
