package com.clients.rpctypes;

import com.models.Money;
import com.models.WithdrawlError;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;

import static com.clients.metadata.ClientConstants.WITHDRAW_ERROR_KEY;

public class MoneyStreamingResponse implements StreamObserver<Money> {

    private CountDownLatch latch;

    public MoneyStreamingResponse(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onNext(Money o) {
        System.out.println("Received Async " + o.getValue());
    }

    @Override
    public void onError(Throwable throwable) {
        Metadata metadata = Status.trailersFromThrowable(throwable);
        WithdrawlError withdrawlError = metadata.get(WITHDRAW_ERROR_KEY);
        System.out.println(withdrawlError.getErrorMessage());
        latch.countDown();
    }

    @Override
    public void onCompleted() {
        System.out.println("completed");
        latch.countDown();
    }
}
