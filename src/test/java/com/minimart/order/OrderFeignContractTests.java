package com.minimart.order;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.minimart.api.error.ErrorCodes;
import com.minimart.api.feign.ApiStatusException;
import com.minimart.api.http.CorrelationHeaders;
import com.minimart.api.http.CorrelationIdHolder;
import com.minimart.api.http.IdempotencyHeaders;
import com.minimart.member.api.MemberAddressClient;
import com.minimart.payment.api.OpenCollectMoneyCommand;
import com.minimart.payment.api.PaymentClient;
import com.minimart.payment.api.PaymentStatus;
import com.minimart.product.api.ConfirmStockCommand;
import com.minimart.product.api.ProductStockClient;
import com.minimart.product.api.ReleaseStockCommand;
import com.minimart.product.api.ReserveStockCommand;
import com.minimart.product.api.StockLine;

@SpringBootTest
class OrderFeignContractTests {

	private static final WireMockServer WM = new WireMockServer(wireMockConfig().dynamicPort());

	static {
		WM.start();
	}

	@DynamicPropertySource
	static void feignUrls(DynamicPropertyRegistry registry) {
		String base = "http://localhost:" + WM.port();
		registry.add("spring.cloud.openfeign.client.config.product-service.url", () -> base);
		registry.add("spring.cloud.openfeign.client.config.member-service.url", () -> base);
		registry.add("spring.cloud.openfeign.client.config.payment-service.url", () -> base);
	}

	@Autowired
	ProductStockClient productStockClient;

	@Autowired
	MemberAddressClient memberAddressClient;

	@Autowired
	PaymentClient paymentClient;

	@BeforeEach
	void reset() {
		WM.resetAll();
		CorrelationIdHolder.clear();
	}

	@AfterAll
	static void stop() {
		WM.stop();
	}

	@Test
	void productStockContractKeepsCentsAndIdempotency() {
		CorrelationIdHolder.set("corr-stock");
		WM.stubFor(post(urlEqualTo("/internal/v1/stock/reservations"))
				.willReturn(okJson("""
						{"orderId":11,"lines":[{"skuId":9,"qty":2,"unitPriceCents":1999}]}
						""")));
		WM.stubFor(post(urlEqualTo("/internal/v1/stock/reservations/11/confirmation")).willReturn(aResponse().withStatus(204)));
		WM.stubFor(post(urlEqualTo("/internal/v1/stock/reservations/11/release")).willReturn(aResponse().withStatus(204)));

		var reserved = productStockClient.reserve("place-11", new ReserveStockCommand(11L, List.of(new StockLine(9L, 2))));
		assertThat(reserved.lines().getFirst().unitPriceCents()).isEqualTo(1999L);
		productStockClient.confirm(11L, "confirm-11", new ConfirmStockCommand(11L));
		productStockClient.release(11L, "release-11", new ReleaseStockCommand(11L));

		WM.verify(postRequestedFor(urlEqualTo("/internal/v1/stock/reservations"))
				.withHeader(IdempotencyHeaders.KEY, equalTo("place-11"))
				.withHeader(CorrelationHeaders.CORRELATION_ID, equalTo("corr-stock"))
				.withRequestBody(matchingJsonPath("$.lines[0].qty", equalTo("2"))));
	}

	@Test
	void productStockErrorBodyIsApiError() {
		WM.stubFor(post(urlEqualTo("/internal/v1/stock/reservations")).willReturn(aResponse().withStatus(409)
				.withHeader("Content-Type", "application/json")
				.withBody("""
						{"code":"STOCK_INSUFFICIENT","message":"last SKU already reserved","correlationId":"c","timestamp":"2026-09-20T12:00:00Z","fields":[]}
						""")));
		assertThatThrownBy(() -> productStockClient.reserve("place-12", new ReserveStockCommand(12L, List.of(new StockLine(9L, 1)))))
				.isInstanceOf(ApiStatusException.class)
				.satisfies(ex -> {
					ApiStatusException api = (ApiStatusException) ex;
					assertThat(api.status()).isEqualTo(409);
					assertThat(api.error().code()).isEqualTo(ErrorCodes.STOCK_INSUFFICIENT);
				});
	}

	@Test
	void memberAddressContractCopiesSnapshot() {
		CorrelationIdHolder.set("corr-addr");
		WM.stubFor(get(urlPathEqualTo("/internal/v1/addresses/4"))
				.withQueryParam("userId", equalTo("7"))
				.willReturn(okJson("""
				{"addressId":4,"userId":7,"receiverName":"张三","phone":"13800000000","province":"上海","city":"上海","district":"浦东","detail":"张江路1号"}
				""")));
		var snapshot = memberAddressClient.getAddress(4L, 7L);
		assertThat(snapshot.receiverName()).isEqualTo("张三");
		assertThat(snapshot.detail()).isEqualTo("张江路1号");
		WM.verify(getRequestedFor(urlPathEqualTo("/internal/v1/addresses/4"))
				.withQueryParam("userId", equalTo("7"))
				.withHeader(CorrelationHeaders.CORRELATION_ID, equalTo("corr-addr")));
	}

	@Test
	void paymentOpenCollectMoneyContract() {
		CorrelationIdHolder.set("corr-pay");
		WM.stubFor(post(urlEqualTo("/internal/v1/payments")).willReturn(okJson("""
				{"paymentId":88,"orderId":11,"amountCents":3998,"status":"PENDING"}
				""")));
		var payment = paymentClient.openCollectMoney("pay-11", new OpenCollectMoneyCommand(11L, 7L, 3998L));
		assertThat(payment.status()).isEqualTo(PaymentStatus.PENDING);
		assertThat(payment.amountCents()).isEqualTo(3998L);
		WM.verify(postRequestedFor(urlEqualTo("/internal/v1/payments"))
				.withHeader(IdempotencyHeaders.KEY, equalTo("pay-11"))
				.withHeader(CorrelationHeaders.CORRELATION_ID, equalTo("corr-pay"))
				.withRequestBody(matchingJsonPath("$.amountCents", equalTo("3998"))));
	}
}
