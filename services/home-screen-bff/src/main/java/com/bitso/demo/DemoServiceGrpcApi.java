package com.bitso.demo;

import com.bitso.demo.balance.v1.BalanceServiceV1Grpc;
import com.bitso.demo.balance.v1.BalanceServiceV1OuterClass;
import com.bitso.demo.trade.v1.TradeServiceV1Grpc;
import com.bitso.demo.trade.v1.TradeServiceV1OuterClass;
import com.bitso.demo.home.v1.HomeScreenBffV1Grpc;
import com.bitso.demo.home.v1.HomeScreenBffV1OuterClass;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class DemoServiceGrpcApi extends HomeScreenBffV1Grpc.HomeScreenBffV1ImplBase {
    private final BalanceServiceV1Grpc.BalanceServiceV1BlockingStub balanceService;
    private final TradeServiceV1Grpc.TradeServiceV1BlockingStub tradeService;

    private static final String MAIN_ASSET = "mxn";

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    DemoServiceGrpcApi(BalanceServiceV1Grpc.BalanceServiceV1BlockingStub balanceService,
                       TradeServiceV1Grpc.TradeServiceV1BlockingStub tradeService) {
        this.balanceService = balanceService;
        this.tradeService = tradeService;
    }

    @Override
    public void summary(HomeScreenBffV1OuterClass.SummaryMessage request, StreamObserver<HomeScreenBffV1OuterClass.SummaryResponseMessage> responseObserver) {

        int userId = request.getPayload().getUserId();
        var balances = getBalances(userId);
        var totalBalance = convertToMainBalance(balances);

        responseObserver.onNext(HomeScreenBffV1OuterClass.SummaryResponseMessage
                .newBuilder()
                .setPayload(HomeScreenBffV1OuterClass.SummaryResponseMessage.SummaryResponsePayload
                        .newBuilder()
                        .setUser(HomeScreenBffV1OuterClass.User
                                .newBuilder()
                                .setUserId(userId)
                                .addAllAssetBalances(balances)
                                .setTotalBalance(totalBalance)
                                .build())
                        .build())
                .build());
        responseObserver.onCompleted();
    }

    private HomeScreenBffV1OuterClass.AssetBalance convertToMainBalance(List<HomeScreenBffV1OuterClass.AssetBalance> balances) {
        Long totalBalance = balances.stream().map(element -> {
                    TradeServiceV1OuterClass.AssetQuoteResponseMessage quote = tradeService.quote(TradeServiceV1OuterClass.AssetQuoteMessage
                            .newBuilder()
                            .setPayload(TradeServiceV1OuterClass.AssetQuoteMessage.AssetQuotePayload
                                    .newBuilder()
                                    .setFromValue(element.getBalance())
                                    .setFromAsset(element.getAsset())
                                    .setToAsset(MAIN_ASSET)
                                    .build())
                            .build());
                    return quote.getPayload().getValue();
                })
                .reduce(Long::sum)
                .orElse(0L);
        return HomeScreenBffV1OuterClass.AssetBalance
                .newBuilder()
                .setAsset(MAIN_ASSET)
                .setBalance(totalBalance)
                .build();
    }

    private List<HomeScreenBffV1OuterClass.AssetBalance> getBalances(int userId) {
        BalanceServiceV1OuterClass.GetBalanceResponseMessage getBalanceResponseMessage = balanceService.get(BalanceServiceV1OuterClass.GetBalanceMessage
                .newBuilder()
                .setPayload(
                        BalanceServiceV1OuterClass.GetBalanceMessage.GetBalancePayload.newBuilder().setUserId(userId).build())
                .build());

        return getBalanceResponseMessage
                .getPayload()
                .getUser()
                .getAssetBalancesList()
                .stream()
                .map(balance -> HomeScreenBffV1OuterClass.AssetBalance
                        .newBuilder()
                        .setAsset(balance.getAsset())
                        .setBalance(balance.getBalance())
                        .build())
                .collect(Collectors.toList());
    }
}
