package br.com.threadstech.stockfy.modules.users.domain.repository;

import br.com.threadstech.stockfy.modules.users.domain.model.User;
import br.com.threadstech.stockfy.modules.users.domain.model.Email;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface UserRepository {
    void save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(Email email);
    List<User> findAll(String nameFilter);
    void update(User user);
}
