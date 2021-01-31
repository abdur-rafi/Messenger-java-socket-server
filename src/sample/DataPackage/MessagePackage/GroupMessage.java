package sample.DataPackage.MessagePackage;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import sample.DataPackage.Account;

import javax.imageio.ImageIO;
import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class GroupMessage implements Serializable {
    private ArrayList<Message> groupMessage;
    private ArrayList<Account> participants;
    private ArrayList<Integer> personIndex;
    private int databaseIndex;
    private ArrayList<String> sharedFiles;
    private String groupName;
    transient private Image groupImage;
    private boolean addAble;
    transient public IntegerProperty unseenCount = new SimpleIntegerProperty(0);
    transient public IntegerProperty newCount = new SimpleIntegerProperty(0);
    private long serialVersionUID = 4L;
    public LocalDateTime lastMessageTime;
    public int fileIndex ;
    transient public BooleanProperty newMessage = new SimpleBooleanProperty(false);

    public ArrayList<Message> getGroupMessage() {
        return groupMessage;
    }

    public void setGroupMessage(ArrayList<Message> groupMessage) {
        this.groupMessage = groupMessage;
    }

    public ArrayList<Account> getParticipants() {
        return participants;
    }

    public void setParticipants(ArrayList<Account> participants) {
        this.participants = participants;
    }

    public ArrayList<Integer> getPersonIndex() {
        return personIndex;
    }

    public void setPersonIndex(ArrayList<Integer> personIndex) {
        this.personIndex = personIndex;
    }

    public int getDatabaseIndex() {
        return databaseIndex;
    }

    public void setDatabaseIndex(int databaseIndex) {
        this.databaseIndex = databaseIndex;
    }

    public ArrayList<String> getSharedFiles() {
        return sharedFiles;
    }

    public void setSharedFiles(ArrayList<String> sharedFiles) {
        this.sharedFiles = sharedFiles;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Image getGroupImage() {
        return groupImage;
    }

    public void setGroupImage(Image groupImage) {
        this.groupImage = groupImage;
    }

    public boolean isAddAble() {
        return addAble;
    }

    public void setAddAble(boolean addAble) {
        this.addAble = addAble;
    }

    public GroupMessage(ArrayList<Message> groupMessage, ArrayList<Account> participants,
                        int databaseIndex, ArrayList<String> sharedFiles, String groupName,
                        Image groupImage, boolean addAble,
                        ArrayList<Integer> personIndex) {
        this.groupMessage = groupMessage;
        this.participants = participants;
        this.databaseIndex = databaseIndex;
        this.sharedFiles = sharedFiles;
        this.groupName = groupName;
        this.groupImage = groupImage;
        this.addAble = addAble;
        this.personIndex = personIndex;
        this.lastMessageTime = LocalDateTime.now();
    }


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        ByteArrayOutputStream bstream = new ByteArrayOutputStream();
        if (groupImage != null) {
            out.write(1);
            ImageIO.write(SwingFXUtils.fromFXImage(groupImage, null), "png", bstream);
            byte[] buffer = bstream.toByteArray();
            out.writeObject(buffer);
        } else out.write(0);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        int present = in.read();
        if (present == 1) {
            byte[] buffer = (byte[]) in.readObject();
            groupImage = SwingFXUtils.toFXImage(ImageIO.read(new ByteArrayInputStream(buffer)), null);
        } else groupImage = null;
    }
}
