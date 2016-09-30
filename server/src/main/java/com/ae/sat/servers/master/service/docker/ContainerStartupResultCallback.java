package com.ae.sat.servers.master.service.docker;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by ae on 16-7-16.
 */
public class ContainerStartupResultCallback implements ResultCallback<Frame> {

    Logger logger = LoggerFactory.getLogger(getClass());

    private String requiredLine;
    private final CountDownLatch completed = new CountDownLatch(1);
    private Closeable stream;
    boolean seenLine = false;

    public ContainerStartupResultCallback(String requiredLine) {
        this.requiredLine = requiredLine;
    }

    public boolean awaitCompletion(long timeout, TimeUnit timeUnit) throws InterruptedException {
        return completed.await(timeout, timeUnit)
               && (requiredLine == null || seenLine);
    }

    public void awaitCompletion() throws InterruptedException {
        completed.await();
    }


    @Override
    public void onStart(Closeable closeable) {
        this.stream = closeable;
    }

    @Override
    public void onNext(Frame frame) {
        String line = new String(frame.getPayload()).replace("\n", "").replace("\r", "");
        logger.info(String.format("%s ..", line));

        if (requiredLine != null && requiredLine.equals(line)) {
            logger.info("Yeah I saw the needed line ..");
            seenLine = true;
            completed.countDown();
            try {
                close();
            } catch (IOException e) {
                logger.warn("Could not close", e);
            }
        }
    }

    @Override
    public void onError(Throwable throwable) {
        logger.info(String.format("Recieved error %s", throwable));
    }

    @Override
    public void onComplete() {
        if (requiredLine == null || (!seenLine) && completed.getCount() >0) {
            completed.countDown();
        }
    }

    @Override
    public void close() throws IOException {
        if (stream != null) {
            stream.close();
        }
        stream = null;
    }
}
