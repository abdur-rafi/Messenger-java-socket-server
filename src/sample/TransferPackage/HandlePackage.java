package sample.TransferPackage;

import javafx.scene.image.Image;
import sample.Convert;
import sample.DataPackage.Account;
import sample.DataPackage.Database;
import sample.DataPackage.MessagePackage.GroupMessage;
import sample.DataPackage.MessagePackage.Message;
import sample.DataPackage.Person;
import sample.Main;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.Integer.max;

public class HandlePackage extends Thread {
    private transferPackage p;
    private final DatagramSocket socket;
    private final DatagramPacket packet;
    private final byte[] buffer;
    private final int receiveATime = 15;
    private final boolean isAndroid;

    public HandlePackage(DatagramSocket socket, DatagramPacket packet, byte[] buffer, boolean isAndroid) {
        this.socket = socket;
        this.packet = packet;
        this.buffer = buffer;
        this.isAndroid = isAndroid;
    }

    @Override
    public void run() {
        try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(buffer))) {
            if (isAndroid) {
                transferPackageAndroid p2 = (transferPackageAndroid) inputStream.readObject();
                p = Convert.convert(p2);
            } else
                p = (transferPackage) inputStream.readObject();
            System.out.println("   ======================== Request for " + p.getState() + " ===========================");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (p.getState().equals("addSocket")) {
            Account account = Database.getInstance().getPersonList().get(p.getSenderId()).getMyAccount();
            account.address = packet.getAddress();
            account.isActive = true;
            account.key = packet.getPort();
            account.datagramSocket = socket;

            Main.printPerson(account);

            Set<Integer> st = new HashSet<>();
            for (var grMessage : Database.getInstance().getPersonList().get(account.getDatabaseIndex()).getGroups()) {
                for (var per : grMessage.getParticipants()) {
                    if (per.getPort() != account.getPort() && !st.contains(per.getPort())) {
                        st.add(per.getPort());
                        if (per.isActive) {
                            FromServer fromServer = new FromServer("active", null, null, null,
                                    account.getDatabaseIndex(), 1);
                            if (!per.isAndroid)
                                SendFromServer.sendFromServer(fromServer, per.key, per.address, per.datagramSocket);
                            else {
                                FromServerAndroid fr = Convert.convert(fromServer);
                                SendFromServer.sendFromServer(fr, per.key, per.address, per.datagramSocket);
                            }
                        }
                    }
                }
            }

        } else if (p.getState().equals("addContact")) {
            Person person2 = Database.getInstance().findPerson(p.getAccount(), p.getState());
            Person person1 = Database.getInstance().getPersonList().get(p.getSenderId());
            System.out.println("  requested by : ");
            Main.printPerson(person1);
            if (person2 == null) {
                System.out.println("  ==================== No User Found =======================");
                FromServer fromServer = new FromServer(p.getState(), null, null, null, -1, -1);
                if(!person1.getMyAccount().isAndroid)
                    SendFromServer.sendFromServer(fromServer,person1.getMyAccount().key,person1.getMyAccount().address,person1.getMyAccount().datagramSocket);
                else{
                    FromServerAndroid frs = Convert.convert(fromServer);
                    SendFromServer.sendFromServer(frs,person1.getMyAccount().key,person1.getMyAccount().address,person1.getMyAccount().datagramSocket);
                }
                return;
            }

            System.out.println("  requested for : ");
            Main.printPerson(person2);
            person1.getParticipationIndex().add(0);
            person1.getNewCount().add(++person1.messageCount);
            person1.getSent().add(0);
            person1.getUnseenCount().add(0);
            person2.getParticipationIndex().add(1);
            person2.getNewCount().add(++person2.messageCount);
            person2.getSent().add(0);
            person2.getUnseenCount().add(0);
            ArrayList<Account> acc = new ArrayList<>();
            acc.add(person1.getMyAccount());
            acc.add(person2.getMyAccount());
            GroupMessage groupMessage = Database.getInstance().newGroupMessage(acc, null, null);
            groupMessage.lastMessageTime = LocalDateTime.now();
            person1.getGroups().add(groupMessage);
            person2.getGroups().add(groupMessage);
            int i = 0;
            for (var sendTo : groupMessage.getParticipants()) {
                int port = sendTo.key;
                FromServer fromServer = new FromServer(p.getState(), null, null, null, i++, -1);
                if (sendTo.isActive) {
                    if (!sendTo.isAndroid)
                        SendFromServer.sendFromServer(fromServer, port, sendTo.address, sendTo.datagramSocket);
                    else {
                        FromServerAndroid fr = Convert.convert(fromServer);
                        SendFromServer.sendFromServer(fr, port, sendTo.address, sendTo.datagramSocket);
                    }
                    Thread thread = new Thread(()->{
                        try {
                            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(sendTo.socket.getOutputStream()));
                            if(sendTo.isAndroid) oos.writeObject(Convert.convert(groupMessage));
                            else oos.writeObject(groupMessage);
                            oos.flush();
                            oos.writeInt(groupMessage.getParticipants().size() - 1);
                            oos.flush();
                            System.out.println(" ================ Sending " + (groupMessage.getParticipants().size()-1) + " files ================");
                            for (var acc2 : groupMessage.getParticipants()) {
                                if (sendTo.getPort() != acc2.getPort())
                                    Main.sendFile(new File("files/" + acc2.fileId), sendTo.socket,oos);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    sendTo.service.execute(thread);
                }
            }
        } else if (p.getState().equals("save")) {
            Database.getInstance().saveFile();
        } else if (p.getState().equals("updateImage")) {
            Person person = Database.getInstance().getPersonList().get(p.getSenderId());
            System.out.println("  Change Image requested by : ");
            Main.printPerson(person);
            Socket socket = person.getMyAccount().socket;
            Main.receiveFile(socket, Integer.toString(person.getMyAccount().fileId),null);
            Image image = p.getImage();
            person.getMyAccount().setImage(image);
            FromServer fromServer = new FromServer(p.getState(), null, null, p.getImage(), person.getMyAccount().getPort(), person.getMyAccount().fileId);
            Set<Integer> st = new HashSet<>();
            for (var groups : person.getGroups()) {
                for (var acc : groups.getParticipants()) {
                    if (acc.getPort() != person.getMyAccount().getPort() && acc.isActive && !st.contains(acc.getPort())) {
                        st.add(acc.getPort());
                        if (!acc.isAndroid)
                            SendFromServer.sendFromServer(fromServer, acc.key, acc.address, acc.datagramSocket);
                        else {
                            FromServerAndroid fr = Convert.convert(fromServer);
                            SendFromServer.sendFromServer(fr, acc.key, acc.address, acc.datagramSocket);
                        }
                        Socket socket1 = acc.socket;
                        System.out.println("  sending updated image to : ");
                        Main.printPerson(acc);
                        Thread thread = new Thread(()-> Main.sendFile(new File("files/" + person.getMyAccount().fileId), socket1,null));
                        System.out.println(acc.service);
                        acc.service.execute(thread);
                    }
                }
            }
        } else if (p.getState().equals("message")) {
            handleMessage();
        } else if (p.getState().equals("newGroup")) {
            String name = p.getAccount().getName();
            Image image = p.getAccount().getImage();
            Person person = Database.getInstance().getPersonList().get(p.getSenderId());
            System.out.println("  requested by : ");
            Main.printPerson(person);
            int curr_file = Database.getInstance().fileId++;
            Main.receiveFile(person.getMyAccount().socket, Integer.toString(curr_file),null);
            person.getUnseenCount().add(0);
            person.getNewCount().add(++person.messageCount);
            person.getSent().add(0);
            person.getParticipationIndex().add(0);
            ArrayList<Account> acc = new ArrayList<>();
            acc.add(person.getMyAccount());
            GroupMessage groupMessage = Database.getInstance().newGroupMessage(acc, name, image);
            groupMessage.lastMessageTime = LocalDateTime.now();
            groupMessage.fileIndex = curr_file;
            person.addGroupMessage(groupMessage);
            int port = person.getMyAccount().key;
            FromServer fromServer = new FromServer(p.getState(), null, null, null, 0, -1);
            System.out.println("sending image file to : ");
            Main.printPerson(person);
            if (person.getMyAccount().isActive) {
                if (!person.getMyAccount().isAndroid) {
                    SendFromServer.sendFromServer(fromServer, port, person.getMyAccount().address, person.getMyAccount().datagramSocket);
                } else {
                    FromServerAndroid fr = Convert.convert(fromServer);
                    SendFromServer.sendFromServer(fr, port, person.getMyAccount().address, person.getMyAccount().datagramSocket);
                }
                Thread thread = new Thread(()->{
                    ObjectOutputStream oos = null;
                    try {
                        oos = new ObjectOutputStream(new BufferedOutputStream(person.getMyAccount().socket.getOutputStream()));
                        if(!person.getMyAccount().isAndroid)
                            oos.writeObject(groupMessage);
                        else{
                            oos.writeObject(Convert.convert(groupMessage));
                        }
                        oos.flush();
                        oos.writeInt(1);
                        oos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Main.sendFile(new File("files/" + groupMessage.fileIndex), person.getMyAccount().socket,oos);
                });
                person.getMyAccount().service.execute(thread);


            }
        } else if (p.getState().equals("newMember")) {
            Person person = Database.getInstance().findPerson(p.getAccount(), p.getState());
            int index = p.getSenderId();
            int index2 = p.getReceiverId();
            Person person1 = Database.getInstance().getPersonList().get(index2);
            System.out.println("  requested by  : ");
            Main.printPerson(person1);
            if (person == null) {
                System.out.println("  ==================== No User Found ======================");
                FromServer fromServer = new FromServer("newMember", null
                        , null, null, -1, -1);
                if (!person1.getMyAccount().isAndroid)
                    SendFromServer.sendFromServer(fromServer, person1.getMyAccount().key, person1.getMyAccount().address,
                            person1.getMyAccount().datagramSocket);
                else {
                    FromServerAndroid fr = Convert.convert(fromServer);
                    SendFromServer.sendFromServer(fr, person1.getMyAccount().key, person1.getMyAccount().address,
                            person1.getMyAccount().datagramSocket);
                }
                return;
            }
            System.out.println("  found User : ");
            Main.printPerson(person);
            GroupMessage groupMessage = Database.getInstance().getMessageList().get(index);
            ArrayList<Account> participants = groupMessage.getParticipants();
            groupMessage.getPersonIndex().add(person.getGroups().size());
            participants.add(person.getMyAccount());
            person.addGroupMessage(groupMessage);
            person.getParticipationIndex().add(participants.size() - 1);
            person.getUnseenCount().add(0);
            person.getNewCount().add(++person.messageCount);
            person.getSent().add(0);
            FromServer fromServer = new FromServer("newGroup", null
                    , null, null, participants.size() - 1, -1);
            if (person.getMyAccount().isActive) {
                System.out.println("  ======== Sending Group to new Member ======== ");
                if (!person.getMyAccount().isAndroid)
                    SendFromServer.sendFromServer(fromServer, person.getMyAccount().key, person.getMyAccount().address,
                            person.getMyAccount().datagramSocket);
                else {
                    FromServerAndroid fr = Convert.convert(fromServer);
                    SendFromServer.sendFromServer(fr, person.getMyAccount().key, person.getMyAccount().address,
                            person.getMyAccount().datagramSocket);
                }
                Socket socket = person.getMyAccount().socket;
                person.getMyAccount().service.execute(new Thread(()->{
                    ObjectOutputStream oos;
                    try {
                        oos = new ObjectOutputStream(new BufferedOutputStream(person.getMyAccount().socket.getOutputStream()));
                        if(!person.getMyAccount().isAndroid) oos.writeObject(groupMessage);
                        else oos.writeObject(Convert.convert(groupMessage));
                        oos.flush();
                        oos.writeInt(groupMessage.getParticipants().size());
                        oos.flush();
                        for (int i = 0; i < groupMessage.getParticipants().size() - 1; ++i) {
                            Main.sendFile(new File("files/" + groupMessage.getParticipants().get(i).fileId), socket,oos);
                        }
                        Main.sendFile(new File("files/" + groupMessage.fileIndex), socket,oos);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));
            }
            ArrayList<Account> acc = new ArrayList<>();
            acc.add(person.getMyAccount());
            GroupMessage toBeSent = new GroupMessage(null, acc
                    , groupMessage.getDatabaseIndex(), null, null, null, true, null);
            FromServer fromServer1 = new FromServer(p.getState(), toBeSent, null, null,
                    groupMessage.getDatabaseIndex(), -1);

            for (int i = 0; i < participants.size() - 1; ++i) {
                if (participants.get(i).isActive) {
                    System.out.println("  Sending new Member Info to : ");
                    Main.printPerson(participants.get(i));
                    if (!participants.get(i).isAndroid)
                        SendFromServer.sendFromServer(fromServer1, participants.get(i).key, participants.get(i).address,
                                participants.get(i).datagramSocket);
                    else {
                        FromServerAndroid fr = Convert.convert(fromServer1);
                        SendFromServer.sendFromServer(fr, participants.get(i).key, participants.get(i).address,
                                participants.get(i).datagramSocket);
                    }
                    int j = i;
                    person.getMyAccount().service.execute(new Thread(()-> Main.sendFile(new File("files/" + person.getMyAccount().fileId), participants.get(j)
                            .socket,null)));

                }
            }

        } else if (p.getState().equals("exit")) {
            System.out.println("  Active status off of : ");
            Person person = Database.getInstance().getPersonList().get(p.getSenderId());
            Main.printPerson(person);
            FromServer fromServer = new FromServer(p.getState(), null, null, null, -1, -1);
            if (!person.getMyAccount().isAndroid)
                SendFromServer.sendFromServer(fromServer, person.getMyAccount().key, person.getMyAccount().address
                        , person.getMyAccount().datagramSocket);
            else {
                FromServerAndroid fr = Convert.convert(fromServer);
                SendFromServer.sendFromServer(fr, person.getMyAccount().key, person.getMyAccount().address
                        , person.getMyAccount().datagramSocket);
            }
            person.getMyAccount().isActive = false;
            person.getMyAccount().address = null;
            person.getMyAccount().socket = null;
            person.getMyAccount().datagramSocket = null;
            person.setUnseenCount(p.getCount());
            person.setNewCount(p.getNewCount());
            person.messageCount = 0;
            if(p.getArchivedMessageSet() != null)
                person.setSet(p.getArchivedMessageSet());
            for (var obj : p.getNewCount()) {
                person.messageCount = max(person.messageCount, obj);
            }
            Account account = person.getMyAccount();
            Set<Integer> st = new HashSet<>();
            for (var grMessage : Database.getInstance().getPersonList().get(account.getDatabaseIndex()).getGroups()) {
                for (var per : grMessage.getParticipants()) {
                    if (per.getPort() != account.getPort() && !st.contains(per.getPort())) {
                        st.add(per.getPort());
                        if (per.isActive) {
                            FromServer fromServer1 = new FromServer("active", null, null, null,
                                    account.getPort(), 0);
                            if (!per.isAndroid)
                                SendFromServer.sendFromServer(fromServer1, per.key, per.address, per.datagramSocket);
                            else {
                                FromServerAndroid fr = Convert.convert(fromServer1);
                                SendFromServer.sendFromServer(fr, per.key, per.address, per.datagramSocket);
                            }
                        }
                    }
                }
            }
        } else if (p.getState().equals("imageMessage")) {
            Person person = Database.getInstance().getPersonList().get(p.getSenderId());
            Socket socket = person.getMyAccount().socket;
            try {
                ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                transferPackage packet = (transferPackage) ois.readObject();
                GroupMessage groupMessage = Database.getInstance().getMessageList().get(packet.getSenderId());
                groupMessage.lastMessageTime = LocalDateTime.now();
                packet.getMessages().get(0).setTime(LocalTime.now());
                packet.getMessages().get(0).setDate(LocalDate.now());
                groupMessage.getGroupMessage().add(packet.getMessages().get(0));
                int i = 0;
                for (var acc : groupMessage.getParticipants()) {
                    int dbId = acc.getDatabaseIndex();
                    int personIndex = groupMessage.getPersonIndex().get(i++);
                    Person person1 = Database.getInstance().getPersonList().get(dbId);
                    person1.getNewCount().set(personIndex, ++person1.messageCount);
                    person1.getUnseenCount().set(personIndex, person1.getUnseenCount().get(personIndex) + 1);
                    FromServer server = new FromServer(packet.getState(), null, null,
                            null, -1, -1);
                    if (acc.isActive) {
                        if (!acc.isAndroid)
                            SendFromServer.sendFromServer(server, acc.key, acc.address, acc.datagramSocket);
                        else {
                            FromServerAndroid fr = Convert.convert(server);
                            SendFromServer.sendFromServer(fr, acc.key, acc.address, acc.datagramSocket);

                        }
                        FromServer server1 = new FromServer(packet.getState(), null, packet.getMessages(),
                                null, groupMessage.getDatabaseIndex(), -1);
                        Socket socket1 = acc.socket;

                        acc.service.execute(new Thread(()->{
                            try {
                                ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(socket1.getOutputStream()));
                                oos.writeObject(server1);
                                oos.flush();
                            } catch (IOException e){
                                e.printStackTrace();
                            }
                        }));

                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else if (p.getState().equals("receiveMessage")) {
            Person person = Database.getInstance().getPersonList().get(p.getSenderId());
            System.out.println("  message request from    : ");
            Main.printPerson(person);
            Socket socket = person.getMyAccount().socket;
            int sent = person.getSent().get(p.getReceiverId());
            int to = max(0, sent - receiveATime);
            List<Message> chunk = person.getGroups().get(p.getReceiverId()).getGroupMessage().subList(to, sent);
            person.getSent().set(p.getReceiverId(), person.getSent().get(p.getReceiverId()) - chunk.size());
            ArrayList<Message> arr = new ArrayList<>(chunk);
            person.getMyAccount().service.execute(new Thread(()->{
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                    oos.writeObject(arr.size());
                    oos.flush();
                    for (var obj : arr) {
                        oos.writeObject(obj);
                        oos.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        } else if (p.getState().equals("fileMessage")) {
            Socket socket = Database.getInstance().getPersonList().get(p.getReceiverId()).getMyAccount().socket;
            System.out.println("  file message from    : ");
            Main.printPerson(Database.getInstance().getPersonList().get(p.getReceiverId()));
            Database.getInstance().getPersonList().get(p.getReceiverId()).getMyAccount().service.execute(new Thread(()->{
                try {
                    System.out.println("   ============ receiving file as message ==============");
                    Message message = p.getMessages().get(0);
                    message.setMessage("file Sent");
                    int j = Database.getInstance().fileId++;
                    message.setFileIndex(j);
                    p.setState("message");
                    int count;
                    ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                    Long size = ois.readLong();
                    long size2 = size;
                    byte[] buffer = new byte[100000];
                    FileOutputStream fos = new FileOutputStream("files/" + j);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    while (size > 0 && (count = ois.read(buffer)) > 0) {
                        bos.write(buffer, 0, count);
                        size -= count;
                    }
                    System.out.println("   file info     :  " +
                            "   file name          : " + j + "\n"+
                            "   file size          : " + size2
                            );
                    bos.close();
                    handleMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));

        } else if (p.getState().equals("downloadFile")) {
            Person person = Database.getInstance().getPersonList().get(p.getSenderId());
            System.out.println("  download file request from   : ");
            Main.printPerson(person);
            Socket socket = person.getMyAccount().socket;
            person.getMyAccount().service.execute(new Thread(()->{
                byte[] buffer = new byte[100000];
                try {
                    System.out.println("   ============ sending file as message ==============");
                    File file = new File("files/" + p.getReceiverId());
                    FileInputStream fis = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    OutputStream os = socket.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(os);
                    dos.writeLong(file.length());
                    int count;
                    while ((count = bis.read(buffer)) > 0) {
                        dos.write(buffer, 0, count);
                    }
                    Main.fileDetails(file);
                    os.flush();
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));

        } else if (p.getState().equals("seen")) {
            Database.getInstance().getPersonList().get(p.getSenderId()).getUnseenCount().set(p.getReceiverId(), 0);
        }
    }

    private void sendDummy(Person person) {
        FromServer fromServer = new FromServer("notFound", null, null, null, -1, -1);
        if (!person.getMyAccount().isAndroid)
            SendFromServer.sendFromServer(fromServer, person.getMyAccount().key, person.getMyAccount().address
                    , person.getMyAccount().datagramSocket);
        else {
            FromServerAndroid fr = Convert.convert(fromServer);
            SendFromServer.sendFromServer(fr, person.getMyAccount().key, person.getMyAccount().address
                    , person.getMyAccount().datagramSocket);

        }
    }

    private void handleMessage() {
        GroupMessage groupMessage = Database.getInstance().getMessageList().get(p.getSenderId());
        p.getMessages().get(0).setDate(LocalDate.now());
        p.getMessages().get(0).setTime(LocalTime.now());
        groupMessage.lastMessageTime = LocalDateTime.now();
        groupMessage.getGroupMessage().add(p.getMessages().get(0));
        int i = 0;
        for (var acc : groupMessage.getParticipants()) {
            int dbId = acc.getDatabaseIndex();
            int personIndex = groupMessage.getPersonIndex().get(i);
            Person person = Database.getInstance().getPersonList().get(dbId);
            person.getNewCount().set(personIndex, ++person.messageCount);
            person.getUnseenCount().set(personIndex, person.getUnseenCount().get(personIndex) + 1);
            FromServer server = new FromServer(p.getState(), null, p.getMessages(),
                    null, groupMessage.getDatabaseIndex(), -1);
            if (acc.isActive) {
                if (!acc.isAndroid)
                    SendFromServer.sendFromServer(server, acc.key, acc.address, acc.datagramSocket);
                else {
                    FromServerAndroid fr = Convert.convert(server);
                    SendFromServer.sendFromServer(fr, acc.key, acc.address, acc.datagramSocket);
                }
            }
            ++i;
        }
    }
}
