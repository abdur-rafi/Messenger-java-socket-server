package sample;

public class Constants {
    private static Constants constants = new Constants();
    public static Constants getInstance(){
        return constants;
    }
    public final String host = "192.168.0.102";
    public final int tcpPort = 4003;
    public final int udpPort = 4000;
    public static String format = ".png";
    private Constants(){
        ;
    }
}
