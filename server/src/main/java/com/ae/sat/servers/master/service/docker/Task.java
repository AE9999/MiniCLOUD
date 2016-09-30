package com.ae.sat.servers.master.service.docker;

import java.util.UUID;

/**
 * Created by ae on 28-5-16.
 */
public final class Task {
    private UUID id;

    private int inpact;

    public Task(int inpact) {
        this.id = UUID.randomUUID();
        this.inpact = inpact;
    }

    public UUID getId() {
        return id;
    }

    public int getInpact() {
        return inpact;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;

        if (inpact != task.inpact) return false;
        return id != null ? id.equals(task.id) : task.id == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + inpact;
        return result;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", inpact=" + inpact +
                '}';
    }
}
