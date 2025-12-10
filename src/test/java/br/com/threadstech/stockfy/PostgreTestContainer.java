package br.com.threadstech.stockfy;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgreTestContainer implements BeforeAllCallback {

  private static final AtomicBoolean containerStarted = new AtomicBoolean(false);

  @Container
  @ServiceConnection
  static PostgreSQLContainer postgre =
      new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    if (!containerStarted.get()) {
      postgre.start();
      containerStarted.set(true);
    }
  }
}
