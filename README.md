# minimart-order-service

MiniMart 的订单进程（Cart / Order / OrderLine）。领域用语与 v1 契约在编排仓 [`minimart-infra`](../minimart-infra)。Payment 在 `minimart-payment-service`。

- Spring 名：`order-service`
- 端口：8083
- 库：`minimart_order`（由 infra 的 `docker/mysql/init.sql` 建；本进程尚未连库）
- Feign：product（reserve/confirm/release）、member（抄 Address）、payment（开单收钱）。不自己写 Stock，不自己把单改成 `PAID`。
- Feign 目标 URL 默认 `http://<service>:<port>`（K8s Service DNS），见 `application.yaml`。

本机运行：

```bash
./gradlew bootRun
```

编排：与其它仓并列 clone 后，在 `minimart-infra` 执行 `docker compose up`（无 Nacos；Docker DNS 与 K8s Service 名一致）。
