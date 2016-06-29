package com.ae.sat.servers.master.service.rabbitmq.machine;

import com.ae.sat.servers.master.service.docker.DockerClientService;
import com.ae.sat.servers.master.service.docker.DockerClientServiceFactory;
import com.ae.sat.servers.master.service.rabbitmq.RabbitMQService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Created by ae on 27-5-16.
 */

@Component
public class DockerMachineRabbitMqService extends RabbitMQService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private DockerClientServiceFactory dockerClientServiceFactory;

    private ConnectionFactory connectionFactory = null;
    private CreateContainerResponse rabbitContainer;
    private DockerClient client;
    private final int stdRabbitPort = 5672;

    private ConnectionFactory createConnectionFactory() {
        try {
            DockerClientService clientService;
            clientService = dockerClientServiceFactory.getDockerClientService();
            log.info(String.format("Recieved %s to run my rabbit service on ..",
                    clientService));
            client =  clientService.getNewDockerClient();

            ExposedPort tcpRabbit = ExposedPort.tcp(stdRabbitPort);
            Ports portBindings = new Ports();
            portBindings.bind(tcpRabbit, Ports.binding(rabbitPort));
            log.info("Pulling RabbitMQ, might take a while ..");
            client.pullImageCmd("rabbitmq:3.5.1-management")
                    .exec(new PullImageResultCallback())
                    .awaitSuccess();
            log.info("Done with pulling creating the container now ..");
            rabbitContainer  = client.createContainerCmd("rabbitmq:3.5.1-management")
                    .withNetworkMode("host")
                    .withPortBindings(portBindings)
                    .exec();
            log.info("Rabbit running the container..");
            client.startContainerCmd(rabbitContainer.getId()).exec();
            log.info("Rabbit MQ should be up by now ..");
            Thread.sleep(3000); // Yeah I know lazy but allow server to start.

            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(clientService.ip());
            connectionFactory.setPort(rabbitPort);
            connectionFactory.setUsername(rabbitUsername);
            connectionFactory.setPassword(rabbitPassword);
            return connectionFactory;
        } catch (InterruptedException |
                IOException  e) {
            log.error("Could not open RabbitMQ connection", e);
            return null;
        }
    }

    @Override
    public ConnectionFactory getConnectionFactory() {
        if (connectionFactory == null) {
            ConnectionFactory createdFactory = createConnectionFactory();
            if (createdFactory != null) {
                // Ensure docker is up
                int attempt = 0;
                final int maxAttempts = 5;
                long waitTime = 3000;
                do {
                    try {
                        Thread.sleep(waitTime);
                        Connection connection = createdFactory.newConnection();
                        connection.close();
                        connectionFactory = createdFactory;
                    } catch (TimeoutException | InterruptedException | IOException e) {
                        log.info("Looks like rabbit is not up yet ..");
                    }
                } while (connectionFactory == null && (++attempt) < maxAttempts);
            }
        }
        return connectionFactory;
    }

    @Override
    public void close() {

    }
}
