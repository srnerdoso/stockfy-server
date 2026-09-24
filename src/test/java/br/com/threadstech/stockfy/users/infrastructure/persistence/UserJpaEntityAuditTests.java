/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.threadstech.stockfy.users.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import br.com.threadstech.stockfy.ContainersConfiguration;
import br.com.threadstech.stockfy.users.domain.model.UserStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(ContainersConfiguration.class)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/audit-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UserJpaEntityAuditTests {

	private static final UUID AUDIT_USER_ID = UUID.fromString("a0000000-0000-0000-0000-000000000001");

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private SpringDataUserRepository userRepository;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Test
	@DisplayName("Criação de usuário não deve persistir dados sensíveis na auditoria")
	void save_whenUserCreated_thenSensitiveDataNotInAudit() {
		UUID userId = UUID.randomUUID();

		// Envers grava auditoria no commit — precisa de transação real
		TransactionTemplate tx = new TransactionTemplate(this.transactionManager);
		tx.executeWithoutResult((status) -> {
			UserJpaEntity user = UserJpaEntity.builder()
				.id(userId)
				.name("New User")
				.email("new.user@example.com")
				.passwordHash("hashed-password-value")
				.status(UserStatus.ACTIVE)
				.active(true)
				.build();
			this.userRepository.saveAndFlush(user);
		});

		// Leitura da auditoria em transação separada
		tx.executeWithoutResult((status) -> {
			List<?> revisions = AuditReaderFactory.get(this.entityManager)
				.createQuery()
				.forRevisionsOfEntity(UserJpaEntity.class, true, true)
				.add(AuditEntity.id().eq(userId))
				.getResultList();

			assertThat(revisions).isNotEmpty();

			for (Object revision : revisions) {
				UserJpaEntity auditedEntity = (UserJpaEntity) revision;
				assertThat(auditedEntity.getEmail()).isNull();
				assertThat(auditedEntity.getPasswordHash()).isNull();
				assertThat(auditedEntity.getResetPasswordCodeHash()).isNull();
				assertThat(auditedEntity.getResetPasswordExpiresAt()).isNull();
			}
		});
	}

	@Test
	@DisplayName("Atualização de usuário não deve persistir dados sensíveis na auditoria")
	void save_whenUserUpdated_thenSensitiveDataNotInAudit() {
		TransactionTemplate tx = new TransactionTemplate(this.transactionManager);

		// Atualização com commit real para Envers registrar
		tx.executeWithoutResult((status) -> {
			UserJpaEntity user = this.userRepository.findById(AUDIT_USER_ID).orElseThrow();
			user.setName("Updated Name");
			user.setEmail("updated.email@example.com");
			user.setPasswordHash("new-password-hash");
			this.userRepository.saveAndFlush(user);
		});

		// Leitura da auditoria em transação separada
		tx.executeWithoutResult((status) -> {
			List<?> revisions = AuditReaderFactory.get(this.entityManager)
				.createQuery()
				.forRevisionsOfEntity(UserJpaEntity.class, true, true)
				.add(AuditEntity.id().eq(AUDIT_USER_ID))
				.getResultList();

			assertThat(revisions).isNotEmpty();

			for (Object revision : revisions) {
				UserJpaEntity auditedEntity = (UserJpaEntity) revision;
				assertThat(auditedEntity.getEmail()).isNull();
				assertThat(auditedEntity.getPasswordHash()).isNull();
				assertThat(auditedEntity.getResetPasswordCodeHash()).isNull();
				assertThat(auditedEntity.getResetPasswordExpiresAt()).isNull();
			}
		});
	}

	@Test
	@DisplayName("Soft delete de usuário não deve manter dados sensíveis acessíveis na auditoria")
	void delete_whenUserSoftDeleted_thenSensitiveDataNotInAudit() {
		TransactionTemplate tx = new TransactionTemplate(this.transactionManager);

		// Soft delete com commit real para Envers registrar
		tx.executeWithoutResult((status) -> {
			this.userRepository.deleteById(AUDIT_USER_ID);
			this.userRepository.flush();
		});

		// Leitura da auditoria em transação separada
		tx.executeWithoutResult((status) -> {
			List<?> revisions = AuditReaderFactory.get(this.entityManager)
				.createQuery()
				.forRevisionsOfEntity(UserJpaEntity.class, true, true)
				.add(AuditEntity.id().eq(AUDIT_USER_ID))
				.getResultList();

			assertThat(revisions).isNotEmpty();

			for (Object revision : revisions) {
				UserJpaEntity auditedEntity = (UserJpaEntity) revision;
				assertThat(auditedEntity.getEmail()).isNull();
				assertThat(auditedEntity.getPasswordHash()).isNull();
				assertThat(auditedEntity.getResetPasswordCodeHash()).isNull();
				assertThat(auditedEntity.getResetPasswordExpiresAt()).isNull();
			}
		});
	}

	@Test
	@DisplayName("Auditoria deve manter campos não sensíveis após operações de CRUD")
	void save_whenUserCreated_thenNonSensitiveDataInAudit() {
		UUID userId = UUID.randomUUID();
		TransactionTemplate tx = new TransactionTemplate(this.transactionManager);

		// Criação com commit real para Envers registrar
		tx.executeWithoutResult((status) -> {
			UserJpaEntity user = UserJpaEntity.builder()
				.id(userId)
				.name("Audit Check User")
				.email("audit.check@example.com")
				.passwordHash("hashed-pw")
				.status(UserStatus.ACTIVE)
				.active(true)
				.build();
			this.userRepository.saveAndFlush(user);
		});

		// Leitura da auditoria em transação separada
		tx.executeWithoutResult((status) -> {
			List<?> revisions = AuditReaderFactory.get(this.entityManager)
				.createQuery()
				.forRevisionsOfEntity(UserJpaEntity.class, true, true)
				.add(AuditEntity.id().eq(userId))
				.getResultList();

			assertThat(revisions).isNotEmpty();

			UserJpaEntity auditedEntity = (UserJpaEntity) revisions.get(0);
			assertThat(auditedEntity.getName()).isEqualTo("Audit Check User");
			assertThat(auditedEntity.getStatus()).isEqualTo(UserStatus.ACTIVE);
			assertThat(auditedEntity.isActive()).isTrue();
		});
	}

}
