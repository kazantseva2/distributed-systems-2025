package ru.msu.cs.nosql.nosqlapp.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.msu.cs.nosql.nosqlapp.User;

@RestController
public class HelloController {
    @GetMapping("/")
    public User hello(@RequestParam("name") String name) {
        return new User(1L, name);
    }
}
