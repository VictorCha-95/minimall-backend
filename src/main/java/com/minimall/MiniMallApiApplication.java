package com.minimall;

import com.minimall.auth.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class MiniMallApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MiniMallApiApplication.class, args);
	}

}
