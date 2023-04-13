package com.clients.metadata;

import com.models.WithdrawlError;
import io.grpc.Metadata;
import io.grpc.protobuf.ProtoUtils;

public class ClientConstants {

    public static final Metadata.Key<WithdrawlError> WITHDRAW_ERROR_KEY = ProtoUtils.keyForProto(
            WithdrawlError.getDefaultInstance());

    public static final Metadata.Key<String> USER_TOKEN = Metadata.Key.of("user-token",
            Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata METADATA = new Metadata();

    static {
        METADATA.put(
                Metadata.Key.of("client-token",Metadata.ASCII_STRING_MARSHALLER),
                "bank-client-secret"
        );
    }
    public static Metadata getClientToken() {
        return METADATA;
    }
}
