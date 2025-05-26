package org.promsnmp.metrics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAsync
@ComponentScan(basePackages = { "org.promsnmp.metrics", "org.promsnmp.common" })
@EntityScan(basePackages = { "org.promsnmp.common.model" })
public class PromSnmp {

	public static void main(String[] args) {
		SpringApplication.run(PromSnmp.class, args);
	}

}
