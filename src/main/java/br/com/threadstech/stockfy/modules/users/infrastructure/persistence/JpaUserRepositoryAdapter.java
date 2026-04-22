package br.com.threadstech.stockfy.modules.users.infrastructure.persistence;

import br.com.threadstech.stockfy.modules.users.domain.model.Email;
import br.com.threadstech.stockfy.modules.users.domain.model.User;
import br.com.threadstech.stockfy.modules.users.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepository {

  private final SpringDataUserRepository repository;
  private final UserPersistenceMapper mapper;

  @Override
  public void save(User user) {
    repository.save(mapper.toEntity(user));
  }

  @Override
  public Optional<User> findById(UUID id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Optional<User> findByEmail(Email email) {
    return repository.findByEmail(email.value()).map(mapper::toDomain);
  }

  @Override
  public List<User> findAll(String nameFilter) {
    return repository.findAllByName(nameFilter).stream().map(mapper::toDomain).toList();
  }

  @Override
  public void update(User user) {
    repository.save(mapper.toEntity(user));
  }
}
