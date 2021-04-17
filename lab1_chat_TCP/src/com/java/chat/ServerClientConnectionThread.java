package com.java.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ServerClientConnectionThread implements Runnable {

    private List<ClientDetails> chat;
    private Socket socket;
    private ClientDetails clientDetails;

    public ServerClientConnectionThread(Socket socket, List<ClientDetails> chat, ClientDetails clientDetails){
        this.socket = socket;
        this.chat = chat;
        this.clientDetails = clientDetails;
    }


    @Override
    public void run() {
        try {
            /*
             * if the client is new and he isn't present in server's collection of ClientDetails
             * it will set all necessary data from current Client in ClientDetails
             */
            if(!chat.contains(clientDetails)){
                setFullClientDetails();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String message = null;
        while(true){
            try {
                message = clientDetails.getClientInput().readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(message!=null){
                for(ClientDetails client : chat){
                    if(!client.equals(clientDetails)){
                        client.getClientOutput().println(clientDetails.getUsername() + ": " + message);
                    }

                }
            }

        }
    }

    public void setFullClientDetails() throws IOException {
        clientDetails.setClientOutput(new PrintWriter(socket.getOutputStream(), true));
        clientDetails.setClientInput(new BufferedReader(new InputStreamReader(socket.getInputStream())));

        clientDetails.getClientOutput().println("Please enter your username to join chat: ");
        clientDetails.setUsername(clientDetails.getClientInput().readLine());

        clientDetails.getClientOutput().println("Hello " + clientDetails.getUsername() + "!");
        for(ClientDetails client : chat){
            if(!client.equals(clientDetails)){
                client.getClientOutput().println("**" + clientDetails.getUsername() + " has joined the chat**");
            }
        }
        clientDetails.setAddressUDP();

        chat.add(clientDetails);
    }


}
