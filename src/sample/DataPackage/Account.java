package sample.DataPackage;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import sample.Constants;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Account implements Serializable {
    private String Name;
    private String password;
    private int port = 0;
    private long serialVersionUID = 1L;
    private int databaseIndex;
    transient private Image image = null;
    transient public InetAddress address;
    transient public int key;
    transient public boolean isActive = false;
    transient public DatagramSocket datagramSocket;
    transient public Socket socket;
    public boolean isAndroid = false;
    public int fileId = -1;
    transient public ExecutorService service = null;
    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getSerialVersionUID() {
        return serialVersionUID;
    }

    public void setSerialVersionUID(long serialVersionUID) {
        this.serialVersionUID = serialVersionUID;
    }

    public int getDatabaseIndex() {
        return databaseIndex;
    }

    public void setDatabaseIndex(int databaseIndex) {
        this.databaseIndex = databaseIndex;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }



    public Account(String name, String password,Image image) {
        Name = name;
        this.password = password;
        this.image = image;
    }

    public Account(String name, String password, int port) {
        Name = name;
        this.password = password;
        this.port = port;
    }

    public Account(Account account){
        Name = account.Name;
        password = account.password;
        port = account.port;
        serialVersionUID = account.serialVersionUID;
        databaseIndex = account.databaseIndex;
        image = null;
    }

    public Account(String name, String password, int port, int databaseIndex, Image image,int fileId) {
        Name = name;
        this.password = password;
        this.port = port;
        this.databaseIndex = databaseIndex;
        this.image = image;
        this.fileId = fileId;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        ByteArrayOutputStream bstream = new ByteArrayOutputStream();
        if(image != null) {
            out.write(1);
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", bstream);
            byte[] buffer = bstream.toByteArray();
            out.writeObject(buffer);
        }
        else out.write(0);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        int present = in.read();
        if(present == 1) {
            byte[] buffer = (byte[]) in.readObject();
            image = SwingFXUtils.toFXImage(ImageIO.read(new ByteArrayInputStream(buffer)), null);
        }
        else image = null;
    }


    @Override
    public String toString() {
        return Name;
    }

}
