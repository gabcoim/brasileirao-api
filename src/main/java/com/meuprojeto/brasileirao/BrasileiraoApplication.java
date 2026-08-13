package com.meuprojeto.brasileirao;

import com.meuprojeto.brasileirao.config.ApiFootballProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ApiFootballProperties.class)
public class BrasileiraoApplication {
	public static void main(String[] args) {
		SpringApplication.run(BrasileiraoApplication.class, args);
	}
}
