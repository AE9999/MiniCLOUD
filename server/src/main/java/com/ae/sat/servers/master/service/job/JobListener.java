package com.ae.sat.servers.master.service.job;

import com.ae.sat.message.ContentType;
import com.ae.sat.message.Message;
import com.ae.sat.message.Stats;
import com.ae.sat.model.Answer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by ae on 24-5-16.
 */
public class JobListener implements Runnable {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ObjectMapper objectMapper = new ObjectMapper();
    private String informationChannelName;
    private Channel channel;
    private Job job;
    private Consumer consumer;

    public JobListener(String informationChannelName,
                       Channel channel) {
        this.informationChannelName = informationChannelName;
        this.channel = channel;
    }

    @Override
    public void run() {
        try {
            channel.exchangeDeclare(informationChannelName, "fanout");
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, informationChannelName, "");
            consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String messageRaw = new String(body, "UTF-8");
                    Message message = objectMapper.readValue(messageRaw, Message.class);
                    if (message.getContentType() == ContentType.ANSWER) {
                        Answer answer;
                        answer = objectMapper.readValue(message.getContent(), Answer.class);
                        job.recieveAnswer(answer);
                    } else {
                        Stats stats = objectMapper.readValue(message.getContent(), Stats.class);
                        job.recieveStatistics(stats);
                    }
                }
            };
            channel.basicConsume(queueName, true, consumer);
        } catch (IOException e) {
            log.error("Unexpected error", e);
        }
    }

    protected void setJob(Job job) {
        this.job = job;
    }

    protected Job getJob() {
        return job;
    }

    protected synchronized void stop() {
        if (consumer != null) {
            //consumer.
        }
        consumer = null;
    }
}

