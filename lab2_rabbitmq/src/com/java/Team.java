package com.java;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;


public class Team {

    private String username;

    public Team(String teamName){
        this.username = teamName;
    }

    public void launch() throws IOException, TimeoutException {

        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.basicQos(1);

        // exchanges
        String EXCHANGE_TEAM = "teamExchange";
        channel.exchangeDeclare(EXCHANGE_TEAM, BuiltinExchangeType.TOPIC);

        String EXCHANGE_SUPP = "suppExchange";
        channel.exchangeDeclare(EXCHANGE_SUPP, BuiltinExchangeType.TOPIC);

        String EXCHANGE_ADMIN = "adminExchange";
        channel.exchangeDeclare(EXCHANGE_ADMIN, BuiltinExchangeType.DIRECT);

        // declaring queues and binding
        //create adminQueue to allow admin to send messages
        String adminQueue = channel.queueDeclare(username, false, false, false, null).getQueue();
        channel.queueBind(adminQueue, EXCHANGE_ADMIN, "ekipy");

        //create confirmQueue to receive order confirmation
        String confirmQueue = channel.queueDeclare("supp->"+username, false, false, false, null).getQueue();
        channel.queueBind(adminQueue, EXCHANGE_SUPP, username);

        //receive message
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String[] message = new String(body, "UTF-8").split("\\.");
                System.out.println(message[0] + ": " + message[1]);
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };
        channel.basicConsume(adminQueue, false, consumer);
        channel.basicConsume(confirmQueue, false, consumer);

        while (true) {

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Wprowadź zamówienie: ");
            String item = br.readLine();
            String message = username + "." + item;
            // break condition
            if ("exit".equals(item)) {
                break;
            }
            //testing sequence
            if(item.equals("test")){
                for(int i = 0; i<2; i++){
                    channel.basicPublish(EXCHANGE_TEAM, "tlen", null, (username+".tlen").getBytes("UTF-8"));
                }
                for(int i = 0; i<2; i++){
                    channel.basicPublish(EXCHANGE_TEAM, "plecak", null, (username+".plecak").getBytes("UTF-8"));
                }
                for(int i = 0; i<2; i++){
                    channel.basicPublish(EXCHANGE_TEAM, "buty", null, (username+".buty").getBytes("UTF-8"));
                }
                continue;
            }

            // publish to admin
            channel.basicPublish(EXCHANGE_TEAM, item, null, message.getBytes("UTF-8"));
            System.out.println("Sent: " + item);
            //receive from admin

        }

    }

}
