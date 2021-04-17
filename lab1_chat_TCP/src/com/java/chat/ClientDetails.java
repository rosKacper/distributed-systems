package com.java.chat;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientDetails {

    /*Has to be initialized by setters, it this implementation it happens in
    * ServerClientConnectionThread*/
    private String username = null;
    private PrintWriter clientOutput = null;
    private BufferedReader clientInput = null;

    private int portNumber;
    private InetAddress addressUDP;


    public ClientDetails(int portNumber){
        this.portNumber = portNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public PrintWriter getClientOutput() {
        return clientOutput;
    }

    public void setClientOutput(PrintWriter clientOutput) {
        this.clientOutput = clientOutput;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public InetAddress getAddressUDP() {
        return addressUDP;
    }

    public void setAddressUDP() throws UnknownHostException {
        this.addressUDP = InetAddress.getByName("localhost");;
    }

    public BufferedReader getClientInput() {
        return clientInput;
    }

    public void setClientInput(BufferedReader clientInput) {
        this.clientInput = clientInput;
    }
}
