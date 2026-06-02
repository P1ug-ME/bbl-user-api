package com.bbl.userapi.service;

import com.bbl.userapi.dto.UserRequest;
import com.bbl.userapi.dto.UserUpdateRequest;
import com.bbl.userapi.exception.UserNotFoundException;
import com.bbl.userapi.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory user store. Backed by a ConcurrentHashMap so it is safe under the
 * servlet container's request threads. Seeded with sample data on startup.
 */
@Service
public class UserService {

    private final Map<Long, User> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public UserService() {
        seed();
    }

    private void seed() {
        save(new User(null, "สมชาย ใจดี", "somchai", "somchai@example.co.th",
                "081-234-5678", "somchai.co.th"));
        save(new User(null, "สุดา รักเรียน", "suda", "suda@example.co.th",
                "089-876-5432", "suda.co.th"));
        save(new User(null, "อานนท์ สุขใจ", "anan", "anan@example.co.th",
                "02-123-4567", "anan.in.th"));
    }

    public List<User> findAll() {
        return new ArrayList<>(store.values());
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    /** Returns the user or throws {@link UserNotFoundException}. */
    public User getOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public User create(UserRequest request) {
        User user = new User(
                sequence.incrementAndGet(),
                request.getName(),
                request.getUsername(),
                request.getEmail(),
                request.getPhone(),
                request.getWebsite());
        store.put(user.getId(), user);
        return user;
    }

    /**
     * Partial update: only the fields present (non-null) in the request are applied;
     * omitted fields keep their current values.
     */
    public User update(Long id, UserUpdateRequest request) {
        User user = getOrThrow(id);
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getWebsite() != null) {
            user.setWebsite(request.getWebsite());
        }
        return user;
    }

    public void delete(Long id) {
        if (store.remove(id) == null) {
            throw new UserNotFoundException(id);
        }
    }

    private User save(User user) {
        Long id = sequence.incrementAndGet();
        user.setId(id);
        store.put(id, user);
        return user;
    }
}
