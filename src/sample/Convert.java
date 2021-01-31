package sample;

import sample.DataPackage.Account;
import sample.DataPackage.AccountAndroid;
import sample.DataPackage.MessagePackage.GroupMessage;
import sample.DataPackage.MessagePackage.GroupMessageAndroid;
import sample.DataPackage.MessagePackage.Message;
import sample.DataPackage.MessagePackage.MessageAndroid;
import sample.DataPackage.Person;
import sample.DataPackage.PersonAndroid;
import sample.TransferPackage.FromServer;
import sample.TransferPackage.FromServerAndroid;
import sample.TransferPackage.transferPackage;
import sample.TransferPackage.transferPackageAndroid;

import java.util.ArrayList;

public class Convert {
    public static Account convert(AccountAndroid a){
        if(a == null) return null;
        return new Account(a.getName(),a.getPassword(),a.getPort(),a.getDatabaseIndex(),null,a.fileId);
    }
    public static AccountAndroid convert(Account a){
        if(a == null) return null;
        return new AccountAndroid(a.getName(),a.getPassword(),a.getPort(),a.getDatabaseIndex(),a.fileId);
    }
    public static Message convert(MessageAndroid a){
        if(a == null) return null;
        return  new Message(a.getMessage(),a.getSenderIndex(),a.getSenderPort(),a.getDate(),
                a.getTime(),a.isFirst(),null,a.getFileName(),a.getFileIndex());
    }
    public static MessageAndroid convert(Message a){
        if(a == null) return null;
        return  new MessageAndroid(a.getMessage(),a.getSenderIndex(),a.getSenderPort(),a.getDate(),
                a.getTime(),a.isFirst(),a.getFileName(),a.getFileIndex());
    }
    public static GroupMessage convert(GroupMessageAndroid a){
        if(a == null) return null;
        ArrayList<Message> arr = null;
        if(a.getGroupMessage() != null){
            arr = new ArrayList<>();
            for(var obj:a.getGroupMessage()){
                arr.add(convert(obj));
            }
        }
        ArrayList<Account> arr2 = null;
        if(a.getParticipants() != null){
            arr2 = new ArrayList<>();
            for(var obj:a.getParticipants()) arr2.add(convert(obj));
        }
        GroupMessage gr;
        gr = new GroupMessage(arr,arr2,a.getDatabaseIndex(),a.getSharedFiles(),a.getGroupName(),null,
                a.isAddAble(),a.getPersonIndex());
        gr.lastMessageTime = a.lastMessageTime;
        gr.fileIndex = a.fileIndex;
        return gr;
    }
    public static GroupMessageAndroid convert(GroupMessage a){
        if(a == null) return null;
        ArrayList<MessageAndroid> arr = null;
        if(a.getGroupMessage() != null){
            arr = new ArrayList<>();
            for(var obj:a.getGroupMessage()){
                arr.add(convert(obj));
            }
        }
        ArrayList<AccountAndroid> arr2 = null;
        if(a.getParticipants() != null){
            arr2 = new ArrayList<>();
            for(var obj:a.getParticipants()) arr2.add(convert(obj));
        }
        GroupMessageAndroid gr = new GroupMessageAndroid(arr,arr2,a.getDatabaseIndex(),a.getSharedFiles(),a.getGroupName(),
                a.isAddAble(),a.getPersonIndex());
        gr.lastMessageTime = a.lastMessageTime;
        gr.fileIndex = a.fileIndex;
        return gr;
    }
    public  static Person convert(PersonAndroid p){
        if(p == null) return null;
        ArrayList<GroupMessage> arr = null;
        if(p.getGroups() != null){
            arr  = new ArrayList<>();
            for(var obj:p.getGroups()) arr.add(convert(obj));
        }
        return new Person(convert(p.getMyAccount()),arr,p.getParticipationIndex(),p.getUnseenCount(),p.getNewCount(),p.getSent(),p.getSet());
    }

    public  static PersonAndroid convert(Person p){
        if(p == null) return null;
        ArrayList<GroupMessageAndroid> arr = null;
        if(p.getGroups() != null){
            arr = new ArrayList<>();
            for(var obj:p.getGroups()) arr.add(convert(obj));
        }
        PersonAndroid pa = new PersonAndroid(convert(p.getMyAccount()),arr,p.getParticipationIndex(),p.getUnseenCount(),p.getNewCount(),p.getSent(),p.getSet());
        pa.messageCount = p.messageCount;
        return pa;
    }

    public static transferPackage convert(transferPackageAndroid p){
        if(p == null) return null;
        ArrayList<Message> arr = null;
        if(p.getMessages() != null){
            arr = new ArrayList<>();
            for(var obj:p.getMessages()) arr.add(convert(obj));
        }
        return  new transferPackage(p.getState(),convert(p.getAccount()),
                arr,null,p.getSenderId(),p.getReceiverId(),p.getCount(),p.getNewCount(),p.getArchivedMessageSet());
    }

    public static transferPackageAndroid convert(transferPackage p){
        if(p == null) return null;
        ArrayList<MessageAndroid> arr = null;
        if(p.getMessages() != null){
            arr = new ArrayList<>();
            for(var obj:p.getMessages()) arr.add(convert(obj));
        }
        return  new transferPackageAndroid(p.getState(),convert(p.getAccount()),
                arr,p.getSenderId(),p.getReceiverId(),p.getCount(),p.getNewCount());
    }

    public static FromServer convert(FromServerAndroid fr){
        if(fr == null) return null;
        ArrayList<Message> arr = null ;
        if(fr.getGroupMessage() != null){
            arr = new ArrayList<>();
            for(var obj:fr.getMessage()) arr.add(Convert.convert(obj));
        }
        return new FromServer(fr.getState(),Convert.convert(fr.getGroupMessage()),arr,null,
                fr.getToBeModified(),fr.getAnother());
    }
    public static FromServerAndroid convert(FromServer fr){
        if(fr == null) return null;
        ArrayList<MessageAndroid> arr = null ;
        if(fr.getMessage() != null){
            arr = new ArrayList<>();
            for(var obj:fr.getMessage()) arr.add(Convert.convert(obj));
        }
        return new FromServerAndroid(fr.getState(),Convert.convert(fr.getGroupMessage()),arr,
                fr.getToBeModified(),fr.getAnother());
    }
}
