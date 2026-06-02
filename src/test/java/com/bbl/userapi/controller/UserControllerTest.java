package com.bbl.userapi.controller;

import com.bbl.userapi.UserApiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

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

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context).build();
    }

    /** POST a user and return the resource path from the Location header (e.g. "/users/4"). */
    private String createUser(String name, String username, String email) throws Exception {
        String body = """
                {"name":"%s","username":"%s","email":"%s"}
                """.formatted(name, username, email);
        String location = mockMvc().perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        return location.substring(location.indexOf("/users"));
    }

    @Test
    void getAllUsers_returnsJsonList() throws Exception {
        createUser("List One", "listone", "listone@example.com");

        mockMvc().perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].username").exists());
    }

    @Test
    void getUser_existing_returnsUser() throws Exception {
        String path = createUser("Read Me", "readme", "readme@example.com");

        mockMvc().perform(get(path))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Read Me"))
                .andExpect(jsonPath("$.username").value("readme"));
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
        String body = """
                {"name":"Ada Lovelace","username":"ada","email":"ada@example.com"}
                """;

        mockMvc().perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Ada Lovelace"));
    }

    @Test
    void createUser_missingRequiredFields_returns400() throws Exception {
        String body = """
                {"phone":"123"}
                """;

        mockMvc().perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void createUser_invalidEmail_returns400() throws Exception {
        String body = """
                {"name":"Bad Email","username":"bad","email":"not-an-email"}
                """;

        mockMvc().perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_existing_returns200() throws Exception {
        String path = createUser("Before", "before", "before@example.com");

        String body = """
                {"name":"After","username":"after","email":"after@example.com"}
                """;

        mockMvc().perform(put(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("After"))
                .andExpect(jsonPath("$.username").value("after"));
    }

    @Test
    void updateUser_unknown_returns404() throws Exception {
        String body = """
                {"name":"X","username":"x","email":"x@example.com"}
                """;

        mockMvc().perform(put("/users/{id}", 9999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_existing_returns204() throws Exception {
        String path = createUser("To Delete", "todelete", "del@example.com");

        mockMvc().perform(delete(path))
                .andExpect(status().isNoContent());

        mockMvc().perform(get(path))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_unknown_returns404() throws Exception {
        mockMvc().perform(delete("/users/{id}", 9999))
                .andExpect(status().isNotFound());
    }
}
