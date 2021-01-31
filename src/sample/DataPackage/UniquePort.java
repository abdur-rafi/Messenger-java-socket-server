package sample.DataPackage;

import java.io.Serializable;

public class UniquePort implements Serializable {
    private static UniquePort instance = new UniquePort();
    public static UniquePort getInstance(){
        return instance;
    }
    public static void setInstance(UniquePort instance){
        UniquePort.instance = instance;
    }
    private long serialVersionUID = 10L;
    private int curr = 1000;
    private int increment = 1;
    public int curr(){
        curr += increment;
        return curr-increment;
    }
}

