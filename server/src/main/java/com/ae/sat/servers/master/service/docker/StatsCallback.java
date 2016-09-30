package com.ae.docker;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.async.ResultCallbackTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by ae on 5-6-16.
 */
public class StatsCallback implements ResultCallback<Statistics> {

    private final CountDownLatch statsLatch = new CountDownLatch(1);

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Statistics statistics;

    private Closeable stream;

    private boolean closed = false;

    public synchronized Statistics getStatistics() {
        return statistics;
    }

    private synchronized void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    @Override
    public void onStart(Closeable closeable) {
        logger.info("Starting stuff ..");
        this.stream = closeable;
    }

    @Override
    public void onNext(Statistics statistics) {
        setStatistics(statistics);
        statsLatch.countDown();
    }

    @Override
    public void onError(Throwable throwable) {
        if (closed) { return; }

        try {
            logger.error("Unexpected error", throwable);
        } finally {
            try {
                close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onComplete() {
        logger.info("Stream complete  ..");
        stream = null;
        statsLatch.countDown();
    }

    @Override
    public void close() throws IOException {
        logger.info("Closing stream ..");
        closed = true;
        if (stream != null) {
            stream.close();
        }
        stream = null;
    }
}
