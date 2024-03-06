package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import handlers.ClientHandler;
import handlers.ServerHandler;

public class ClientServer {
    
    private ServerSocket server;
    private Socket client;
    private Socket connectedClient;
    private String ip;
    private int port;
    private int nextPort;
    private int id;

    public ClientServer(String ip, int id, int p, int p2) {
        this.ip = ip;
        this.port = p;
        this.nextPort = p2;
        this.id = id;
        this.rodar();
    }

    private void rodar() {

        try {

            server = new ServerSocket(port);

            client = new Socket(ip, nextPort);
            ClientHandler ch = new ClientHandler(client, id);
            Thread t1 = new Thread(ch);
            t1.start();

            while(true) {

                connectedClient = server.accept();
                ServerHandler sh = new ServerHandler(connectedClient, ch);
                Thread t2 = new Thread(sh);
                t2.start();

            }

        }
        catch(IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        new ClientServer("localhost", 0, 5000, 5000);
        //new ClientServer("localhost", 1, 5001, 5000);
        //new ClientServer("localhost", 2, 5002, 5000);
        //new ClientServer("localhost", 3, 5003, 5000);
        //new ClientServer("localhost", 4, 5004, 5000);
    }

}
