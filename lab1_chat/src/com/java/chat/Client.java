package com.java.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {

    /*basic info about client*/
    int portNumber;
    private final ExecutorService executorService;

    /*TCP connection*/
    private Socket socketTCP = null;

    /*UDP connection*/
    private DatagramSocket socketUDP = null;

    /*multicast connection*/
    private MulticastSocket multicastSocket;
    private final InetAddress multicastInetAddress  = InetAddress.getByName("224.10.10.10");
    int multicastPortNumber = 9090;
    private InetSocketAddress multicastSocketAddress;
    private NetworkInterface nif;


    public Client(int portNumber) throws IOException {
        this.portNumber = portNumber;
        executorService = Executors.newFixedThreadPool(3);
    }

    public void run() throws IOException {
        /*set up TCP*/
        String hostname = "localhost";
        socketTCP = new Socket(hostname, portNumber);
        PrintWriter out = new PrintWriter(socketTCP.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socketTCP.getInputStream()));
        channelTCP(in);

        /*set up UDP*/
        socketUDP = new DatagramSocket(socketTCP.getLocalPort());
        byte[] receiveBuffer = new byte[16384];
        channelUDP(receiveBuffer);

        /*set up multicast*/
        multicastSocket = new MulticastSocket(multicastPortNumber);
        multicastSocketAddress = new InetSocketAddress(multicastInetAddress, multicastPortNumber);
        nif = NetworkInterface.getByInetAddress(multicastInetAddress);
        channelMulticast(multicastSocketAddress, receiveBuffer);

        /*send message to other clients*/
        sendOutput(out, multicastSocketAddress);
    }

    public void channelTCP(BufferedReader in){
        Runnable runnable = () -> {
            while (true) {
                String message;
                try {
                    message = in.readLine();
                    if(message!=null){
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    break;
                }
            }
            /*closing all sockets and executorService*/
            try {
                shutdown();
            } catch (IOException e) {
                e.printStackTrace();
            }

        };
        executorService.execute(runnable);
    }

    private void channelUDP(byte[] receiveBuffer) {
        /*send one UDP packet to let server know about it's existence*/
        Runnable runnable = () -> {
            String message;
            while (true) {
                try {
                    Arrays.fill(receiveBuffer, (byte) 0);
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    socketUDP.receive(receivePacket);
                    message = new String(receivePacket.getData());
                    System.out.println("(UDP)" + message);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
            try {
                shutdown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        executorService.execute(runnable);
    }

    private void channelMulticast(InetSocketAddress multicastSocketAddress, byte[] receiveBuffer) throws IOException {
        multicastSocket.joinGroup(multicastSocketAddress, nif);
        Runnable runnable = () -> {
            String message;
            while (true) {
                try {
                    Arrays.fill(receiveBuffer, (byte) 0);
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    multicastSocket.receive(receivePacket);
                    message = new String(receivePacket.getData());
                    System.out.println("(mutlicast)" + message);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
            try {
                shutdown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        executorService.execute(runnable);
    }

    private void sendOutput(PrintWriter out, InetSocketAddress multicastSocketAddress) throws IOException {
        String message;
        Scanner scanner = new Scanner(System.in);
        while(true){
            message = scanner.nextLine();
            /*send ASCII art via UDP*/
            if(message.startsWith("/U")){
                InetAddress addressUDP = InetAddress.getByName("localhost");
                byte[] sendBuffer = setAsciiArt().getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, addressUDP, portNumber);
                socketUDP.send(sendPacket);
            }
            /*multicast*/
            else if(message.startsWith("/M")){
                message = message.substring(3);
                byte[] sendBuffer = message.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, multicastSocketAddress);
                multicastSocket.send(sendPacket);
            }
            /*quit from chat*/
            else if(message.equals("q")){
                shutdown();
            }
            /*TCP*/
            else
                out.println(message);
        }

    }

    private String setAsciiArt(){
        return  "\n" +
                ".--.            .--.\n" +
                " ( (`\\\\.\"--``--\".//`) )\n" +
                "  '-.   __   __    .-'\n" +
                "   /   /__\\ /__\\   \\\n" +
                "  |    \\ 0/ \\ 0/    |\n" +
                "  \\     `/   \\`     /\n" +
                "   `-.  /-\"\"\"-\\  .-`\n" +
                "     /  '.___.'  \\\n" +
                "     \\     I     /\n" +
                "      `;--'`'--;`\n" +
                "        '.___.'";
    }

    public void shutdown() throws IOException {
        socketTCP.close();
        socketUDP.close();
        multicastSocket.leaveGroup(multicastSocketAddress, nif);
        multicastSocket.close();
        executorService.shutdown();
        System.exit(1);
    }
}
