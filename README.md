# microcks-for-java-local-env

This project showcases how to set up [Microcks](https://microcks.io/), as a way to create local environments for Spring
Boot applications accessing mocked downstream services.

## Local Environment

Having [Docker](https://docs.docker.com/engine/install/) installed, you can run the following commands to start Microcks
mocks of the downstream dependencies, as well as the demo service itself(home-screen-bff).

```bash
# on the root folder of the project
./gradlew clean build
docker compose up -d
./gradlew :services:home-screen-bff:bootRun
```

One can also start both directly from IntelliJ IDEA, if you have the plugins for Docker and Spring Boot installed.

Then you can access the application via gRPC using a client of your preference, such
as [grpcurl](https://github.com/fullstorydev/grpcurl).

```shell
grpcurl --plaintext \
    --proto ./libs/home-screen-bff-protos/src/main/resources/com/bitso/demo/home/v1/home_screen_bff_v1.proto \
    -d '{"payload": {"user_id": "1234"}}' \
    localhost:8201 com.bitso.demo.home.v1.HomeScreenBffV1/Summary
```

Send `"user_id": "123"` if you want to simulate an error on a downstream service.

### Microcks

The mock configurations are available on `/services/home-screen-bff/src/test/resources/microcks`.
Once the container is running, Microcks' admin UI can be accessed through http://localhost:9081/#/.
gRPC port is 9201, and one can verify the mock responses executing the following commands from the root folder of the
project.

```shell
grpcurl --plaintext \
    --proto ./libs/balance-service-protos/src/main/resources/com/bitso/demo/balance/v1/balance_service_v1.proto \
    -d '{"payload": {"user_id": "1234"}}' \
    localhost:9201 com.bitso.demo.balance.v1.BalanceServiceV1/Get
```

```shell
grpcurl --plaintext \
    --proto ./libs/trade-service-protos/src/main/resources/com/bitso/demo/trade/v1/trade_service_v1.proto \
    -d '{"payload": {"from_value": "1234", "from_asset": "btc", "to_asset": "mxn"}}' \
    localhost:9201 com.bitso.demo.trade.v1.TradeServiceV1/Quote
```
