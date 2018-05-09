package com.neeraj.powerpointcontroller;


import java.util.UUID;

/**
 * Created by neeraj on 03-09-2016.
 */
public class Device {
    private String name;
    private String address;
    public static UUID getUUID(String uuidStr){
        long msb=0,lsb=0;
        int count=0;
        int i=0;
        while(i<uuidStr.length()){
            char c = uuidStr.charAt(i);
            int v;
            if(c>='0' && c<='9'){
                v = c - '0';
                if(count<16) {
                    msb = (msb << 4) + v;
                }
                else{
                    lsb = (lsb<<4) + v;
                }
                count++;
            }
            if(c>='a' && c<='f'){
                v = c - 'a' + 10;
                if(count<16) {
                    msb = (msb << 4) + v;
                }
                else{
                    lsb = (lsb<<4) + v;
                }
                count++;
            }
            i++;
        }
        return new UUID(msb,lsb);
    }
    Device(String name,String address){
        this.name = name;
        this.address = address;
    }
    public String getAddress(){
        return address;
    }
    public String getName(){
        return name;
    }
    public String toString(){
        return name;
    }
    public int hashCode(){
        return address.hashCode();
    }
    public boolean equals(Object o){
        return address.equals(o);
    }
}

