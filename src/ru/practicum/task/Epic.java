package ru.practicum.task;

import ru.practicum.enums.Status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private final List<Subtask> subtaskList = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description);
    }

    public Epic(int id, String name, String description, Status status) {
        super(id, name, description, status);
    }

    @Override
    public Duration getDuration() {
        Duration epicDuration = Duration.ZERO;
        for (Subtask subtask : subtaskList) {
            epicDuration = epicDuration.plus(subtask.getDuration());
        }
        return epicDuration;
    }

    @Override
    public LocalDateTime getStartTime() {
        if (subtaskList.isEmpty()) {
            return null;
        }
        LocalDateTime earliestStart = null;
        for (Subtask subtask : subtaskList) {
            if (subtask.getStartTime() != null) {
                if (earliestStart == null || subtask.getStartTime().isBefore(earliestStart)) {
                    earliestStart = subtask.getStartTime();
                }
            }
        }
        return earliestStart;
    }

    @Override
    public LocalDateTime getEndTime() {
        if (subtaskList.isEmpty()) {
            return null;
        }
        LocalDateTime latestEnd = null;
        for (Subtask subtask : subtaskList) {
            if (subtask.getEndTime() != null) {
                if (latestEnd == null || subtask.getEndTime().isAfter(latestEnd)) {
                    latestEnd = subtask.getEndTime();
                }
            }
        }
        this.endTime = latestEnd;
        return this.endTime;
    }

    public void addSubtask(Subtask subtask) {
        subtaskList.add(subtask);
    }

    public void clearSubtasks() {
        subtaskList.clear();
    }


    public List<Subtask> getSubtaskList() {
        return subtaskList;
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