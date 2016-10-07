package com.ae.sat.servers.master;

import com.ae.sat.servers.master.service.preprocess.MinisatPreprocessService;
import com.ae.sat.servers.master.service.preprocess.PreProcessService;
import com.ae.sat.servers.master.service.rabbitmq.RabbitMQService;
import com.ae.sat.servers.master.service.rabbitmq.machine.DockerMachineRabbitMqService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executor;

@EnableAutoConfiguration
@EnableScheduling
@EnableAsync
@SpringBootApplication
public class App {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Profile({"localvm", "oceanvm"})
	@Bean
	public RabbitMQService localSwarmConnectionFactory() {
		return new DockerMachineRabbitMqService();
	}

	@Bean
	public PreProcessService preProcessService() {
		return new MinisatPreprocessService();
	}

	@Bean
	public CommonsMultipartResolver multipartResolver() {
		CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
//		commonsMultipartResolver.setMaxUploadSize();
		return commonsMultipartResolver;
	}

	//@Bean
	//public ExecutorService getTaskExecutor() {
	//	return Executors.newScheduledThreadPool(4);
	//}
	@Bean
	public Executor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(50);
		executor.setQueueCapacity(1024);
		executor.initialize();
		return executor;
	}

	@Bean
	public SpringApplicationRunListener myListner() {
		return new SpringApplicationRunListener() {

			@Override
			public void started() {}

			@Override
			public void environmentPrepared(ConfigurableEnvironment configurableEnvironment) {}

			@Override
			public void contextPrepared(ConfigurableApplicationContext configurableApplicationContext) {}

			@Override
			public void contextLoaded(ConfigurableApplicationContext configurableApplicationContext) {
				// Will not work because we are running inside docker container.
				String url = "http://localhost:8080";

				if(Desktop.isDesktopSupported()){
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.browse(new URI(url));
					} catch (IOException | URISyntaxException e) {
						log.error("Strange error", e);
					}
				}
			}

			@Override
			public void finished(ConfigurableApplicationContext configurableApplicationContext, Throwable throwable) {}
		};
	}


	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
}
