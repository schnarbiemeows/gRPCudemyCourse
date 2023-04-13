package com.clients.metadata;

import com.clients.deadline.DeadlineInterceptor;
import com.clients.rpctypes.BalanceStreamObserver;
import com.clients.rpctypes.MoneyStreamingResponse;
import com.models.*;
import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MetadataClientTest {

    private BankServiceGrpc.BankServiceBlockingStub blockingStub;
    private BankServiceGrpc.BankServiceStub nonblockingStub;

    @BeforeAll
    public void setup() {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 6565)
                .intercept(MetadataUtils.newAttachHeadersInterceptor(ClientConstants.getClientToken()))
                .usePlaintext().build();
        this.blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
        this.nonblockingStub = BankServiceGrpc.newStub(managedChannel);
    }

    @Test
    public void balanceTest() {
        BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(7)
                .build();
        try {
            Balance balance = this.blockingStub
                    .withCallCredentials(new UserSessionToken("user-secret-3:prime"))
                    .getBalance(balanceCheckRequest);
            System.out.println("balance = " + balance.getAmount());
        } catch(StatusRuntimeException ex) {
            System.out.println("timed out");
        }

    }

    @Test
    public void withdrawlTest() {
        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
                .setAccountNumber(7)
                .setAmount(40)
                .build();
        try {
            this.blockingStub
                    .withdraw(withdrawRequest)
                    .forEachRemaining(money -> System.out.println("received: " +
                            money.getValue()));
        } catch(StatusRuntimeException ex) {
            System.out.println("blah");
        }
    }

    @Test
    public void withdrawlAsyncTest() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
                .setAccountNumber(8)
                .setAmount(50)
                .build();
        this.nonblockingStub.withdraw(withdrawRequest, new MoneyStreamingResponse(countDownLatch));
        countDownLatch.await();
    }

    @Test
    public void cashStreamingRequest() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        StreamObserver<DepositRequest> streamObserver =
                this.nonblockingStub.cashDeposit(new BalanceStreamObserver(countDownLatch));
        for (int count = 0; count < 10; count++) {
            DepositRequest depositRequest = DepositRequest.newBuilder()
                    .setAccountNumber(8)
                    .setAmount(10)
                    .build();
            streamObserver.onNext(depositRequest);
        }
        streamObserver.onCompleted();
        countDownLatch.await();
    }
}
