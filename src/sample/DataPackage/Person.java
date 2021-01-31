package sample.DataPackage;

import sample.DataPackage.MessagePackage.GroupMessage;
import sample.DataPackage.MessagePackage.GroupMessageAndroid;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Person implements Serializable {
    private Account myAccount;
    private ArrayList<GroupMessage> groups;
    private ArrayList<Integer> participationIndex;
    private ArrayList<Integer> unseenCount;
    private ArrayList<Integer> newCount;
    private ArrayList<Integer> sent;
    public int messageCount = 0;
    private long serialVersionUID = 2L;
    private Set<Integer> set = new HashSet<>();

    public Account getMyAccount() {
        return myAccount;
    }

    public void setMyAccount(Account myAccount) {
        this.myAccount = myAccount;
    }

    public ArrayList<GroupMessage> getGroups() {
        return groups;
    }

    public void setGroups(ArrayList<GroupMessage> groups) {
        this.groups = groups;
    }

    public ArrayList<Integer> getParticipationIndex() {
        return participationIndex;
    }

    public ArrayList<Integer> getUnseenCount() {
        return unseenCount;
    }

    public void setUnseenCount(ArrayList<Integer> unseenCount) {
        this.unseenCount = unseenCount;
    }

    public void setParticipationIndex(ArrayList<Integer> participationIndex) {
        this.participationIndex = participationIndex;
    }

    public ArrayList<Integer> getNewCount() {
        return newCount;
    }

    public void setNewCount(ArrayList<Integer> newCount) {
        this.newCount = newCount;
    }

    public ArrayList<Integer> getSent() {
        return sent;
    }

    public void setSent(ArrayList<Integer> sent) {
        this.sent = sent;
    }

    public Set<Integer> getSet() {
        return set;
    }

    public void setSet(Set<Integer> set) {
        this.set = set;
    }

    public Person(Account myAccount, ArrayList<GroupMessage> groups, ArrayList<Integer> participationIndex, ArrayList<Integer> unseenCount, ArrayList<Integer> newCount, ArrayList<Integer> sent) {
        this.myAccount = myAccount;
        this.groups = groups;
        this.participationIndex = participationIndex;
        this.unseenCount = unseenCount;
        this.newCount = newCount;
        this.sent = sent;
    }

    public Person(Account myAccount, ArrayList<GroupMessage> groups, ArrayList<Integer> participationIndex, ArrayList<Integer> unseenCount, ArrayList<Integer> newCount, ArrayList<Integer> sent,Set<Integer> s) {
        this.myAccount = myAccount;
        this.groups = groups;
        this.participationIndex = participationIndex;
        this.unseenCount = unseenCount;
        this.newCount = newCount;
        this.sent = sent;
        this.set = s;
    }

    public void addGroupMessage(GroupMessage groupMessage){
        groups.add(groupMessage);
    }
}
