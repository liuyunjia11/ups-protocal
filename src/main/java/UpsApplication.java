import org.example.communication.UpsServer;

public class UpsApplication {
    public static void main(String[] args) {
        final String toWorldHost = "vcm-30970.vm.duke.edu";
        final int toWorldPortNum = 12345;

        final int myPortNum = 8080;

        final int worldID;


        try {
            UpsServer upsServer = new UpsServer(toWorldHost, toWorldPortNum, myPortNum);
            upsServer.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
