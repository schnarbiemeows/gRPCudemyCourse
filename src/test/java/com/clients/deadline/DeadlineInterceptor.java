package com.clients.deadline;

import io.grpc.*;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DeadlineInterceptor implements ClientInterceptor {
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor,
        CallOptions callOptions, Channel channel) {
        // this shit allows you to override the default dealine in individual calls
        Deadline deadline = callOptions.getDeadline();  // it will be null if it's not set for an individual call
        if(Objects.isNull(deadline)) {
            callOptions = callOptions.withDeadline(Deadline.after(8, TimeUnit.SECONDS));
        }
        return channel.newCall(methodDescriptor,callOptions);
    }
}
