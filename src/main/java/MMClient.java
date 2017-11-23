import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.RootScopeService.*;
import io.grpc.StatusRuntimeException;
import io.grpc.internal.AbstractManagedChannelImplBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;


import javax.imageio.ImageIO;
import javax.net.ssl.SSLException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MMClient {
    private static final Logger logger = Logger.getLogger(MMClient.class.getName());

    private final ManagedChannel channel;
    private final MmServiceGrpc.MmServiceBlockingStub blockingStub;

    /** Construct client connecting to HelloWorld server at {@code host:port}. */
    public MMClient(String host, int port) {

//        this(ManagedChannelBuilder.forAddress(host, port)
//                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
//                // needing certificates.
//                .maxInboundMessageSize(1048576000)
//                .sslContext(SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build())
//                //.usePlaintext(true)
//                .build());

        SslContext ssLContext = null;

        try {
            ssLContext = GrpcSslContexts.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } catch (SSLException e) {
            e.printStackTrace();
        }

        if (ssLContext != null) {
//            channel = AbstractManagedChannelImplBuilder.forAddress(host, port)
//                    .maxInboundMessageSize(1048576000)
//                    //.sslContext(ssLContext)
//                    .build();
//
            channel = NettyChannelBuilder.forAddress(host, port)
                    .maxInboundMessageSize(1048576000)
                    .sslContext(ssLContext)
                    .build();
        } else {
            channel = NettyChannelBuilder.forAddress(host, port)
                    .usePlaintext(true)
                    .build();
        }
        blockingStub = MmServiceGrpc.newBlockingStub(channel);
    }

    /** Construct client for accessing RouteGuide server using the existing channel. */
    MMClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = MmServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void getRoi(String name) {
        logger.info("Requesting Roi " + name + " ...");
        RoiRequest request = RoiRequest.newBuilder().setAver("roi please").build();
        RoiReply response;
        try {
            response = blockingStub.withCompression("gzip").getRoi(request);
            //response = blockingStub.getRoi(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("RoiX: " + response.getRoiX() + " RoiY: " + response.getRoiY());
    }

//    public void getImgBlobs(String aver) {
//        logger.info("Requesting image data blobs...");
//        ImgPutRequest request = ImgPutRequest.newBuilder().setMessage("hm..").build();
//
//        InputStream is = null;
//
//        List<InputStream> iss = new ArrayList<>();
//
//        Iterator<ImgPutReply> blobIter;
//
//        blobIter = blockingStub.getImgBlob(request);
//        for (int i = 1; blobIter.hasNext(); ++i) {
//            ImgPutReply blob = blobIter.next();
//
//            byte[] data = blob.getData().toByteArray();
//            long offset = blob.getOffset();
//            String name = blob.getName();
//
//            try {
//                is.read(data);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        new SequenceInputStream(Collections.enumeration(iss)).;
//    }

    public void getImgData(String req) {
        logger.info("Requesting image data...");
        ImgDataRequest request = ImgDataRequest.newBuilder().setAver("img please").build();
        ImgDataReply response;
        try {
            response = blockingStub.withCompression("gzip").getImgData(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }

        byte[] content = response.getImgData().toByteArray();

        System.out.println("read " + content.length + " byte");

        InputStream in = new ByteArrayInputStream(content);
        try {
            BufferedImage bImageFromConvert = ImageIO.read(in);
            ImageIO.write(bImageFromConvert, "png", new File(
                    "/Users/alexander.bindeus/Desktop/rootscope/response.png"));

            System.out.println("done");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        MMClient client = new MMClient("10.60.32.11", 50051);
        try {
            String user = "world";
            if (args.length > 0) {
                user = args[0];
            }
            client.getRoi(user);
            client.getImgData("bitte");
        } finally {
            client.shutdown();
        }
    }
}

