package sample.TransferPackage;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import sample.Constants;
import sample.DataPackage.Account;
import sample.DataPackage.MessagePackage.Message;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.ArrayList;
import java.util.Set;

public class transferPackage implements Serializable {
    private String state = null;
    private Account account = null;
    private ArrayList<Message> messages = null;
    transient private Image image = null;
    private int senderId = - 1;
    private int receiverId = -1;
    private ArrayList<Integer> count;
    private ArrayList<Integer> newCount;
    private long serialVersionUID = 3L;
    private Set<Integer> archivedMessageSet;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public ArrayList<Integer> getCount() {
        return count;
    }

    public void setCount(ArrayList<Integer> count) {
        this.count = count;
    }

    public ArrayList<Integer> getNewCount() {
        return newCount;
    }

    public void setNewCount(ArrayList<Integer> newCount) {
        this.newCount = newCount;
    }

    public Set<Integer> getArchivedMessageSet() {
        return archivedMessageSet;
    }

    public void setArchivedMessageSet(Set<Integer> archivedMessageSet) {
        this.archivedMessageSet = archivedMessageSet;
    }

    public transferPackage(String state, Account account, ArrayList<Message> messages, Image image, int senderId, int receiverId, ArrayList<Integer> count, ArrayList<Integer> newCount) {
        this.state = state;
        this.account = account;
        this.messages = messages;
        this.image = image;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.count = count;
        this.newCount = newCount;
        this.archivedMessageSet = null;
    }

    public transferPackage(String state, Account account, ArrayList<Message> messages, Image image, int senderId, int receiverId, ArrayList<Integer> count, ArrayList<Integer> newCount,Set<Integer> s) {
        this.state = state;
        this.account = account;
        this.messages = messages;
        this.image = image;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.count = count;
        this.newCount = newCount;
        this.archivedMessageSet = s;
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
