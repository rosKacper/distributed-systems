package com.java.main;

import com.java.Supplier;
import com.java.Team;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainSupplier {
    public static void main(String[] argv) throws Exception {

        System.out.println("Podaj nazwe dostawcy: ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String username = br.readLine();
        ArrayList<String> products = new ArrayList<>();
        System.out.println("Podaj sprzęt produkowany przez dostawcę. Aby zakończyć dodawanie należy napisać 'quit'.");
        String product = "";
        while(true){
            product = br.readLine();
            if(product.equals("quit") && !products.isEmpty()){
                break;
            }
            products.add(product);
        }
        Supplier supplier = new Supplier(username,products);
        supplier.launch();

    }
}
