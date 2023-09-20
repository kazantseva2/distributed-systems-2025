package ru.msu.cs.nosql.nosqlapp.rest;

import org.springframework.web.bind.annotation.*;
import ru.msu.cs.nosql.nosqlapp.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    private Map<Long, User> users = new HashMap<>();
    private long id = 0;

    @GetMapping("/user")
    public List<User> listAllUsers() {
        return new ArrayList<>(users.values());
    }

    @GetMapping("/user/{id}")
    public User getUserById(@PathVariable("id") Long id) {
        return users.get(id);
    }

    @PostMapping("/user")
    public User saveUser(@RequestBody User user) {
        if (user.getId() == null) {
            user.setId(id++);
        }
        users.put(user.getId(), user);
        return user;
    }

    @DeleteMapping("/user/{id}")
    public void deleteUser(@PathVariable("id") Long id) {
        users.remove(id);
    }
}
