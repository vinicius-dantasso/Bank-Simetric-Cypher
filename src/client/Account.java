package client;

import java.io.Serializable;

public class Account implements Serializable{
    
    //private User userInfo;
    private String email;
    private String password;
    private double balance;

    public Account(String e, String p) {
        //this.setUserInfo(i);
        this.setEmail(e);
        this.setPassword(p);
        this.setBalance(0.0);
    }

    public Account(String e, String p, double b) {
        //this.setUserInfo(i);
        this.setEmail(e);
        this.setPassword(p);
        this.setBalance(b);
    }

    // public User getUserInfo() {
    //     return this.userInfo;
    // }

    // public void setUserInfo(User userInfo) {
    //     this.userInfo = userInfo;
    // }    

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getBalance() {
        return this.balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String toString() {
        return getEmail() + "//" + getPassword() + "//" + getBalance();
    }

}
