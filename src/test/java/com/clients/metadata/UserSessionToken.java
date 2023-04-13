package com.clients.metadata;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;

import java.util.concurrent.Executor;

public class UserSessionToken extends CallCredentials {

    private String jwt; // it is assumed that this jwt is coming to us from somewhere else

    public UserSessionToken(String jwt) {
        this.jwt = jwt;
    }

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier) {
        // applying the token to the metadata needs to be non-blocking(asynchronous) for some reason
        executor.execute(() -> {
            // also, only need this try/catch if this class is the one that is generating the jwt token
            // so that if that fails, it doesn't bother to send the request
            try {
                Metadata metadata = new Metadata();
                metadata.put(ClientConstants.USER_TOKEN, this.jwt);
                metadataApplier.apply(metadata);
            } catch(Exception e) {
                metadataApplier.fail(Status.ABORTED);
            }
        });
    }

    @Override
    public void thisUsesUnstableApi() {
        // may change in future
    }
}
