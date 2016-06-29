package com.ae.sat.servers.master.service.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by ae on 27-5-16.
 */
public abstract class RabbitMQService {

    @Value("${rabbitHost}")
    protected String rabbitHost;

    @Value("${rabbitPort}")
    protected int rabbitPort;

    @Value("${rabbitUsername}")
    protected String rabbitUsername;

    @Value("${rabbitPassword}")
    protected String rabbitPassword;

    public abstract ConnectionFactory getConnectionFactory();

    public abstract void close();
}
