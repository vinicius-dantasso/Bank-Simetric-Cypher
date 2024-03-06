package handlers;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import criptos.AES;
import criptos.Vernam;
import hmac.HMAC;
import service.ServerService;

public class ServerHandler implements Runnable{

    private ClientHandler clientHandler;
    private ServerService service;

    private Socket client;
    private Socket sender;

    private boolean connection = true;

    private Scanner sc = null;
    private Scanner keyReader = null;

    private File vernamKey = new File("src\\keys\\vernamKey.txt");
    private File aesKey = new File("src\\keys\\aesKey.txt");

    // Separa todos os atributos da mensagem
    private String[] splitMsg;
    private String clientHMac, serverHMac, finalMessage;
    private String email, requestType;
    private int clientId;
    private String key,hMacKey;

    public ServerHandler(Socket c, ClientHandler ch) {
        this.client = c;
        this.clientHandler = ch;
        this.service = new ServerService();
    }

    @Override
    public void run() {

        String received;
        
        try {

            // Responsável por receber a mensagem do cliente, caso seja do PrintStream
            sc = new Scanner(client.getInputStream());
            
            // Responsável por ler a chave HMac do servidor
            keyReader = new Scanner(new File("src\\keys\\hMacKey.txt"));
            hMacKey = keyReader.nextLine();

            while(connection) {

                received = sc.nextLine();

                // Verifica se recebeu uma mensagem com "verified"
                if(received.contains("verified")) {
                    // Caso verdadeiro, o cliente que pediu login
                    // ou criou uma conta agora está liberado no sistema
                    service.signInClient(received, clientHandler);
                }
                else if(received.equals("Conta já existente!")) {
                    // Caso tenha recebido uma mensagem de conta existente
                    // ao tentar se cadastrar com email já existente
                    System.out.println(received);
                }
                else if(received.contains("notVerified")) {
                    // Caso o cliente ainda não esteja logado
                    // ou cadastrado
                    service.signClient(received, clientHandler);
                }
                else { // Quando o cliente já estiver logado

                    if(clientHandler.getId() == 0) {
                        // Separa os atributos da mensagem
                        splitMsg = received.split("//");
                        clientHMac = splitMsg[0];
                        finalMessage = splitMsg[1];

                        finalMessage = decode(finalMessage);

                        splitMsg = finalMessage.split("//");
                        messageType(splitMsg);

                    }
                    else {
                        
                        splitMsg = received.split("//");
                        serverHMac = splitMsg[0];
                        finalMessage = splitMsg[1];

                        finalMessage = decode(finalMessage);

                        clientHMac = HMAC.hMac(hMacKey, finalMessage);

                        if(clientHMac.equals(serverHMac)) {
                            System.out.println(finalMessage);
                        }

                    }
                    
                }
                
            }

            sc.close();
            keyReader.close();
            client.close();
            sender.close();

        }
        catch(IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            //e.printStackTrace();
        }

    }

    private String decode(String finalMessage) throws Exception {
        keyReader = new Scanner(aesKey);
        key = keyReader.nextLine();

        finalMessage = AES.decrypt(finalMessage, key);

        keyReader = new Scanner(vernamKey);
        key = keyReader.nextLine();

        finalMessage = Vernam.decrypt(finalMessage, key);
        return finalMessage;
    }

    private void messageType(String[] splitMsg) throws Exception {
        requestType = splitMsg[0];
        email = splitMsg[1];
        clientId = Integer.parseInt(splitMsg[2]);

        switch(splitMsg.length) {

            case 4 -> {
                double value = Double.parseDouble(splitMsg[3]);
                finalMessage = requestType+"//"+email+"//"+clientId+"//"+value;    
                serverHMac = HMAC.hMac(hMacKey, finalMessage);

                if(serverHMac.equals(clientHMac)) {
                    String necessaryInfo = email+"//"+clientId+"//"+value;
                    service.handleClientRequest(necessaryInfo, requestType);
                }
                else {
                    service.handleClientRequest(Integer.toString(clientId), "intruder");
                }
            }

            case 5 -> {
                double value = Double.parseDouble(splitMsg[3]);
                String transferTo = splitMsg[4];

                finalMessage = requestType+"//"+email+"//"+clientId
                +"//"+value+"//"+transferTo;
                                    
                serverHMac = HMAC.hMac(hMacKey, finalMessage);

                if(serverHMac.equals(clientHMac)) {
                    String necessaryInfo = email+"//"+clientId
                    +"//"+value+"//"+transferTo;
                    service.handleClientRequest(necessaryInfo, requestType);
                }
                else {
                    service.handleClientRequest(Integer.toString(clientId), "intruder");
                }
            }

            default -> {
                finalMessage = requestType+"//"+email+"//"+clientId;    
                serverHMac = HMAC.hMac(hMacKey, finalMessage);

                 if(serverHMac.equals(clientHMac)) {
                    String necessaryInfo = email+"//"+clientId;
                    service.handleClientRequest(necessaryInfo, requestType);
                }
                else {
                    service.handleClientRequest(Integer.toString(clientId), "intruder");
                }
            }

        }
    } 
    
}
