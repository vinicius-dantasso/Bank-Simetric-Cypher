package service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import criptos.AES;
import criptos.Vernam;
import handlers.ClientHandler;
import hmac.HMAC;

public class ServerService {

    private Socket sender;
    private PrintStream printer;

    private DecimalFormat df = new DecimalFormat("#.##");

    private Scanner fileReader = null;
    private Scanner myScanner = null;
    private FileWriter writer = null;

    private File database = new File("src\\database\\accounts.txt");
    private File hMacFile = new File("src\\keys\\hMacKey.txt");
    private File vernamKey = new File("src\\keys\\vernamKey.txt");
    private File aesKey = new File("src\\keys\\aesKey.txt");

    private List<String> datas = new ArrayList<>();

    private String[] splitMsg;
    private String email, password;
    private String hMacAddress, key;

    private int clientId, sign;
    private double balance = 0.0;
    
    public void signClient(String msg, ClientHandler ch) throws IOException {

        splitMsg = msg.split("//");
        email = splitMsg[0];
        password = splitMsg[1];
        balance = Double.parseDouble(splitMsg[2]);
        clientId = Integer.parseInt(splitMsg[3]);
        sign = Integer.parseInt(splitMsg[4]);
        
        fileReader = new Scanner(database);
        String acc;
        boolean notCreate = true;

        switch(sign) {

            // Caso seja uma mensagem para cadastro
            case 1 -> {
                
                // Procura no arquivo de contas salvas
                while(fileReader.hasNextLine()) {
                    acc = fileReader.nextLine();
                    // Se houver um email igual ao email enviado
                    // será retornado uma mensagem de conta existente
                    if(acc.contains("@") && acc.equals(email)) {
                        sender = new Socket("localhost",5000+clientId);
                        printer = new PrintStream(sender.getOutputStream());
                        printer.println("Conta já existente!");
                        notCreate = false;
                    }
                }

                // Caso não haja nenhuma conta com o email enviado
                // ela será salva e o acesso será liberado para o cliente
                if(notCreate) {
                    writer = new FileWriter(database, true);
                    writer.write(clientId + "\n" + email + "\n" + password + "\n" + balance + "\n\n");
                    writer.close();
                    sender = new Socket("localhost", 5000+clientId);
                    printer = new PrintStream(sender.getOutputStream());
                    printer.println(email + "//" + password + "//" + balance + "//" + "verified");
                }

            }

            // Caso seja uma mensagem para login
            case 2 -> {

                // Procura no arquivo de contas salvas
                while(fileReader.hasNextLine()) {
                    acc = fileReader.nextLine();
                    // Vai procurar por um email igual na lista dos salvos
                    // caso encontre irá comparar as senhas e então
                    // permitir acesso ao cliente
                    if(acc.contains("@") && acc.equals(email)) {
                        acc = fileReader.nextLine();
                        if(acc.equals(password)) {
                            acc = fileReader.nextLine();
                            balance = Double.parseDouble(acc);
                            sender = new Socket("localhost",5000+clientId);
                            printer = new PrintStream(sender.getOutputStream());
                            printer.println(email + "//" + password + "//" + balance + "//" + "verified");
                        }
                    }
                }

            }

            default -> {}

        }

    }

    public void signInClient(String received, ClientHandler clientHandler) {

        splitMsg = received.split("//");
        email = splitMsg[0];
        password = splitMsg[1];
        balance = Double.parseDouble(splitMsg[2]);

        clientHandler.getAccount().setEmail(email);
        clientHandler.getAccount().setPassword(password);
        clientHandler.getAccount().setBalance(balance);
        clientHandler.setIsLoged(true);

    }

    public void handleClientRequest(String received, String requestType) {

        switch(requestType) {

            case "getBalance"    -> { getBalance(received); }
            case "getWithdraw"   -> { getWithdraw(received); }
            case "getDeposit"    -> { getDeposit(received); }
            case "transferTo"    -> { getTransferency(received); }
            case "getInvestment" -> { getInvestment(received); }
            default              -> { intruderDetected(received); }

        }

    }

