package com.bbl.userapi.controller;

import com.bbl.userapi.UserApiApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = UserApiApplication.class)
class UserControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void getAllUsers_returnsSeededList() throws Exception {
        mockMvc().perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[0].username").exists());
    }

    @Test
    void getUser_existing_returnsUser() throws Exception {
        mockMvc().perform(get("/users/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Leanne Graham"));
    }

    @Test
    void getUser_unknown_returns404() throws Exception {
        mockMvc().perform(get("/users/{id}", 9999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createUser_valid_returns201WithLocation() throws Exception {
        Map<String, String> body = Map.of(
                "name", "Ada Lovelace",
                "username", "ada",
                "email", "ada@example.com");

        mockMvc().perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Ada Lovelace"));
    }

    @Test
    void createUser_missingRequiredFields_returns400() throws Exception {
        Map<String, String> body = Map.of("phone", "123");

        mockMvc().perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void createUser_invalidEmail_returns400() throws Exception {
        Map<String, String> body = Map.of(
                "name", "Bad Email",
                "username", "bad",
                "email", "not-an-email");

        mockMvc().perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_unknown_returns404() throws Exception {
        Map<String, String> body = Map.of(
                "name", "X",
                "username", "x",
                "email", "x@example.com");

        mockMvc().perform(put("/users/{id}", 9999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_existing_returns204() throws Exception {
        // create first so we don't depend on seed ordering across tests
        Map<String, String> body = Map.of(
                "name", "To Delete",
                "username", "todelete",
                "email", "del@example.com");

        String response = mockMvc().perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc().perform(delete("/users/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc().perform(get("/users/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_unknown_returns404() throws Exception {
        mockMvc().perform(delete("/users/{id}", 9999))
                .andExpect(status().isNotFound());
    }
}
