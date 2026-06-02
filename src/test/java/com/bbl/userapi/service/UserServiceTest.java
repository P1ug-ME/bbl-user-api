package com.bbl.userapi.service;

import com.bbl.userapi.dto.UserRequest;
import com.bbl.userapi.exception.UserNotFoundException;
import com.bbl.userapi.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceTest {

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService();
    }

    @Test
    void findAll_startsEmpty() {
        assertThat(service.findAll()).isEmpty();
    }

    @Test
    void create_assignsIdAndStoresUser() {
        User created = service.create(newRequest("Ada Lovelace", "ada", "ada@example.com"));

        assertThat(created.getId()).isEqualTo(1L);
        assertThat(service.findAll()).hasSize(1);
        assertThat(service.getOrThrow(created.getId()).getName()).isEqualTo("Ada Lovelace");
    }

    @Test
    void create_incrementsIdForEachUser() {
        User first = service.create(newRequest("First", "first", "first@example.com"));
        User second = service.create(newRequest("Second", "second", "second@example.com"));

        assertThat(first.getId()).isEqualTo(1L);
        assertThat(second.getId()).isEqualTo(2L);
        assertThat(service.findAll()).hasSize(2);
    }

    @Test
    void getOrThrow_unknownId_throwsNotFound() {
        assertThatThrownBy(() -> service.getOrThrow(9999L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void update_existingUser_overwritesFields() {
        Long id = service.create(newRequest("Old Name", "old", "old@example.com")).getId();

        User updated = service.update(id, newRequest("New Name", "newuser", "new@example.com"));

        assertThat(updated.getId()).isEqualTo(id);
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
        Long id = service.create(newRequest("To Delete", "todelete", "del@example.com")).getId();

        service.delete(id);

        assertThat(service.findById(id)).isEmpty();
        assertThat(service.findAll()).isEmpty();
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
