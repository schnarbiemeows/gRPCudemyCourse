package com.services.metadata;

import io.grpc.*;

import java.util.Objects;

public class AuthInterceptor implements ServerInterceptor {

    /*
    user-secret-3 and user-secret-2 are valid
    user-secret-3 = prime
    user-secret-2 = regular
    they have different permission levels

     */
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall,
        Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        // below for 1st part
        // String clientToken = metadata.get(ServerConstants.TOKEN);
        // below for 2nd part
        String clientToken = metadata.get(ServerConstants.USER_TOKEN);
        if(validate(clientToken)) {
            System.out.println("Auth validated!");
            UserRole userRole = extractRole(clientToken);
            Context context = Context.current().withValue(
                    ServerConstants.CTX_USER_ROLE, userRole
            );
            // for lecture 123 & 124
            return Contexts.interceptCall(context,serverCall,metadata,serverCallHandler);
            // for lectures 111-122
            // return serverCallHandler.startCall(serverCall,metadata);
        } else {
            Status status = Status.UNAUTHENTICATED.withDescription("invalid/expired token");
            serverCall.close(status,metadata);
        }
        return new ServerCall.Listener<ReqT>() {
        };
    }

    private boolean validate(String token) {
        // below for 1st part
        // return Objects.nonNull(token) && token.equals("bank-client-secret");
        // below for 2nd part
        return Objects.nonNull(token) &&
                (token.startsWith("user-secret-2") || token.startsWith("user-secret-3"));
    }

    private UserRole extractRole(String jwt) {
        return jwt.endsWith("prime") ? UserRole.PRIME : UserRole.STANDARD;
    }
}