    private void getBalance(String infos) {

        try {

            splitMsg = infos.split("//");
            email = splitMsg[0];
            clientId = Integer.parseInt(splitMsg[1]);

            System.out.println("Pedido de verificação de saldo do cliente -> " + email);

            fileReader = new Scanner(database);
            String acc;

            while(fileReader.hasNextLine()) {
                
                acc = fileReader.nextLine();
                if(acc.contains("@") && acc.equals(email)) {
                    acc = fileReader.nextLine();
                    acc = fileReader.nextLine();
                    balance = Double.parseDouble(acc);
                    
                    sender = new Socket("localhost", 5000+clientId);
                    printer = new PrintStream(sender.getOutputStream());

                    String msg = "Seu saldo é: R$" + df.format(balance);

                    myScanner = new Scanner(hMacFile);
                    String hMacKey = myScanner.nextLine();
                    hMacAddress = HMAC.hMac(hMacKey, msg);

                    myScanner = new Scanner(vernamKey);
                    key = myScanner.nextLine();
                    msg = Vernam.encrypt(msg, key);

                    myScanner = new Scanner(aesKey);
                    key = myScanner.nextLine();
                    msg = AES.encrypt(msg, key);
                    
                    msg = hMacAddress + "//" + msg;
                    printer.println(msg);
                    sender.close();
                    printer.close();
                }

            }

            fileReader.close();
            myScanner.close();

        }
        catch(IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getWithdraw(String infos) {

        try {

            splitMsg = infos.split("//");
            email = splitMsg[0];
            clientId = Integer.parseInt(splitMsg[1]);
            double amount = Double.parseDouble(splitMsg[2]);

            System.out.println("Pedido de saque do cliente -> " + email);

            fileReader = new Scanner(database);
            String acc;

            datas.clear();
            while(fileReader.hasNextLine()) {
                acc = fileReader.nextLine();
                datas.add(acc);
            }

            fileReader.close();

            writer = new FileWriter(database);

            for(int i=0;i<datas.size();i++) {
                String data = datas.get(i);

                if(data.contains("@") && data.equals(email)) {
                    writer.append(data + "\n");
                    i++;
                    password = datas.get(i);
                    writer.append(password + "\n");
                    i++;
                    balance = Double.parseDouble(datas.get(i));

                    if(balance >= amount) {
                        balance -= amount;
                        writer.append(Double.toString(balance) + "\n");

                        sender = new Socket("localhost", 5000+clientId);
                        printer = new PrintStream(sender.getOutputStream());

                        String msg = "Saque de R$" + df.format(amount) + " realizado com sucesso.";

                        myScanner = new Scanner(hMacFile);
                        String hMacKey = myScanner.nextLine();
                        hMacAddress = HMAC.hMac(hMacKey, msg);

                        myScanner = new Scanner(vernamKey);
                        key = myScanner.nextLine();
                        msg = Vernam.encrypt(msg, key);

                        myScanner = new Scanner(aesKey);
                        key = myScanner.nextLine();
                        msg = AES.encrypt(msg, key);
                        
                        msg = hMacAddress + "//" + msg;
                        printer.println(msg);
                    }
                    else {
                        writer.append(Double.toString(balance) + "\n");
                        sender = new Socket("localhost", 5000+clientId);
                        printer = new PrintStream(sender.getOutputStream());

                        String msg = "Saldo insuficiente.";

                        myScanner = new Scanner(hMacFile);
                        String hMacKey = myScanner.nextLine();
                        hMacAddress = HMAC.hMac(hMacKey, msg);

                        myScanner = new Scanner(vernamKey);
                        key = myScanner.nextLine();
                        msg = Vernam.encrypt(msg, key);

                        myScanner = new Scanner(aesKey);
                        key = myScanner.nextLine();
                        msg = AES.encrypt(msg, key);
                        
                        msg = hMacAddress + "//" + msg;
                        printer.println(msg);
                    }

                }
                else {
                    writer.append(data + "\n");
                }

            }

            sender.close();
            printer.close();
            myScanner.close();
            writer.close();

        }
        catch(IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void getDeposit(String infos) {

        try {

            splitMsg = infos.split("//");
            email = splitMsg[0];
            clientId = Integer.parseInt(splitMsg[1]);
            double amount = Double.parseDouble(splitMsg[2]);

            System.out.println("Pedido de depósito do cliente -> " + email);

            fileReader = new Scanner(database);
            String acc;

            datas.clear();
            while(fileReader.hasNextLine()) {
                acc = fileReader.nextLine();
                datas.add(acc);
            }
            fileReader.close();

            writer = new FileWriter(database);

            for(int i=0;i<datas.size();i++) {
                String data = datas.get(i);

                if(data.contains("@") && data.equals(email)) {
                    writer.append(data + "\n");
                    i++;

                    password = datas.get(i);
                    writer.append(password + "\n");
                    i++;

                    balance = Double.parseDouble(datas.get(i));
                    balance += amount;
                    writer.append(Double.toString(balance) + "\n");
                    
                    sender = new Socket("localhost", 5000+clientId);
                    printer = new PrintStream(sender.getOutputStream());

                    String msg = "Depósito de R$" + df.format(amount) + " realizado com sucesso.";

                    myScanner = new Scanner(hMacFile);
                    String hMacKey = myScanner.nextLine();
                    hMacAddress = HMAC.hMac(hMacKey, msg);

                    myScanner = new Scanner(vernamKey);
                    key = myScanner.nextLine();
                    msg = Vernam.encrypt(msg, key);

                    myScanner = new Scanner(aesKey);
                    key = myScanner.nextLine();
                    msg = AES.encrypt(msg, key);
                    
                    msg = hMacAddress + "//" + msg;
                    printer.println(msg);

                }
                else {
                    writer.append(data + "\n");
                }

            }

            writer.close();
            myScanner.close();
            sender.close();
            printer.close();

        }
        catch(IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void getTransferency(String infos) {

        try {

            splitMsg = infos.split("//");
            email = splitMsg[0];
            clientId = Integer.parseInt(splitMsg[1]);
            double amount = Double.parseDouble(splitMsg[2]);
            String emailToTransfer = splitMsg[3];

            System.out.println("Pedido de transferência do cliente -> " + email + ", para o cliente -> " + emailToTransfer);

            fileReader = new Scanner(database);
            String acc;

            datas.clear();
            while(fileReader.hasNextLine()) {
                acc = fileReader.nextLine();
                datas.add(acc);
            }

            fileReader.close();

            myScanner = new Scanner(hMacFile);
            String hMacKey = myScanner.nextLine();

            boolean canTransfer = false;
            for(int i=0;i<datas.size();i++) {

                String data = datas.get(i);

                if(data.contains("@") && data.equals(email)) {
                    i++;
                    password = datas.get(i);
                    i++;
                    balance = Double.parseDouble(datas.get(i));
                    
                    if(balance >= amount) {
                        balance -= amount;
                        canTransfer = true;
                        datas.set(i, Double.toString(balance));
                    }
                    else {
                        sender = new Socket("localhost", 5000+clientId);
                        printer = new PrintStream(sender.getOutputStream());

                        String msg = "Saldo insuficiente para realizar transferencia.";

                        hMacAddress = HMAC.hMac(hMacKey, msg);

                        myScanner = new Scanner(vernamKey);
                        key = myScanner.nextLine();
                        msg = Vernam.encrypt(msg, key);

                        myScanner = new Scanner(aesKey);
                        key = myScanner.nextLine();
                        msg = AES.encrypt(msg, key);
                        
                        msg = hMacAddress + "//" + msg;
                        printer.println(msg);
                    }

                    if(canTransfer) {

                        for(int j=0;j<datas.size();j++) {

                            String data2 = datas.get(j);
                            int idToTransfer;
    
                            if(data2.contains("@") && data2.equals(emailToTransfer)) {
                                idToTransfer = Integer.parseInt(datas.get(j-1));
                                j++;
                                password = datas.get(j);
                                j++;
                                balance = Double.parseDouble(datas.get(j));
    
                                balance += amount;
                                datas.set(j, Double.toString(balance));

                                sender = new Socket("localhost", 5000+clientId);
                                printer = new PrintStream(sender.getOutputStream());

                                String msg = "Transferencia bem sucedida.";
                               
                                hMacAddress = HMAC.hMac(hMacKey, msg);

                                myScanner = new Scanner(vernamKey);
                                key = myScanner.nextLine();
                                msg = Vernam.encrypt(msg, key);

                                myScanner = new Scanner(aesKey);
                                key = myScanner.nextLine();
                                msg = AES.encrypt(msg, key);
                                
                                msg = hMacAddress + "//" + msg;
                                printer.println(msg);

                                sender = new Socket("localhost", 5000+idToTransfer);
                                printer = new PrintStream(sender.getOutputStream());

                                msg = "Transferencia recebida de " + email;

                                hMacAddress = HMAC.hMac(hMacKey, msg);

                                myScanner = new Scanner(vernamKey);
                                key = myScanner.nextLine();
                                msg = Vernam.encrypt(msg, key);

                                myScanner = new Scanner(aesKey);
                                key = myScanner.nextLine();
                                msg = AES.encrypt(msg, key);
                                
                                msg = hMacAddress + "//" + msg;
                                printer.println(msg);
    
                            }
    
                        }

                    }

                }

            }

            writer = new FileWriter(database);

            for(int m=0;m<datas.size();m++) {
                writer.append(datas.get(m) + "\n");
            }

            writer.close();
            myScanner.close();
            sender.close();
            printer.close();

        }
        catch(IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void getInvestment(String infos) {

        try {

            splitMsg = infos.split("//");
            email = splitMsg[0];
            clientId = Integer.parseInt(splitMsg[1]);

            System.out.println("Pedido para simulação de investimentos do cliente -> " + email);

            fileReader = new Scanner(database);
            String acc;

            while(fileReader.hasNextLine()) {
                
                acc = fileReader.nextLine();
                if(acc.contains("@") && acc.equals(email)) {
                    acc = fileReader.nextLine();
                    acc = fileReader.nextLine();
                    balance = Double.parseDouble(acc);
                }

            }

            double[] poupanca = new double[3];
            double[] rendaFixa = new double[3];
            double amount;

            poupanca[0] = balance * Math.pow(1+0.005, 3);
            amount = poupanca[0];
            poupanca[1] = amount * Math.pow(1+0.005, 6);
            amount = poupanca[1];
            poupanca[2] = amount * Math.pow(1+0.005, 12);

            rendaFixa[0] = balance * Math.pow(1+0.015, 3);
            amount = rendaFixa[0];
            rendaFixa[1] = amount * Math.pow(1+0.015, 6);
            amount = rendaFixa[1];
            rendaFixa[2] = amount * Math.pow(1+0.015, 12);

            sender = new Socket("localhost", 5000+clientId);
            printer = new PrintStream(sender.getOutputStream());

            String msg = "Poupança:\n" +
            "Rendimento em 3 meses: " + df.format(poupanca[0]) + "\n" +
            "Rendimento em 6 meses: " + df.format(poupanca[1]) + "\n" +
            "Rendimento em 12 meses: " + df.format(poupanca[2]) + "\n\n" +
            "Renda Fixa:\n" +
            "Rendimento em 3 meses: " + df.format(rendaFixa[0]) + "\n" +
            "Rendimento em 6 meses: " + df.format(rendaFixa[1]) + "\n" +
            "Rendimento em 12 meses: " + df.format(rendaFixa[2]);

            myScanner = new Scanner(hMacFile);
            String hMacKey = myScanner.nextLine();
            hMacAddress = HMAC.hMac(hMacKey, msg);

            myScanner = new Scanner(vernamKey);
            key = myScanner.nextLine();
            msg = Vernam.encrypt(msg, key);

            myScanner = new Scanner(aesKey);
            key = myScanner.nextLine();
            msg = AES.encrypt(msg, key);
                    
            msg = hMacAddress + "//" + msg;
            printer.println(msg);
            sender.close();
            printer.close();

            fileReader.close();
            myScanner.close();

        }
        catch(IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void intruderDetected(String infos) {

        clientId = Integer.parseInt(infos);
        String msg;

        try {

            sender = new Socket("localhost", 5000+clientId);
            printer = new PrintStream(sender.getOutputStream());

            msg = "Mensagem invalida.";

            myScanner = new Scanner(hMacFile);
            String hMacKey = myScanner.nextLine();
            hMacAddress = HMAC.hMac(hMacKey, msg);

            myScanner = new Scanner(vernamKey);
            key = myScanner.nextLine();
            msg = Vernam.encrypt(msg, key);

            myScanner = new Scanner(aesKey);
            key = myScanner.nextLine();
            msg = AES.encrypt(msg, key);
                    
            msg = hMacAddress + "//" + msg;
            printer.println(msg);

            printer.close();
            sender.close();

        }
        catch(IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
