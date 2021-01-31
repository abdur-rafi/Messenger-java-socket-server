package sample.DataPackage.MessagePackage;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import sample.Constants;

import javax.imageio.ImageIO;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class Message implements Serializable {
    private String message;
    private int senderIndex;
    private int senderPort;
    private LocalDate date;
    private LocalTime time;
    private boolean isFirst;
    transient private Image image;
    private String fileName = null;
    private int fileIndex = 0;
    private long serialVersionUID = 5L;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getSenderIndex() {
        return senderIndex;
    }

    public void setSenderIndex(int senderIndex) {
        this.senderIndex = senderIndex;
    }

    public int getSenderPort() {
        return senderPort;
    }

    public void setSenderPort(int senderPort) {
        this.senderPort = senderPort;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileIndex() {
        return fileIndex;
    }

    public void setFileIndex(int fileIndex) {
        this.fileIndex = fileIndex;
    }

    public Message(String message, int senderIndex, int senderPort, LocalDate date, LocalTime time, boolean isFirst, Image image) {
        this.message = message;
        this.senderIndex = senderIndex;
        this.senderPort = senderPort;
        this.date = date;
        this.time = time;
        this.isFirst = isFirst;
        this.image = image;
    }

    public Message(String message, int senderIndex, int senderPort, LocalDate date, LocalTime time, boolean isFirst, Image image, String fileName, int fileIndex) {
        this.message = message;
        this.senderIndex = senderIndex;
        this.senderPort = senderPort;
        this.date = date;
        this.time = time;
        this.isFirst = isFirst;
        this.image = image;
        this.fileName = fileName;
        this.fileIndex = fileIndex;
    }


    private void writeObject(ObjectOutputStream out) {
        try {
            out.defaultWriteObject();
            ByteArrayOutputStream bstream = new ByteArrayOutputStream();
            if (image != null) {
                out.write(1);
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", bstream);
                byte[] buffer = bstream.toByteArray();
                out.writeObject(buffer);
            } else out.write(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readObject(ObjectInputStream in) {
        try {

            in.defaultReadObject();
            int present = in.read();
            if (present == 1) {
                byte[] buffer = (byte[]) in.readObject();
                image = SwingFXUtils.toFXImage(ImageIO.read(new ByteArrayInputStream(buffer)), null);
            } else image = null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
