package com.java;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.log4j.PropertyConfigurator;
import java.io.IOException;
import java.util.Scanner;

public class MainExecutor {

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {

        String address = "127.0.0.1:2181";
        String zNode = "/z";
        String exec = "notepad";
        PropertyConfigurator.configure(".\\src\\com\\java\\properties\\log4j.properties");
        Scanner scanner = new Scanner(System.in);

        ZooKeeper zk = new ZooKeeper(address,5000,  null);
        ChildTreeExecutor childTreeExecutor = new ChildTreeExecutor(zk);
        MainWatcher mainWatcher = new MainWatcher(zk, zNode, exec);
        zk.exists(zNode, mainWatcher);


        System.out.println("Wpisz /tree aby wyświetlić drzewo potomków, wpisz /quit aby wyjść");
        String out = "";
        while (true) {
            out = scanner.nextLine();
            if(out.equals("/tree")){
                childTreeExecutor.showTree(zNode);
            }
            else if(out.equals("/quit")) break;
        }
    }
}