package br.com.threadstech.stockfy.modules.users.domain.repository;

import br.com.threadstech.stockfy.modules.users.domain.model.Email;
import br.com.threadstech.stockfy.modules.users.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
  void save(User user);

  Optional<User> findById(UUID id);

  Optional<User> findByEmail(Email email);

  List<User> findAll(String nameFilter);

  void update(User user);
}
