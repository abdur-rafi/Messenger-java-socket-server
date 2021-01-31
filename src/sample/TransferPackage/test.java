package sample.TransferPackage;

import java.io.Serializable;

public class test implements Serializable{
    byte[] arr;
    private long serialVersionUID = 7L;
    public test(int n){
        arr = new byte[n];
    }
    public test(byte [] b){
        arr = b;
    }
}
