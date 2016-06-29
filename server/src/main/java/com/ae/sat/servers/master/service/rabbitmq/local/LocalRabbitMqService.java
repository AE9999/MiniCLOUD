package com.ae.sat.servers.master.service.rabbitmq.local;

import com.ae.sat.servers.master.service.rabbitmq.RabbitMQService;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * Created by ae on 27-5-16.
 */

@Component
public class LocalRabbitMqService extends RabbitMQService {

    @Override
    public ConnectionFactory getConnectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setPort(rabbitPort);
        factory.setHost(rabbitHost);
        factory.setUsername(rabbitUsername);
        factory.setPassword(rabbitPassword);
        return factory;
    }

    @Override
    public void close() {

    }
}
