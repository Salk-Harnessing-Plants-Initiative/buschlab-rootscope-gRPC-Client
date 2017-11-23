import com.google.protobuf.ByteString;
import io.grpc.RootScopeService.*;
import io.grpc.Server;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.io.*;
import java.nio.file.Files;
import java.security.cert.CertificateException;
import java.util.logging.Logger;

public class MMServer {
    private static final Logger logger = Logger.getLogger(MMServer.class.getName());

    private Server server;

    private void start() throws IOException {
        int port = 50051;

        try {
            SelfSignedCertificate ssc = new SelfSignedCertificate();

            server = NettyServerBuilder.forPort(port)
                    .maxMessageSize(1048576000)
                    .addService(new MmServiceImpl())
                    .sslContext(GrpcSslContexts.forServer(ssc.certificate(),ssc.privateKey()).build())
                    .build()
                    .start();

        } catch (CertificateException e) {
            e.printStackTrace();
        }

        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                MMServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final MMServer server = new MMServer();
        server.start();
        server.blockUntilShutdown();
    }

    static class MmServiceImpl extends MmServiceGrpc.MmServiceImplBase {

        int xRoi = 10;
        int yRoi = 20;

        @Override
        public void getRoi(RoiRequest req, StreamObserver<RoiReply> responseObserver) {
            RoiReply.Builder builder = RoiReply.newBuilder();
            builder.setRoiX(42).setRoiY(21);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        }

        @Override
        public void getImgData(ImgDataRequest req, StreamObserver<ImgDataReply> responseObserver) {

            ImgDataReply.Builder builder = ImgDataReply.newBuilder();
            File file = new File("/Users/alexander.bindeus/Desktop/bigpng.png");

            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                builder.setImgData(ByteString.copyFrom(bytes));
            } catch (IOException e) {
                e.printStackTrace();
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        }
    }
}
