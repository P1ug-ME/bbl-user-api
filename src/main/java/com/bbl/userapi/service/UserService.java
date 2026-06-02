package com.bbl.userapi.service;

import com.bbl.userapi.dto.UserRequest;
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
        save(new User(null, "Leanne Graham", "Bret", "Sincere@april.biz",
                "1-770-736-8031 x56442", "hildegard.org"));
        save(new User(null, "Ervin Howell", "Antonette", "Shanna@melissa.tv",
                "010-692-6593 x09125", "anastasia.net"));
        save(new User(null, "Clementine Bauch", "Samantha", "Nathan@yesenia.net",
                "1-463-123-4447", "ramiro.info"));
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

    public User update(Long id, UserRequest request) {
        User user = getOrThrow(id);
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setWebsite(request.getWebsite());
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
