package com.java;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class Supplier {

    private String username;
    private ArrayList<String> products;
    private int orderID = 0;
    private ArrayList<String> orders;

    public Supplier(String username, ArrayList<String> products){
        this.username = username;
        this.products = products;
        this.orders = new ArrayList<>();
    }


    public void launch() throws IOException, TimeoutException {

        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();


        // exchanges
        String EXCHANGE_TEAM = "teamExchange";
        channel.exchangeDeclare(EXCHANGE_TEAM, BuiltinExchangeType.TOPIC);

        String EXCHANGE_SUPP = "suppExchange";
        channel.exchangeDeclare(EXCHANGE_SUPP, BuiltinExchangeType.TOPIC);

        String EXCHANGE_ADMIN = "adminExchange";
        channel.exchangeDeclare(EXCHANGE_ADMIN, BuiltinExchangeType.DIRECT);

        String prodQueue;
        // declaring queues and binding
        //create and bind product queue to supplier
        for(String product: products){
            prodQueue = channel.queueDeclare(product, false, false, false, null).getQueue();
            channel.basicQos(1);
            channel.queueBind(prodQueue, EXCHANGE_TEAM, product);
        }
        //create adminQueue to allow admin to send messages
        String adminQueue = channel.queueDeclare(username, false, false, false, null).getQueue();
        channel.queueBind(adminQueue, EXCHANGE_ADMIN, "dostawcy");
        channel.basicQos(1);


        //receive message
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                //message structure: teamName.product
                String[] message = new String(body, "UTF-8").split("\\.");
                System.out.println("Otrzymano zamówienie: " + message[1] + " od " + message[0]);
                String teamName = message[0];
                String reply = username + ".Zamówienie " + orderID + " na " + message[1]
                        + " zrealizowano dla " + teamName;
                //add to order list
                orders.add(orderID, message[0]+"."+message[1]);
                orderID++;
                //key to confirm queue for every order is team's username
                channel.basicPublish(EXCHANGE_SUPP, teamName, null, reply.getBytes("UTF-8"));
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };
        //receive messages from admin
        Consumer adminConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String[] message = new String(body, "UTF-8").split("\\.");
                System.out.println("Wiadomość od " + message[0] + ":" + message[1]);
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };
        System.out.println("Odbieranie zamowien:");

        for(String productQueue: products){
            channel.basicConsume(productQueue, false, consumer);
        }
        channel.basicConsume(adminQueue, false, adminConsumer);

    }

}
