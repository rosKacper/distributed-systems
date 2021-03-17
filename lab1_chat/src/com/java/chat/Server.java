package com.java.chat;


import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class Server {

    /*basic info about server*/
    int portNumber;
    ExecutorService executorService;
    private List<ClientDetails> chat;

    /*server sockets*/
    private ServerSocket serverSocketTCP = null;
    private DatagramSocket serverSocketUDP = null;


    public Server(int portNumber, int maxClientNumber){
        this.portNumber = portNumber;

        executorService = Executors.newFixedThreadPool(maxClientNumber + 3);
        /*
        * When we are modifying th List, the whole content of the CopyOnWriteArrayList is copied
        * Thanks to this we can iterate over the list in a safe way, even when concurrent modification
        * is happening.
        * */
        this.chat = new CopyOnWriteArrayList<>();
    }

    public void launch() throws IOException {
        channelTCP();
        channelUDP();
    }

    public void channelTCP() throws IOException {
        serverSocketTCP = new ServerSocket(portNumber);
        /*we create thread to listen new TCP connections*/
        Runnable runnable = () -> {
            Socket socket = null;
            try {
                while(true) {
                    socket = serverSocketTCP.accept();
                    System.out.println("Connection set");
                    /*we create new threads for individual connection between the client and the server*/
                    assert socket != null;
                    executorService.execute(new ServerClientConnectionThread(socket,chat, new ClientDetails(socket.getPort())));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                serverSocketTCP.close();
                serverSocketUDP.close();
                executorService.shutdown();
                System.exit(-1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        executorService.execute(runnable);
        }

        public void channelUDP() throws IOException {
            serverSocketUDP = new DatagramSocket(portNumber);
            byte[] receiveBuffer = new byte[16384];
            Runnable runnable = () -> {
                try{
                    while(true){
                        Arrays.fill(receiveBuffer, (byte)0);
                        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        serverSocketUDP.receive(receivePacket);
                        String message = new String(receivePacket.getData());
                        String username = "";
                        int currentPortNumber = receivePacket.getPort();

                        /*get username from clientDetails*/
                        for(ClientDetails client : chat){
                            if(client.getPortNumber() == currentPortNumber) {
                                username = client.getUsername();
                                break;
                            }
                        }
                        message = username + ": " + message;
                        byte[] sendBuffer = message.getBytes();

                        for(ClientDetails client: chat){
                            if(client.getPortNumber() != currentPortNumber){
                                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, client.getAddressUDP(), client.getPortNumber());
                                serverSocketUDP.send(sendPacket);
                                System.out.println("UDP datagram packet sent to: " + client.getPortNumber() + " " + client.getAddressUDP());
                            }
                        }
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }
                try {
                    serverSocketTCP.close();
                    serverSocketUDP.close();
                    executorService.shutdown();
                    System.exit(-1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            executorService.execute(runnable);
        }


}


