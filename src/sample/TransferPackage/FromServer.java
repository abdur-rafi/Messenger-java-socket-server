package sample.TransferPackage;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import sample.Constants;
import sample.DataPackage.MessagePackage.GroupMessage;
import sample.DataPackage.MessagePackage.Message;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.ArrayList;

public class FromServer implements Serializable {
    private String state = null;
    private GroupMessage groupMessage = null;
    private ArrayList<Message>  message = null;
    transient private Image image = null;
    private int toBeModified = 0;
    private int another = 0;
    private long serialVersionUID = 6L;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public GroupMessage getGroupMessage() {
        return groupMessage;
    }

    public void setGroupMessage(GroupMessage groupMessage) {
        this.groupMessage = groupMessage;
    }

    public ArrayList<Message> getMessage() {
        return message;
    }

    public void setMessage(ArrayList<Message> message) {
        this.message = message;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public int getToBeModified() {
        return toBeModified;
    }

    public void setToBeModified(int toBeModified) {
        this.toBeModified = toBeModified;
    }

    public int getAnother() {
        return another;
    }

    public void setAnother(int another) {
        this.another = another;
    }

    public FromServer(String state, GroupMessage groupMessage, ArrayList<Message> message, Image image, int toBeModified, int another) {
        this.state = state;
        this.groupMessage = groupMessage;
        this.message = message;
        this.image = image;
        this.toBeModified = toBeModified;
        this.another = another;
    }

//    private void writeObject(ObjectOutputStream out) throws IOException {
//        out.defaultWriteObject();
//        ByteArrayOutputStream bstream = new ByteArrayOutputStream();
//        if(image != null) {
//            out.write(1);
//            ImageIO.write(SwingFXUtils.fromFXImage(image, null), Constants.format, bstream);
//            byte[] buffer = bstream.toByteArray();
//            out.writeObject(buffer);
//        }
//        else out.write(0);
//    }
//
//    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
//        in.defaultReadObject();
//        int present = in.read();
//        if(present == 1) {
//            byte[] buffer = (byte[]) in.readObject();
//            image = SwingFXUtils.toFXImage(ImageIO.read(new ByteArrayInputStream(buffer)), null);
//        }
//        else image = null;
//    }
}
