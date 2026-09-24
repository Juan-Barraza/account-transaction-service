package com.bancolombia.challenge.account.grpc;


import com.bancolombia.challenge.telemetry.grpc.TelemetryGrpcServiceGrpc;
import com.bancolombia.challenge.telemetry.grpc.TransactionGrpcRequest;
import com.bancolombia.challenge.telemetry.grpc.TransactionGrpcResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class TelemetryGrpcClientService {

    @GrpcClient("telemetry-service")
    private TelemetryGrpcServiceGrpc.TelemetryGrpcServiceBlockingStub telemetryStub;

    public TransactionGrpcResponse evaluateRiskAndCalculateFee(
            String transactionId,
            String accountId,
            Double amount,
            String channel,
            String paymentProvider
    ) {
        TransactionGrpcRequest request = TransactionGrpcRequest.newBuilder()
                .setTransactionId(transactionId)
                .setAccountId(accountId)
                .setAmount(amount)
                .setChannel(channel)
                .setPaymentProvider(paymentProvider)
                .setStatus("NORMAL")
                .build();

        return telemetryStub.evaluateTransactionRisk(request);
    }
}
