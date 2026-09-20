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
	void laptopYamlMarksNacosOptionalAndRuntimeRequiresIt() throws Exception {
		String laptop = Files.readString(Path.of("src/main/resources/application.yaml"));
		assertThat(laptop).contains("optional:nacos:minimart-common.yaml?group=MINIMART");
		String runtime = Files.readString(Path.of("src/main/resources/application-runtime.yaml"));
		assertThat(runtime).doesNotContain("optional:nacos");
	}
}
