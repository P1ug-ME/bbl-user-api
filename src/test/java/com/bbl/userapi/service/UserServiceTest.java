package com.bbl.userapi.service;

import com.bbl.userapi.dto.UserRequest;
import com.bbl.userapi.exception.UserNotFoundException;
import com.bbl.userapi.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceTest {

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService();
    }

    @Test
    void findAll_returnsSeededUsers() {
        List<User> users = service.findAll();
        assertThat(users).hasSize(3);
        assertThat(users).extracting(User::getUsername)
                .contains("Bret", "Antonette", "Samantha");
    }

    @Test
    void create_assignsIdAndStoresUser() {
        UserRequest request = newRequest("Ada Lovelace", "ada", "ada@example.com");

        User created = service.create(request);

        assertThat(created.getId()).isNotNull();
        assertThat(service.findAll()).hasSize(4);
        assertThat(service.getOrThrow(created.getId()).getName()).isEqualTo("Ada Lovelace");
    }

    @Test
    void getOrThrow_unknownId_throwsNotFound() {
        assertThatThrownBy(() -> service.getOrThrow(9999L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void update_existingUser_overwritesFields() {
        User updated = service.update(1L, newRequest("New Name", "newuser", "new@example.com"));

        assertThat(updated.getId()).isEqualTo(1L);
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getUsername()).isEqualTo("newuser");
    }

    @Test
    void update_unknownUser_throwsNotFound() {
        assertThatThrownBy(() -> service.update(9999L, newRequest("X", "x", "x@example.com")))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void delete_existingUser_removesIt() {
        service.delete(1L);
        assertThat(service.findById(1L)).isEmpty();
        assertThat(service.findAll()).hasSize(2);
    }

    @Test
    void delete_unknownUser_throwsNotFound() {
        assertThatThrownBy(() -> service.delete(9999L))
                .isInstanceOf(UserNotFoundException.class);
    }

    private UserRequest newRequest(String name, String username, String email) {
        UserRequest r = new UserRequest();
        r.setName(name);
        r.setUsername(username);
        r.setEmail(email);
        return r;
    }
}
