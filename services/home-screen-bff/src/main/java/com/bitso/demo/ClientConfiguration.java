package com.bitso.demo;

import com.bitso.demo.balance.v1.BalanceServiceV1Grpc;
import com.bitso.demo.trade.v1.TradeServiceV1Grpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.GrpcClientBean;
import org.springframework.context.annotation.Configuration;

@GrpcClientBean(
        clazz = BalanceServiceV1Grpc.BalanceServiceV1BlockingStub.class,
        beanName = "balanceServiceGrpcStub",
        client = @GrpcClient(value = "balance-service"))
@GrpcClientBean(
        clazz = TradeServiceV1Grpc.TradeServiceV1BlockingStub.class,
        beanName = "tradeServiceGrpcStub",
        client = @GrpcClient(value = "trade-service"))
@Configuration
public class ClientConfiguration {
}
