package com.ae.sat.servers.master.service.docker.machine.utils;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ae on 6-2-16.
 */
public class Utils {

  private final static Logger log = LoggerFactory.getLogger(Utils.class);

  private static class ProcessOutputWriter extends Thread {

    private InputStream is;
    private String prefix;
    private List<String> readOutput = new ArrayList<>();
    boolean done = false;

    public ProcessOutputWriter(InputStream is, String prefix) {
      this.is = is;
      this.prefix = prefix;
    }

    private synchronized boolean getDone() {
      return done;
    }

    private synchronized void setDone(boolean done) {
      this.done = done;
    }

    @Override
    public void run() {
      try {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        String line_out;
        while ((line_out = bufferedReader.readLine()) != null) {
          log.info(String.format("[%s] Read: %s ", prefix, line_out));
          readOutput.add(line_out);
        }
      } catch (IOException e) {
        log.error("Error while reading process", e);
      }
      setDone(true);
    }

    public List<String> getOutput() {
      start();
      while (!getDone()) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          log.error("Something strange happened ..", e);
        }
      }
      return readOutput;
    }
  }

  private static List<String> doExecuteCommands(List<String> commands) throws IOException {
    String line_out;
    ProcessBuilder pb = new ProcessBuilder(commands);
    Process p = pb.start();
    ProcessOutputWriter out = new ProcessOutputWriter(p.getInputStream(), "OUT");
    ProcessOutputWriter err = new ProcessOutputWriter(p.getErrorStream(), "ERR");
    err.start();
    return out.getOutput();
  }

  public static void exportFileFromJar(String url, String fname) throws IOException {
    URL inputUrl = Utils.class.getResource(url);
    File dest = new File(fname);
    FileUtils.copyURLToFile(inputUrl, dest);
  }

  public static List<String> executeCommandAsShArgument(String command) throws IOException {
    log.info(String.format("Running %s ..", command));
    List<String> commands = Arrays.asList(new String[]{"sh", "-c", command});
    return doExecuteCommands(commands);
  }

  public static Process executeCommandInBackGround(String command) throws IOException {
    List<String> commands = Arrays.asList(new String[]{"sh", "-c", command});
    ProcessBuilder pb = new ProcessBuilder(commands);
    Process p = pb.start();
    return p;
  }
}
