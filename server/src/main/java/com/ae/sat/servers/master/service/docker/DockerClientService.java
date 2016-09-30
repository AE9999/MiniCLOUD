package com.ae.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.ErrorDetail;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.ResponseItem;
import com.github.dockerjava.core.command.PullImageResultCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * Created by ae on 4-2-16.
 */
public interface DockerClientService {

  Logger logger = LoggerFactory.getLogger(DockerClientService.class);

  UUID id = UUID.randomUUID();

  default  String id() { return id.toString(); }

  String ip();

  int port();

  SafeDockerClient getNewDockerClient();

  void close();

  default void downloadImages(List<String> imageNames) throws IOException {
    DockerClient client = getNewDockerClient();

    logger.info("Obtained new docker client ..");
    List<Image> presentImages = client.listImagesCmd().exec();
    logger.info(String.format("Downloaded present images %s ..", presentImages));
    for (String imageName : imageNames) {
      String tagName = imageName.contains(":") ? imageName
                                               : String.format("%s:latest", imageName);
      logger.info(String.format("Checking %s ..", tagName));
      boolean needsImage;
      needsImage = presentImages.stream()
                                .map(f -> Arrays.asList(f.getRepoTags()))
                                .noneMatch(f -> f.contains(tagName));
      if (needsImage) {
        logger.info(String.format("Pulling %s, might take a while ..", imageName));
        final CountDownLatch completed = new CountDownLatch(1);
        boolean ok;
        ok = client.pullImageCmd(imageName).exec(new ResultCallback<PullResponseItem>() {
          private boolean ok = true;

          @Override
          public void onStart(Closeable closeable) {}

          @Override
          public void onNext(PullResponseItem object) {
            logger.trace(String.format("Recieved info %s ..", object));
            if (object.isErrorIndicated()) {
              ResponseItem.ErrorDetail errorDetail = object.getErrorDetail();
              String m;
              if (errorDetail == null) {
                m = "";
              }
              else {
                m = errorDetail.getMessage();
              }
              throw new IllegalStateException(m);
            }
          }

          @Override
          public void onError(Throwable throwable) {
            ok = false;
          }

          @Override
          public void onComplete() {
            completed.countDown();
          }

          @Override
          public void close() throws IOException {}

          public boolean ok() {
            try {
              completed.await();
              return ok;
            } catch (InterruptedException e) {
              logger.error("Unexpected exception", e);
              return false;
            }
          }
        }).ok();
        if (!ok) {
          throw new IOException("Could not download image ..");
        }
        logger.info("Done with pulling  ..");
      }
    }
    client.close();
  }
}

