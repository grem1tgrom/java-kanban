package ru.practicum.task;

import ru.practicum.enums.Status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {

    private final List<Subtask> subtaskList = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description);
        this.status = Status.NEW;
    }

    public Epic(int id, String name, String description, Status status) {
        super(id, name, description, status);
    }

    public void updateEpicTime() {
        this.duration = subtaskList.stream()
                .map(Task::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);

        this.startTime = subtaskList.stream()
                .map(Task::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        this.endTime = subtaskList.stream()
                .map(Task::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }


    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void addSubtask(Subtask subtask) {
        subtaskList.add(subtask);
    }

    public void removeSubtask(Subtask subtask) {
        subtaskList.remove(subtask);
    }


    public void clearSubtasks() {
        subtaskList.clear();
        updateEpicTime();
    }


    public List<Subtask> getSubtaskList() {
        return Collections.unmodifiableList(subtaskList);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", subtasks_count=" + subtaskList.size() +
                '}';
    }
}