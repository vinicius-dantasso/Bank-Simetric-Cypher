package handlers;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

import client.Account;
import service.ClientService;

public class ClientHandler implements Runnable{

    private Account account;
    private ClientService clientService;

    private Socket client;

    private boolean connection = true;
    private boolean isLoged = false;
    private boolean intruder = false;

    private PrintStream printer;

    private int id;

    public ClientHandler(Socket c, int id) {
        this.client = c;
        this.id = id;
        clientService = new ClientService(this);
    }

    public ClientHandler(Socket c, int id, boolean in) {
        this.client = c;
        this.id = id;
        this.intruder = in;
        this.isLoged = true;
        clientService = new ClientService(this);
    }

    @Override
    public void run() {
        
        try {

            System.out.println("O cliente conectou ao servidor");

            // Responsável por ler a mensagem do cliente enviar para o servidor
            Scanner sc =  new Scanner(System.in);
            printer = new PrintStream(client.getOutputStream());
            String msg;

            String option, email, password;

            // Pede para se cadastrar ou
            // logar no sistema
            do {

                if(id != 0 && !intruder) {
                    System.out.println("[1] Para se cadastrar;");
                    System.out.println("[2] Para entrar.");
                    option = sc.nextLine();

                    if(option.equals("1")) {
                        System.out.println("Digite seu email:");
                        email = sc.nextLine();
                        System.out.println("Digite sua senha:");
                        password = sc.nextLine();

                        account = new Account(email, password);

                        msg = account.toString()+"//"+id+"//1"+"//notVerified";
                        printer.println(msg);
                    }
                    else if(option.equals("2")) {
                        System.out.println("Digite seu email:");
                        email = sc.nextLine();
                        System.out.println("Digite sua senha:");
                        password = sc.nextLine();

                        account = new Account(email, password);

                        msg = account.toString()+"//"+id+"//2"+"//notVerified";
                        printer.println(msg);
                    }

                    Thread.sleep(1000);
                }

            } while(!isLoged);

            // Caso tenha logado com sucesso
            // agora o cliente terá acesso ao sistema
            while(connection && isLoged) {

                // Espera uma mensagem do cliente
                clientService.clientOption(account, sc, printer);
                Thread.sleep(1000);

            }

            printer.close();
            sc.close();
            //fileReader.close();
            client.close();
            System.out.println("Conexão finalizada.");

        }
        catch(IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean getIsLoged() {
        return this.isLoged;
    }

    public void setIsLoged(boolean log) {
        this.isLoged = log;
    }

    public boolean getIntruder() {
        return this.intruder;
    }

    public void setIntruder(boolean in) {
        this.intruder = in;
    }

    public Account getAccount() {
        return this.account;
    }

    public int getId() {
        return this.id;
    }

}
