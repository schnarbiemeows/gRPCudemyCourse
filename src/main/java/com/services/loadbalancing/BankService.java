package com.services.loadbalancing;


import com.models.*;
import com.services.rpctypes.AccountDatabase;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class BankService extends BankServiceGrpc.BankServiceImplBase {

    @Override
    public void getBalance(BalanceCheckRequest request, StreamObserver<Balance> responseObserver) {
        int accountNumber = request.getAccountNumber();
        System.out.println(
                "Received request for account # : " + accountNumber
        );
        Balance balance = Balance.newBuilder()
                .setAmount(AccountDatabase.getBalance(accountNumber))
                .build();
        responseObserver.onNext(balance);
        responseObserver.onCompleted();
    }

    @Override
    public void withdraw(WithdrawRequest request, StreamObserver<Money> responseObserver) {
        int accountNumber = request.getAccountNumber();
        System.out.println(
                "Received request for account # : " + accountNumber
        );
        int amount = request.getAmount();
        Integer balance = AccountDatabase.getBalance(accountNumber);
        if(balance<amount) {
            Status status = Status.FAILED_PRECONDITION.withDescription("Not enough money! You only have " +
                    balance + " you idgit!");
            responseObserver.onError(status.asRuntimeException());
        }
        for (int i=0; i<amount/10;i++) {
            Money money = Money.newBuilder().setValue(10).build();
            responseObserver.onNext(money);
            AccountDatabase.deductBalance(accountNumber,10);
            try {
                Thread.sleep(1100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<DepositRequest> cashDeposit(StreamObserver<Balance> responseObserver) {
        return new CashStreamingRequest(responseObserver);
    }
}
