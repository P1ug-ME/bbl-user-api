package com.bbl.userapi.service;

import com.bbl.userapi.dto.UserRequest;
import com.bbl.userapi.dto.UserUpdateRequest;
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
                .contains("somchai", "suda", "anan");
    }

    @Test
    void create_assignsNextIdAndStoresUser() {
        User created = service.create(newRequest("ใหม่ ทดสอบ", "newuser", "new@example.co.th"));

        assertThat(created.getId()).isEqualTo(4L);
        assertThat(service.findAll()).hasSize(4);
        assertThat(service.getOrThrow(created.getId()).getName()).isEqualTo("ใหม่ ทดสอบ");
    }

    @Test
    void getOrThrow_unknownId_throwsNotFound() {
        assertThatThrownBy(() -> service.getOrThrow(9999L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void update_onlyChangesProvidedField_leavesOthersUntouched() {
        User before = service.getOrThrow(1L);
        String originalName = before.getName();
        String originalEmail = before.getEmail();

        // Change only the phone field.
        UserUpdateRequest req = new UserUpdateRequest();
        req.setPhone("099-999-9999");

        User updated = service.update(1L, req);

        assertThat(updated.getId()).isEqualTo(1L);
        assertThat(updated.getPhone()).isEqualTo("099-999-9999");
        assertThat(updated.getName()).isEqualTo(originalName);
        assertThat(updated.getEmail()).isEqualTo(originalEmail);
    }

    @Test
    void update_multipleProvidedFields_areApplied() {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setName("แก้ไข แล้ว");
        req.setUsername("edited");

        User updated = service.update(1L, req);

        assertThat(updated.getName()).isEqualTo("แก้ไข แล้ว");
        assertThat(updated.getUsername()).isEqualTo("edited");
    }

    @Test
    void update_unknownUser_throwsNotFound() {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setName("X");
        assertThatThrownBy(() -> service.update(9999L, req))
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
