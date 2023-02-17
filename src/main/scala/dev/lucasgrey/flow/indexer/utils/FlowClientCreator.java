package dev.lucasgrey.flow.indexer.utils;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.onflow.protobuf.access.AccessAPIGrpc;

public class FlowClientCreator {

    private static final String DEFAULT_USER_AGENT = "Flow JVM SDK";
    private static final int DEFAULT_MAX_MESSAGE_SIZE = 16777216;

    public static AccessAPIGrpc.AccessAPIFutureStub buildAPIFutureStubs(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(host, port)
                .userAgent(DEFAULT_USER_AGENT)
                .maxInboundMessageSize(DEFAULT_MAX_MESSAGE_SIZE)
                .usePlaintext()
                .build();
        return AccessAPIGrpc.newFutureStub(channel);
    }


}
