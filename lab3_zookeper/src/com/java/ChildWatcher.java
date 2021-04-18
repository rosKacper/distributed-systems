package com.java;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

import static org.apache.zookeeper.Watcher.Event.EventType.NodeChildrenChanged;

public class ChildWatcher implements Watcher {

    ZooKeeper zk;
    String zNode;

    public ChildWatcher(ZooKeeper zk, String zNode){
        this.zk = zk;
        this.zNode = zNode;
    }

    private void assignWatcher(String zNode) throws KeeperException, InterruptedException {
        if (zk.exists(zNode, false)!=null) {
            List<String> children = zk.getChildren(zNode, this);
            for (String child : children) {
                assignWatcher(zNode + "/" + child);
            }
        }
    }
    @Override
    public void process(WatchedEvent watchedEvent) {
        try {
            assignWatcher(zNode);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        if (watchedEvent.getType().equals(NodeChildrenChanged)) {
            try {
                System.out.println("Number of children nodes: " + zk.getAllChildrenNumber(zNode));
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
