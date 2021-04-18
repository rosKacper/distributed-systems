package com.java;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.log4j.PropertyConfigurator;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class MainExecutor {

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {

        if(args.length!=3){
            System.out.println("Niepoprawna liczba argumentów");
            System.exit(-1);
        }

        String address = args[0];
        String zNode = args[1];
        String exec = args[2];
        PropertyConfigurator.configure(".\\src\\com\\java\\properties\\log4j.properties");
        Scanner scanner = new Scanner(System.in);

        ZooKeeper zk = new ZooKeeper(address,5000,  null);
        MainWatcher mainWatcher = new MainWatcher(zk, zNode, exec);
        zk.exists(zNode, mainWatcher);


        System.out.println("Wpisz /tree aby wyświetlić drzewo potomków, wpisz /quit aby wyjść");
        String out = "";
        while (true) {
            out = scanner.nextLine();
            if(out.equals("/tree")){
                showTree(zk, zNode);
            }
            else if(out.equals("/quit")) break;
        }
    }

    private static void showChildren(ZooKeeper zk, String zNode) throws KeeperException, InterruptedException {
        List<String> children = zk.getChildren(zNode, false);
        for(String child : children){
            String childNode = zNode + "/" + child;
            System.out.println(childNode);
            if(zk.exists(childNode, false)!=null){
                showChildren(zk, childNode);
            }
        }
    }
    public static void showTree(ZooKeeper zk, String zNode) throws KeeperException, InterruptedException {
        System.out.println(zNode);
        showChildren(zk, zNode);
    }
}