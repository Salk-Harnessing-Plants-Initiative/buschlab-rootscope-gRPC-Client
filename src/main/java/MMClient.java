import io.grpc.ManagedChannel;
import io.grpc.RootScopeService.*;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;


import javax.imageio.ImageIO;
import javax.net.ssl.SSLException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MMClient {
    private static final Logger logger = Logger.getLogger(MMClient.class.getName());

    private final ManagedChannel channel;
    private final MmServiceGrpc.MmServiceBlockingStub blockingStub;

    public MMClient(String host, int port) {
        SslContext ssLContext = null;

        try {
            ssLContext = GrpcSslContexts.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } catch (SSLException e) {
            e.printStackTrace();
        }

        if (ssLContext != null) {
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

