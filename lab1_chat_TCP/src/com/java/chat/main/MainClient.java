package com.java.chat.main;

import com.java.chat.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainClient {
    public static void main(String[] args) throws IOException {

        System.out.println("JAVA CLIENT");

        Client client = new Client(12345);
        client.run();
    }
}
