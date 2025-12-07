package br.com.threadstech.stockfy.security.refreshtoken;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  
  Optional<RefreshToken> findByToken(UUID token);

  boolean existsByEmployeeIdAndToken(Long employeeId, UUID token);
}
