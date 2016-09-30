package com.ae.docker.machine;

import com.ae.docker.AbstractDockerClientServiceFactoryImpl;
import com.ae.docker.Task;
import com.ae.docker.MyDockerClientConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by ae on 28-5-16.
 */

@Profile({"localvmDocker", "oceanvmDocker", "mixedDocker"})
@Component
public class MachineDockerClientServiceFactory extends AbstractDockerClientServiceFactoryImpl {

    private Map<UUID, Machine> taskToMachines = new HashMap<>();

    private Map<String, Machine> name2Machine = new HashMap<>();

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private MachineDockerClientConfigBuilderService dockerClientConfigBuilderService;

    @Value("${dockerMachineTimeoutInMinutes}")
    private long dockerMachineTimeoutInMinutes;

    @Value("${initialMinimalCapacity}")
    private long machineMinimalCapacity;

    private Machine getMachine() {
        final int initialCap = dockerClientConfigBuilderService.getMaxInpactForSingeMachine();
        final String name = UUID.randomUUID().toString().replace("-", "");
        final Machine machine = new Machine(name, initialCap);
        name2Machine.put(name, machine);

        Runnable r = () -> {
            synchronized (name2Machine) {
                name2Machine.remove(name);
            }
            getMachine();
        };

        Future<MyDockerClientConfigBuilder> config = null;
        config = dockerClientConfigBuilderService.getDockerClientConfigBuilder(name, r);
        machine.setDockerClientConfigBuilder(config);
        return machine;
    }

    private List<Machine> getMachines(long capacity) {
        int amountNeeded = new Double(Math.ceil(capacity / (double) getMaxInpactForSingeMachine())).intValue();
        if (amountNeeded < 1) { return new ArrayList<>(); }
        return IntStream.rangeClosed(1, amountNeeded)
                        .mapToObj(f -> getMachine())
                        .collect(Collectors.toList());
    }

    @PostConstruct
    private void init() {
        synchronized (name2Machine) {
            getMachines(machineMinimalCapacity);
        }
    }

    @Override
    protected Future<MyDockerClientConfigBuilder> doGetNewDockerService(Task task) throws ExecutionException {
        synchronized (name2Machine) {
            Machine machine;
            machine = name2Machine.values().stream()
                                           .filter(m -> m.getDockerClientConfigBuilder().isDone()
                                                   && m.capacity() >= task.getInpact())
                                           .findFirst()
                                           .orElse(null);
            if (machine == null) {
                machine = name2Machine.values().stream()
                                               .filter(m -> m.capacity() >= task.getInpact())
                                               .findFirst()
                                               .orElse(null);
            }

            if (machine == null) {
                machine = getMachine();
            }

            if (machineMinimalCapacity - doGetCapacity() > 0) {
                getMachines(machineMinimalCapacity - doGetCapacity());
            }

            log.info(String.format("Assigning %s to %s ..", task, machine));
            taskToMachines.put(task.getId(), machine);
            machine.addTask(task);
            return machine.getDockerClientConfigBuilder();
        }
    }

    @Scheduled(initialDelay = 18000,
            fixedRateString= "${shutDownMachines.fixedRateString}")
    protected void shutDownMachines() {
        log.info("Checking if we can shut down superflous machines ..");
        synchronized (name2Machine) {
            int capacity = doGetCapacity();
            log.info(String.format("Current capacity: %s ..", capacity));

            if (capacity < machineMinimalCapacity) {
                log.info(String.format("Current capacity insufficient: %s < %s not shutting down machines ..",
                        capacity,
                        machineMinimalCapacity));
                return;
            }

            log.info("Selecting machines to shut down.");
            List<Machine> activeMachine_;
            activeMachine_ = name2Machine.values().stream()
                                                  .filter(f -> f.getDockerClientConfigBuilder().isDone())      // Machine must have been delivered before we can delete is
                                                  .filter(f -> f.getActiveTasks().isEmpty()) // Don't delete machines that run tasks
                                                  .sorted((l, r) -> {                        // Destroy old machines first.
                                                        if (l.getCreationTime().isAfter(r.getCreationTime())) {
                                                            return 1;
                                                        } else if (l.getCreationTime().isAfter(r.getCreationTime())) {
                                                            return -1;
                                                        }
                                                        return 0;
                                                  })
                                                  .collect(Collectors.toList());              // Compose the list
            int killRate = 0; // Make this a predicate ..
            List<Machine> toKill = new ArrayList<>();
            for (int i = 0;
                 (i < activeMachine_.size()
                         && (capacity - (killRate + activeMachine_.get(i).capacity())
                         >= machineMinimalCapacity));
                 i++) {
                killRate += activeMachine_.get(i).capacity();
                toKill.add(activeMachine_.get(i));
                log.info(String.format("Killrate: %s -> %s ..",
                                        killRate,
                                        machineMinimalCapacity));
            }
            toKill.parallelStream()                          // Run in parralel.
                    .forEach(f -> {                            // Remove services.
                        log.info(String.format("Due to overcapacity we are deleting %s ..",
                                f));
                        try {
                            String name = f.getId();
                            name2Machine.remove(name);
                            dockerClientConfigBuilderService.destroyMachineDockerClientConfigBuilderByName(name);
                        } catch (IOException e) {
                            log.error("Could not delete machine", e);
                        }
                    });
            toKill.stream().forEach(f -> name2Machine.remove(f));
        }
        log.info("Done checking if we can delete machines ..");
    }

    @Override
    protected void close(Task task) {
        synchronized (name2Machine) {
            log.info(String.format("Closing task %s ..", task));
            Machine machine = taskToMachines.get(task.getId());
            machine.removeTask(task);
            taskToMachines.remove(task.getId());
        }
    }

    @Override
    protected int getMaxInpactForSingeMachine() {
        return dockerClientConfigBuilderService.getMaxInpactForSingeMachine();
    }

    private int doGetCapacity() {
        return name2Machine.values().stream()
                                    .filter(f -> f.getDockerClientConfigBuilder().isDone())
                                    .mapToInt(Machine::capacity).sum();
    }
}
