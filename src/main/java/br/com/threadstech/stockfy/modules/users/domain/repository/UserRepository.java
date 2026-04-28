package br.com.threadstech.stockfy.modules.users.domain.repository;

import br.com.threadstech.stockfy.modules.users.domain.model.Email;
import br.com.threadstech.stockfy.modules.users.domain.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository {
  void save(User user);

  Optional<User> findById(UUID id);

  Optional<User> findByEmail(Email email);

  List<User> findAll(String nameFilter);

  Page<User> findAll(String nameFilter, Pageable pageable);

  void update(User user);
}
