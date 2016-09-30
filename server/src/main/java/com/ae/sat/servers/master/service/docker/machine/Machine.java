package com.ae.sat.servers.master.service.docker.machine;

import com.ae.sat.servers.master.service.docker.MyDockerClientConfigBuilder;
import com.ae.sat.servers.master.service.docker.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by ae on 12-6-16.
 */
public class Machine {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private String id;

    private List<Task> tasks = new ArrayList<>();

    private int inititalCapacity; // Improve this to ask wherefor

    private Future<MyDockerClientConfigBuilder> dockerClientConfigBuilder;

    private LocalDateTime creationTime;

    public Machine(String id, int inititalCapacity) {
        this.id = id;
        this.inititalCapacity = inititalCapacity;
        this.creationTime =  LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    protected int doGetCapacity() {
        return inititalCapacity - tasks.stream().mapToInt(Task::getInpact).sum();
    }

    public int capacity() {
        synchronized (tasks) {
            int capacity = doGetCapacity();
            return capacity > 0 ? capacity : 0;
        }
    }

    public List<Task> getActiveTasks() {
        synchronized (tasks) {
            return new ArrayList<>(tasks);
        }
    }

    public void addTask(Task task) {
        synchronized (tasks) {
            log.info(String.format("Actually assigning %s to %s ..", task, this));
            int capacity = doGetCapacity();
            if (capacity - task.getInpact() < 0) {
                throw new IllegalStateException("Trying to assign task to full machine ..");
            }
            tasks.add(task);
        }
    }

    public void removeTask(Task task) {
        synchronized (tasks) {
            log.info(String.format("Actually removing %s from %s ..", this, task));
            tasks.remove(task);
        }
    }

    public Future<MyDockerClientConfigBuilder> getDockerClientConfigBuilder() {
        return dockerClientConfigBuilder;
    }

    public void setDockerClientConfigBuilder(Future<MyDockerClientConfigBuilder> dockerClientConfigBuilder) {
        this.dockerClientConfigBuilder = dockerClientConfigBuilder;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }
}
