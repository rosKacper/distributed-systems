package com.java;


import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

import static org.apache.zookeeper.Watcher.Event.EventType.NodeCreated;
import static org.apache.zookeeper.Watcher.Event.EventType.NodeDeleted;


public class MainWatcher implements Watcher {

    ZooKeeper zk;
    String zNode;
    String exec;
    ChildWatcher childWatcher;
    Process application;

    public MainWatcher(ZooKeeper zk, String zNode, String exec){
        this.zk = zk;
        this.zNode = zNode;
        this.exec = exec;
        this.childWatcher = new ChildWatcher(zk, zNode);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        try {
            if(watchedEvent.getType().equals(NodeCreated)){
                zk.getChildren(zNode, childWatcher);
                application = Runtime.getRuntime().exec(exec);
                System.out.println("Node /z created: starting app");
            }
            else if(watchedEvent.getType().equals(NodeDeleted)){
                if(application!=null){
                    application.destroy();
                    if(application.isAlive()) application.destroyForcibly();
                }
                System.out.println("Node /z deleted: shutting down app");
            }
            zk.exists(zNode, this);
        } catch (IOException | InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

}
