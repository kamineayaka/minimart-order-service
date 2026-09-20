package com.minimart.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

import com.minimart.api.autoconfigure.MinimartFeignConfiguration;
import com.minimart.member.api.MemberAddressClient;
import com.minimart.payment.api.PaymentClient;
import com.minimart.product.api.ProductStockClient;

@SpringBootApplication
@Import(MinimartFeignConfiguration.class)
@EnableFeignClients(clients = { ProductStockClient.class, MemberAddressClient.class, PaymentClient.class })
public class OrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderApplication.class, args);
	}

}
