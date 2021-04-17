package com.java;


import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

public class ChildTreeExecutor {

    ZooKeeper zk;

    public ChildTreeExecutor(ZooKeeper zk){
        this.zk = zk;
    }


    private void showChildren(String zNode) throws KeeperException, InterruptedException {
        List<String> children = zk.getChildren(zNode, false);
        for(String child : children){
            String childNode = zNode + "/" + child;
            System.out.println(childNode);
            if(zk.exists(childNode, false)!=null){
                showChildren(childNode);
            }
        }
    }
    public void showTree(String zNode) throws KeeperException, InterruptedException {
        System.out.println(zNode);
        showChildren(zNode);
    }


}
