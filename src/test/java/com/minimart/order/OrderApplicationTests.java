package com.minimart.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootTest
class OrderApplicationTests {

	@Test
	void enablesPublishedFeignClients() {
		EnableFeignClients enable = OrderApplication.class.getAnnotation(EnableFeignClients.class);
		assertThat(enable).isNotNull();
		assertThat(enable.clients()).hasSize(3);
	}

	@Test
	void applicationYamlDeclaresFeignServiceUrlsWithoutNacos() throws Exception {
		String yaml = Files.readString(Path.of("src/main/resources/application.yaml"));
		assertThat(yaml).doesNotContain("nacos");
		assertThat(yaml).contains("product-service");
		assertThat(yaml).contains("member-service");
		assertThat(yaml).contains("payment-service");
		assertThat(Path.of("src/main/resources/application-runtime.yaml")).doesNotExist();
	}
}
