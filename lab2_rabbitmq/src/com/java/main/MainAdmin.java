package com.java.main;

import com.java.Admin;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class MainAdmin {

    public static void main(String[] args) throws IOException, TimeoutException {

        Admin admin = new Admin();
        admin.launch();
    }
}
