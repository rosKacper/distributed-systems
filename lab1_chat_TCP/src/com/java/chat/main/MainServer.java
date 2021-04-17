package com.java.chat.main;

import com.java.chat.Server;

import java.io.IOException;

public class MainServer {
    public static void main(String[] args) throws IOException {

        System.out.println("JAVA SERVER");
        Server server = new Server(12345, 7);
        server.launch();
    }
}
