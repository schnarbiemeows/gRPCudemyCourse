package com.services.metadata;


import com.google.common.util.concurrent.Uninterruptibles;
import com.models.*;
import com.services.loadbalancing.CashStreamingRequest;
import com.services.rpctypes.AccountDatabase;
import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;

public class MetadataService extends BankServiceGrpc.BankServiceImplBase {

    @Override
    public void getBalance(BalanceCheckRequest request, StreamObserver<Balance> responseObserver) {
        int accountNumber = request.getAccountNumber();
        System.out.println(
                "Received request for account # : " + accountNumber
        );
        // for lecture 124 - Context
        int amount = AccountDatabase.getBalance(accountNumber);
        UserRole userRole = ServerConstants.CTX_USER_ROLE.get(); // somehow it finds this in the currect thread's context
        UserRole userRole1 = ServerConstants.CTX_USER_ROLE1.get();
        System.out.println("role1 = " + userRole1);
        amount = UserRole.PRIME.equals(userRole) ? amount : (amount-15);
        // END for lecture 124
        Balance balance = Balance.newBuilder()
                .setAmount(amount)
                .build();
        //Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
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
        if(amount<10 || (amount%10) != 0) {
            Metadata metadata = new Metadata();
            Metadata.Key<WithdrawlError> errorKey = ProtoUtils.keyForProto(WithdrawlError.getDefaultInstance());
            WithdrawlError withdrawlError = WithdrawlError.newBuilder()
                    .setAmount(amount)
                    .setErrorMessage(ErrorMessage.ONLY_TEN_MULTIPLES)
                    .build();
            metadata.put(errorKey,withdrawlError);
            responseObserver.onError(Status.FAILED_PRECONDITION.asRuntimeException(metadata));
        }
        if(balance<amount) {
            Metadata metadata = new Metadata();
            Metadata.Key<WithdrawlError> errorKey = ProtoUtils.keyForProto(WithdrawlError.getDefaultInstance());
            WithdrawlError withdrawlError = WithdrawlError.newBuilder()
                    .setAmount(balance)
                    .setErrorMessage(ErrorMessage.INSUFFICIENT_BALANCE)
                    .build();
            metadata.put(errorKey,withdrawlError);
            responseObserver.onError(Status.FAILED_PRECONDITION.asRuntimeException(metadata));
        }
        for (int i=0; i<amount/10;i++) {
            Money money = Money.newBuilder().setValue(10).build();
            responseObserver.onNext(money);
            System.out.println("Delivered : $10");
            AccountDatabase.deductBalance(accountNumber,10);
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<DepositRequest> cashDeposit(StreamObserver<Balance> responseObserver) {
        return new CashStreamingRequest(responseObserver);
    }
}
