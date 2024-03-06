package service;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import client.Account;
import criptos.AES;
import criptos.Vernam;
import handlers.ClientHandler;
import hmac.HMAC;

public class ClientService {

    private ClientHandler myClient;

    private File hMacKey;
    private File vernamKey = new File("src\\keys\\vernamKey.txt");
    private File aesKey = new File("src\\keys\\aesKey.txt");

    private Scanner fileReader;
    private Scanner sc;

    private String msg;
    private String hMacAddress;

    public ClientService(ClientHandler ch) {
        this.myClient = ch;
    }
    
    public void clientOption(Account account, Scanner sc, PrintStream printer) {

        System.out.println("============================================");
        System.out.println("Bem vindo ao Banco dos Bancos!");
        System.out.println("[1] para ver seu saldo;");
        System.out.println("[2] para fazer um saque;");
        System.out.println("[3] para depositar um valor;");
        System.out.println("[4] para fazer uma transferência;");
        System.out.println("[5] para ver seus investimentos.");
        System.out.println("============================================");
        msg = sc.nextLine();

        if(myClient.getIntruder()) {
            hMacKey = new File("src\\keys\\falseKey.txt");
        }
        else {
            hMacKey = new File("src\\keys\\hMacKey.txt");
        }

        int option = Integer.parseInt(msg);
        switch(option) {

            case 1  -> { getClientBalance(account, printer); }
            case 2  -> { getWithdraw(account, printer); }
            case 3  -> { getDeposit(account, printer); }
            case 4  -> { getTransferency(account, printer); }
            case 5  -> { getInvestments(account, printer); }
            default -> {}

        }

    }

    private void getClientBalance(Account account, PrintStream printer) {

        try{

            String key, msg;

            // Retirada do HMac
            fileReader = new Scanner(hMacKey);

            if(myClient.getIntruder()) {
                msg = "getBalance//noAccount//" + myClient.getId();
            }
            else {
                msg = "getBalance//" + account.getEmail() + "//" + myClient.getId();
            }
            
            key = fileReader.nextLine();
            hMacAddress = HMAC.hMac(key, msg);

            // Criptografia na cifra de Vernam
            fileReader = new Scanner(vernamKey);

            key = fileReader.nextLine();
            msg = Vernam.encrypt(msg, key);

            // Criptografia na cifra AES
            fileReader = new Scanner(aesKey);

            key = fileReader.nextLine();
            msg = AES.encrypt(msg, key);

            // Envio da mensagem criptografada junto de seu HMac
            msg = hMacAddress + "//" + msg;
            printer.println(msg);
            fileReader.close();

        }
        catch(IOException e){
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getWithdraw(Account account, PrintStream printer) {

        try {

            sc = new Scanner(System.in);
            String key, msg;
            double amount;

            System.out.println("Quanto você deseja sacar?");
            amount = Double.parseDouble(sc.nextLine());

            if(myClient.getIntruder()) {
                msg = "getWithdraw//noAccount//"+myClient.getId()+"//"+amount;
            }
            else {
                msg = "getWithdraw//"+account.getEmail()+"//"+myClient.getId()+"//"+amount;
            }

            // Retirada do HMac da mensagem
            fileReader = new Scanner(hMacKey);

            key = fileReader.nextLine();
            hMacAddress = HMAC.hMac(key, msg);

            // Criptografia na cifra de Vernam
            fileReader = new Scanner(vernamKey);

            key = fileReader.nextLine();
            msg = Vernam.encrypt(msg, key);

            // Criptografia na cifra AES
            fileReader = new Scanner(aesKey);

            key = fileReader.nextLine();
            msg = AES.encrypt(msg, key);

            // Envio da mensagem criptografada junto de seu HMac
            msg = hMacAddress + "//" + msg;
            printer.println(msg);
            fileReader.close();

        }
        catch(IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getDeposit(Account account, PrintStream printer) {

        try {

            sc = new Scanner(System.in);
            String key, msg;
            double amount;

            System.out.println("Quanto você deseja depositar?");
            amount = Double.parseDouble(sc.nextLine());

            if(myClient.getIntruder()) {
                msg = "getDeposit//noAccount//"+myClient.getId()+"//"+amount;
            }
            else {
                msg = "getDeposit//"+account.getEmail()+"//"+myClient.getId()+"//"+amount;
            }

            // Retirada do HMac da mensagem
            fileReader = new Scanner(hMacKey);

            key = fileReader.nextLine();
            hMacAddress = HMAC.hMac(key, msg);

            // Criptografia na cifra de Vernam
            fileReader = new Scanner(vernamKey);

            key = fileReader.nextLine();
            msg = Vernam.encrypt(msg, key);

            // Criptografia na cifra AES
            fileReader = new Scanner(aesKey);

            key = fileReader.nextLine();
            msg = AES.encrypt(msg, key);

            // Envio da mensagem criptografada junto de seu HMac
            msg = hMacAddress + "//" + msg;
            printer.println(msg);
            fileReader.close();

        }
        catch(IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getTransferency(Account account, PrintStream printer) {

        try {

            sc = new Scanner(System.in);
            String key, msg;
            String transferTo;
            double amount;

            System.out.println("Para quem você deseja transferir? (Digite o email)");
            transferTo = sc.nextLine();
            System.out.println("Quanto você deseja transferir?");
            amount = Double.parseDouble(sc.nextLine());

            if(myClient.getIntruder()) {
                msg = "transferTo//noAccount//"+myClient.getId()
                +"//"+amount+"//"+transferTo;
            }
            else {
                msg = "transferTo//"+account.getEmail()+"//"+myClient.getId()
                +"//"+amount+"//"+transferTo;
            }

            fileReader = new Scanner(hMacKey);

            key = fileReader.nextLine();
            hMacAddress = HMAC.hMac(key, msg);

            // Criptografia na cifra de Vernam
            fileReader = new Scanner(vernamKey);

            key = fileReader.nextLine();
            msg = Vernam.encrypt(msg, key);

            // Criptografia na cifra AES
            fileReader = new Scanner(aesKey);

            key = fileReader.nextLine();
            msg = AES.encrypt(msg, key);

            // Envio da mensagem criptografada junto de seu HMac
            msg = hMacAddress + "//" + msg;
            printer.println(msg);
            fileReader.close();

        }
        catch(IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getInvestments(Account account, PrintStream printer) {

        try{

            String key, msg;

            // Retirada do HMac
            fileReader = new Scanner(hMacKey);

            if(myClient.getIntruder()) {
                msg = "getInvestment//noAccount//" + myClient.getId();
            }
            else {
                msg = "getInvestment//" + account.getEmail() + "//" + myClient.getId();
            }
            
            key = fileReader.nextLine();
            hMacAddress = HMAC.hMac(key, msg);

            // Criptografia na cifra de Vernam
            fileReader = new Scanner(vernamKey);

            key = fileReader.nextLine();
            msg = Vernam.encrypt(msg, key);

            // Criptografia na cifra AES
            fileReader = new Scanner(aesKey);

            key = fileReader.nextLine();
            msg = AES.encrypt(msg, key);

            // Envio da mensagem criptografada junto de seu HMac
            msg = hMacAddress + "//" + msg;
            printer.println(msg);
            fileReader.close();

        }
        catch(IOException e){
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
