package br.com.threadstech.stockfy.users.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {

	Optional<UserJpaEntity> findByEmail(String email);

	Optional<UserJpaEntity> findByResetPasswordCodeHash(String resetPasswordCodeHash);

	@Query("SELECT u FROM UserJpaEntity u "
			+ "WHERE (:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')))")
	List<UserJpaEntity> findAllByName(@Param("name") String name);

	Page<UserJpaEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

}
