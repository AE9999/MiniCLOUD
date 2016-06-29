package com.ae.sat.servers.worker.service;

import com.ae.sat.message.Message;
import com.ae.sat.model.Answer;
import com.ae.sat.model.Literal;
import com.ae.sat.model.SolverAssignment;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Created by ae on 6-6-16.
 */
public abstract class Solver {
    private final Logger log = LoggerFactory.getLogger(getClass());
    protected List<Literal> incomingUnitClauses = Collections.synchronizedList(new ArrayList<>());
    protected ObjectMapper objectMapper = new ObjectMapper();

    protected String unitClauseChannelName;
    protected String informationChannelName;
    protected ConnectionFactory connectionFactory;

    private String queueName;
    protected Channel informationChannel;
    protected Channel unitChannel;

    public void setUnitClauseChannelName(String unitClauseChannelName) {
        this.unitClauseChannelName = unitClauseChannelName;
    }

    public void setInformationChannelName(String informationChannelName) {
        this.informationChannelName = informationChannelName;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    protected class QueueListener implements Runnable {

        @Override
        public void run() {
            try {
                queueName = unitChannel.queueDeclare().getQueue();
                unitChannel.queueBind(queueName, unitClauseChannelName, "");
                Consumer consumer = new DefaultConsumer(unitChannel) {
                    private ObjectMapper objectMapper = new ObjectMapper();

                    @Override
                    public void handleDelivery(String consumerTag,
                                               Envelope envelope,
                                               AMQP.BasicProperties properties, byte[] body) throws IOException {
                        String message = new String(body, "UTF-8");
                        Literal value = objectMapper.readValue(message, Literal.class);
                        synchronized (incomingUnitClauses) {
                            incomingUnitClauses.add(value);
                        }
                        log.info(" [x] Received '" + message + "'");
                    }
                };
                unitChannel.basicConsume(queueName, true, consumer);
            } catch (IOException e) {
                log.error("Unexpected error", e);
            }
        }
    }

    protected void publish(Literal learnt) {
        try {
            log.info(String.format("Publishing %s ..", learnt));
            String message = objectMapper.writeValueAsString(learnt);
            unitChannel.basicPublish(unitClauseChannelName, "", null, message.getBytes());
        }  catch (IOException e) {
            log.error("Could not publish learnt value", e);
        }
    }

    public abstract void setConfig(byte[] rawConfigData);

    public abstract Answer doSolve(SolverAssignment assignment);

    public void solve(SolverAssignment assignment) throws IOException, TimeoutException {
        log.info(String.format("Starting to solve assingment %s ..", assignment));

        Connection connection = connectionFactory.newConnection();
        informationChannel = connection.createChannel();
        unitChannel = connection.createChannel();
        unitChannel.exchangeDeclare(unitClauseChannelName, "fanout");

        log.info(String.format("Opening UnitChannel with unitClauseChannelName:%s ..",
                               unitClauseChannelName));


        QueueListener queueListener = new QueueListener();
        (new Thread(queueListener)).start();

        Answer answer = doSolve(assignment);
        log.info(String.format("Calculated %s as answer ..", answer));

        log.info("Sending answer ..");
        Message message = new Message(answer);
        String rawMessage = objectMapper.writeValueAsString(message);
        informationChannel.basicPublish(informationChannelName, "", null, rawMessage.getBytes());

        log.info("Closing infoConnection to rabbit ..");
        informationChannel.close();
        unitChannel.close();
        connection.close();
    }
}
