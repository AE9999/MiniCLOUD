package com.ae.docker;

import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by ae on 7-9-16.
 */
public interface DockerClientServiceFactory {

    @Async
    Future<DockerClientService> getDockerClientService(int inpact)
                                throws ExecutionException,
                                       CannotProvideException,
                                       InterruptedException;

    final class CannotProvideException extends Exception {
        public CannotProvideException(String m) {
            super(m);
        }
    }
}
