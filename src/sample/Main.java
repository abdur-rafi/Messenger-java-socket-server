package sample;


import sample.DataPackage.Account;
import sample.DataPackage.Database;
import sample.DataPackage.MessagePackage.Message;
import sample.DataPackage.Person;
import sample.TransferPackage.HandlePackage;
import sample.TransferPackage.transferPackage;
import sample.TransferPackage.transferPackageAndroid;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class Main {
    public static void main(String[] args) throws Exception {
        Database.getInstance().readFromFile();
        try {
            Files.createDirectories(Paths.get("files"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(4003)) {
                while (true) {
                    Socket socket = serverSocket.accept();
                    ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                    transferPackage p = (transferPackage) ois.readObject();
                    handle(p, socket, false, ois);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(4010)) {
                while (true) {
                    Socket socket = serverSocket.accept();
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    transferPackageAndroid p = (transferPackageAndroid) ois.readObject();
                    handle(Convert.convert(p), socket, true, ois);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(4020)) {
                while (true) {
                    byte[] buffer = new byte[100000];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    new HandlePackage(socket, packet, buffer, true).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        try (DatagramSocket socket = new DatagramSocket(4000)) {
            while (true) {
                byte[] buffer = new byte[100000];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                new HandlePackage(socket, packet, buffer, false).start();
            }
        }

    }

    public static void handle(transferPackage p, Socket socket, boolean isAndroid, ObjectInputStream ois) {
        System.out.println("============================ " + p.getState() + " request ============================ ");
        Person person;
        int curr_file = -1;
        if (p.getState().equals("logIn") || p.getState().equals("createUser")) {
            if (p.getState().equals("logIn"))
                person = Database.getInstance().findPerson(p.getAccount(), p.getState());
            else person = Database.getInstance().createPerson(p.getAccount(), isAndroid);
            ArrayList<Message> messages[] = new ArrayList[0];

            if (person != null) {
                person.getMyAccount().isAndroid = isAndroid;
                person.getMyAccount().socket = socket;
                messages = new ArrayList[person.getGroups().size()];
                int i = 0;
                if (!isAndroid) {
                    for (var obj : person.getGroups()) {
                        person.getSent().set(i, obj.getGroupMessage().size());
                        messages[i++] = new ArrayList<>(obj.getGroupMessage());
                        obj.setGroupMessage(new ArrayList<Message>());
                    }
                }
                if(p.getState().equals("createUser")){
                    System.out.println("   ================ Account Created ===================");
                    curr_file = person.getMyAccount().fileId = Database.getInstance().fileId;
                    ++Database.getInstance().fileId;
                }
                else{
                    System.out.println("   ================ Found User =================");
                }
                printPerson(person);
            }
            else{
                System.out.println("   ================ No User Found =================");
            }
            ObjectOutputStream oos = null;
            try {
                oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                if (!isAndroid)
                    oos.writeObject(person);
                else
                    oos.writeObject(Convert.convert(person));
                oos.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
            if(person == null) return;

            if (p.getState().equals("createUser")) {
                if(!isAndroid)
                    receiveFile(socket,Integer.toString(curr_file),ois);
                else{
                    try{
                        FileInputStream fis = new FileInputStream("src/sample/defaultAccountImage.png");
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        FileOutputStream fos = new FileOutputStream("files/"+ curr_file);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                        int count;
                        byte[] buffer = new byte[100000];
                        while ((count = bis.read(buffer)) > 0) {
                            bos.write(buffer, 0, count);
                        }
                        bos.close();
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                    sendFile(new File("files/"+curr_file),socket,oos);
                }
            }
            else if (p.getState().equals("logIn")) {
                Set<Integer> s = new HashSet<>();
                s.add(person.getMyAccount().fileId);
                for (var groups : person.getGroups()) {
                    for (var acc : groups.getParticipants()) s.add(acc.fileId);
                    if(groups.isAddAble()) s.add(groups.fileIndex);
                }
                try {
                    oos.writeInt(s.size());
                    oos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (var id : s) sendFile(new File("files/" + id), socket,oos);
            }

            if (person != null && !isAndroid) {
                int i = 0;
                for (var obj : person.getGroups()) {
                    obj.setGroupMessage(messages[i++]);
                }
            }
        } else if (p.getState().equals("imageMessage")) {
            System.out.println(p.getImage().getHeight());
        }
    }

    public static void receiveFile(Socket socket, String fileName,ObjectInputStream ois) {
        try {
            if(ois == null){
                ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            }
            System.out.println("   ==================== Receiving File ======================");
            int count = 0;
            String name = ois.readUTF();
            if (fileName != null) name = fileName;
            Long size = ois.readLong();
            long size2 = size;

            byte[] buffer = new byte[100000];
            FileOutputStream fos = new FileOutputStream("files/" + name);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            while (size > 0 && (count = ois.read(buffer, 0, (int) Math.min(buffer.length, size))) > 0) {
                bos.write(buffer, 0, count);
                size -= count;
            }

            System.out.println("   File Info : \n"
                    + "   name        : " + name + "\n"
                    + "   size        : " + size2 + " bytes"
            );

            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendFile(File file, Socket socket,ObjectOutputStream oos) {
        byte[] buffer = new byte[100000];

        try {
            if(oos == null){
                oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            }
            System.out.println("   ==================== Sending File ======================");

            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            oos.writeUTF(file.getName());
            oos.flush();
            oos.writeLong(file.length());
            oos.flush();
            double sz = file.length();
            double s = 0L;
            int count;
            while ((count = bis.read(buffer)) > 0) {
                oos.write(buffer, 0, count);
            }
            oos.flush();
            bis.close();
            fileDetails(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printPerson(Person person){
        System.out.println("      Person Info : \n"+
            "      Name               : " + person.getMyAccount().getName() + "\n"+
            "      Port               : " + person.getMyAccount().getPort()
        );
    }
    public static void printPerson(Account acc){
        System.out.println("      Person Info : \n"+
                "      Name               : " + acc.getName() + "\n"+
                "      Port               : " + acc.getPort()
        );
    }
    public static void fileDetails(File file){
        System.out.println("   File Info : \n"
                + "   name        : " + file.getName() + "\n"
                + "   size        : " + file.length() + " bytes"
        );
    }

}
