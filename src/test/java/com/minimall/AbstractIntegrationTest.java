package com.minimall;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("integration-test")
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("minimall")
                    .withUsername("test")
                    .withPassword("test")
                    .withInitScript("db/schema-mysql.sql");
}
