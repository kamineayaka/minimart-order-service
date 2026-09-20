# minimart-order-service

MiniMart 的订单进程（Cart / Order / OrderLine）。领域用语与 v1 契约在编排仓 [`minimart-infra`](../minimart-infra)。Payment 在 `minimart-payment-service`。

- Spring 名：`order-service`
- 端口：8083
- 库：`minimart_order`（由 infra 的 `docker/mysql/init.sql` 建）

本机运行（Nacos 需已起，`NACOS_ADDR=127.0.0.1:8848`）：

```bash
./gradlew bootRun
```

编排：与其它仓并列 clone 后，在 `minimart-infra` 执行 `docker compose up`。
