package br.com.threadstech.stockfy.modules.users.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.threadstech.stockfy.TestcontainersConfiguration;
import br.com.threadstech.stockfy.modules.users.domain.model.Email;
import br.com.threadstech.stockfy.modules.users.domain.model.Password;
import br.com.threadstech.stockfy.modules.users.domain.model.User;
import br.com.threadstech.stockfy.modules.users.domain.model.UserRole;
import br.com.threadstech.stockfy.modules.users.domain.model.UserStatus;
import br.com.threadstech.stockfy.modules.users.domain.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class UserRepositoryIT {

  @Autowired private UserRepository userRepository;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  @DisplayName("Should find users by name filter")
  void shouldFindUsersByNameFilter() {
    userRepository.save(createUser("Alice", "alice@example.com"));
    userRepository.save(createUser("Bob", "bob@example.com"));

    var results = userRepository.findAll("Ali");
    assertEquals(1, results.size());
    assertEquals("Alice", results.get(0).getName());
  }

  @Test
  @DisplayName("Should update user status")
  void shouldUpdateUserStatus() {
    User user = createUser("Status Test", "status@example.com");
    userRepository.save(user);

    user.lock();
    userRepository.update(user);

    var updated = userRepository.findById(user.getId());
    assertTrue(updated.isPresent());
    assertEquals(UserStatus.LOCKED, updated.get().getStatus());
  }

  @Test
  @DisplayName("Deve persistir role ADMIN ao salvar usuário administrador")
  void save_whenUserRoleIsAdmin_thenPersistsAdminRole() {
    User user = createUser("Admin User", "admin-role@example.com", UserRole.ADMIN);
    userRepository.save(user);

    var persisted = userRepository.findByEmail(new Email("admin-role@example.com"));
    String persistedRole =
        jdbcTemplate.queryForObject(
            "SELECT role FROM users WHERE id = ?", String.class, user.getId());

    assertTrue(persisted.isPresent());
    assertEquals(UserRole.ADMIN, persisted.get().getRole());
    assertEquals("ADMIN", persistedRole);
  }

  private User createUser(String name, String email) {
    return createUser(name, email, UserRole.USER);
  }

  private User createUser(String name, String email, UserRole role) {
    return User.builder()
        .id(UUID.randomUUID())
        .name(name)
        .email(new Email(email))
        .password(new Password("password123"))
        .role(role)
        .status(UserStatus.ACTIVE)
        .active(true)
        .build();
  }
}
