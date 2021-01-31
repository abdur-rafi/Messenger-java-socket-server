package sample.DataPackage;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import sample.DataPackage.MessagePackage.GroupMessage;
import sample.DataPackage.MessagePackage.Message;

import javax.imageio.ImageIO;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.concurrent.Executors;

public class Database implements Serializable {
    private String fileName = "database";
    private static Database instance = new Database();
    private ArrayList<Person> personList;
    private ArrayList<GroupMessage> messageList;
    private long serialVersionUID = 10L;
    public int fileId = 0;
    public static Database getInstance() {
        return instance;
    }

    private Database() {
        personList = new ArrayList<>();
        messageList = new ArrayList<>();
    }

    public ArrayList<Person> getPersonList() {
        return personList;
    }

    public ArrayList<GroupMessage> getMessageList() {
        return messageList;
    }

    public Person findPerson(Account account, String state) {
        if (state.equals("logIn") || state.equals("updateImage") || state.equals("exit")) {
            for (var per : personList) {
                if (per.getMyAccount().getPort() == account.getPort()
                        && per.getMyAccount().getPassword().equals(account.getPassword())) {
                    return per;
                }
            }

        } else if (state.equals("addContact") || state.equals("newMember")) {
            for (var per : personList) {
                if (per.getMyAccount().getPort() == account.getPort()
                        && per.getMyAccount().getName().equals(account.getName())) {
                    return per;
                }
            }

        }
        return null;
    }

    public Person createPerson(Account account,boolean isAndroid) {
        account.setPort(UniquePort.getInstance().curr());
        account.setDatabaseIndex(personList.size());
        Person person = new Person(account, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        personList.add(person);
        person.getMyAccount().isAndroid = isAndroid;
        account.service = Executors.newFixedThreadPool(1);
        return person;
    }

    public GroupMessage newGroupMessage(ArrayList<Account> acc, String name, Image image) {
        boolean addAble = acc.size() == 1;
        ArrayList<Integer> personIndex = new ArrayList<>();
        for (var ac : acc) {
            int dbId = ac.getDatabaseIndex();
            personIndex.add(personList.get(dbId).getGroups().size());
        }
        GroupMessage groupMessage = new GroupMessage(new ArrayList<>(), acc, messageList.size()
                , new ArrayList<>(), name, image, addAble, personIndex);
        Message message = new Message("",-1,-1, LocalDate.now(), LocalTime.now(),
                false,null);
        groupMessage.getGroupMessage().add(message);
        messageList.add(groupMessage);
        return groupMessage;
    }

    public void saveFile() {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(fileName))) {
            objectOutputStream.writeObject(UniquePort.getInstance());
            objectOutputStream.writeObject(Database.getInstance());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readFromFile() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(fileName))) {
            UniquePort.setInstance((UniquePort) objectInputStream.readObject());
            Database.instance = (Database) objectInputStream.readObject();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        for(var person:personList) person.getMyAccount().service = Executors.newFixedThreadPool(1);
    }

}
