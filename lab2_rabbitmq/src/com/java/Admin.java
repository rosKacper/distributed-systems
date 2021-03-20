package com.java;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;


public class Admin {

    public Admin(){}

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
        //create teamQueue to send messages from all teams
        String teamQueue = channel.queueDeclare("team->admin", false, false, false, null).getQueue();
        channel.queueBind(teamQueue, EXCHANGE_TEAM, "#");

        //create suppQueue to receive messages from all suppliers
        String suppQueue = channel.queueDeclare("supp->admin", false, false, false, null).getQueue();
        channel.queueBind(suppQueue, EXCHANGE_SUPP, "#");

        //receive message
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String[] message = new String(body, "UTF-8").split("\\.");
                System.out.println(message[0] + ": " + message[1]);
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        channel.basicConsume(teamQueue, false, consumer);
        channel.basicConsume(suppQueue, false, consumer);


        while (true) {

            // read msg
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Wyślij wiadomość [adresat:wiadomość]: ");
            System.out.println("(ekipy, dostawcy, wszyscy)");
            String message = br.readLine();
            String key = message.split(":")[0];
            if(message.length() < 2){
                System.out.println("Niepoprawny format. Wiadomość powinna mieć format adresat:wiadomość.");
                continue;
            }
            message = "admin." + message.split(":")[1];

            if (key.equals("ekipy") || key.equals("dostawcy")){
                channel.basicPublish(EXCHANGE_ADMIN, key, null, message.getBytes("UTF-8"));
            }
            else if(key.equals("wszyscy")){
                channel.basicPublish(EXCHANGE_ADMIN, "ekipy", null, message.getBytes("UTF-8"));
                channel.basicPublish(EXCHANGE_ADMIN, "dostawcy", null, message.getBytes("UTF-8"));
            }
            else if(key.equals("quit") || message.equals("quit")){
                break;
            }
            else{
                System.out.println("Podano niepoprawną ekipę");
            }
        }
    }
}
