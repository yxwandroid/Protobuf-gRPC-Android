package com.xuexiang.protobufdemo;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class BackendInterceptor implements ClientInterceptor {

    public static final Metadata.Key<String> TRACE_ID_KEY = Metadata.Key.of("traceId", ASCII_STRING_MARSHALLER);

    @Override
    public <M, R> ClientCall<M, R> interceptCall(
            final MethodDescriptor<M, R> method, CallOptions callOptions, Channel next) {
        return new BackendForwardingClientCall<M, R>(method,
                next.newCall(method, callOptions.withDeadlineAfter(10000, TimeUnit.MILLISECONDS))) {

            @Override
            public void sendMessage(M message) {
                Log.e("wilson","Method: {%@}, Message: {}-------" +methodName +"--------"+ message);
                super.sendMessage(message);
            }

            @Override
            public void start(Listener<R> responseListener, Metadata headers) {
                // TODO: Use the sleuth traceId instead of 999
                headers.put(TRACE_ID_KEY, "999");

                BackendListener<R> backendListener = new BackendListener<>(methodName, responseListener);
                super.start(backendListener, headers);
            }
        };
    }

    private class BackendListener<R> extends ClientCall.Listener<R> {

        String methodName;
        ClientCall.Listener<R> responseListener;

        protected BackendListener(String methodName, ClientCall.Listener<R> responseListener) {
            super();
            this.methodName = methodName;
            this.responseListener = responseListener;
        }

        @Override
        public void onMessage(R message) {
          //  logger.info("Method: {}, Response: {}", methodName, message);
            Log.e("wilson","Method: {}, Message: {}  --- "+methodName+" ---------  "+message);
            responseListener.onMessage(message);
        }

        @Override
        public void onHeaders(Metadata headers) {
            responseListener.onHeaders(headers);
        }

        @Override
        public void onClose(Status status, Metadata trailers) {
            responseListener.onClose(status, trailers);
        }

        @Override
        public void onReady() {
            responseListener.onReady();
        }
    }

    private class BackendForwardingClientCall<M, R> extends io.grpc.ForwardingClientCall.SimpleForwardingClientCall<M, R> {

        String methodName;

        protected BackendForwardingClientCall(MethodDescriptor<M, R> method, ClientCall delegate) {
            super(delegate);
            methodName = method.getFullMethodName();
        }
    }
}