package ru.msu.cs.nosql.nosqlapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.msu.cs.nosql.nosqlapp.model.User;
import ru.msu.cs.nosql.nosqlapp.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        userRepository.deleteAll(); // чистим коллекцию перед каждым тестом
    }

    @Test
    void testCreateUserAndGetById() throws Exception {
        User user = new User(null, "Alice", 0);

        // Сохраняем пользователя
        String userJson = objectMapper.writeValueAsString(user);
        String response = mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        User savedUser = objectMapper.readValue(response, User.class);

        // Получаем пользователя по ID
        mockMvc.perform(get("/user/" + savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void testListAllUsers() throws Exception {
        userRepository.save(new User(null, "Bob", 0));
        userRepository.save(new User(null, "Charlie", 0));

        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testDeleteUser() throws Exception {
        User user = userRepository.save(new User(null, "David", 0));

        mockMvc.perform(delete("/user/" + user.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/user/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }
}
