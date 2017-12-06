package app;

import connection.MMClient;
import gui.InitFxGui;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

public class Main {

    public final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static void main(String[] args) {

        MMClient client = null;

        try {
            client = new MMClient("10.60.32.60", 50051);
            client.getRoi("test");

            System.out.println("camera props: " + client.getCameraProperties("test"));

            InitFxGui.run(client);

            client.getRoi("test");
        } finally {
            try {
                client.shutdown();
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static String StackTraceToString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
