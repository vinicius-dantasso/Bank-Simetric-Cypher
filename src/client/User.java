package client;

import java.io.Serializable;

public class User implements Serializable {
    
    private String cpf;
    private String name;
    private String address;
    private String phoneNumber;

    public User(String c, String n, String a, String p) {
        this.setCpf(c);
        this.setName(n);
        this.setAddress(a);
        this.setPhoneNumber(p);
    }

    public String getCpf() {
        return this.cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

}
