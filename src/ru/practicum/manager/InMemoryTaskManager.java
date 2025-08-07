package ru.practicum.manager;

import ru.practicum.task.Task;
import ru.practicum.task.Epic;
import ru.practicum.task.Subtask;
import ru.practicum.enums.Status;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final HistoryManager historyManager = Managers.getDefaultHistory();

    protected final Set<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime,
            Comparator.nullsLast(Comparator.naturalOrder())));

    protected int nextID = 1;

    public int getNextID() {
        return nextID++;
    }

    private void validateAndAddTask(Task task) {
        if (isOverlapping(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с существующей!");
        }
        prioritizedTasks.add(task);
    }

    private boolean isOverlapping(Task newTask) {
        if (newTask.getStartTime() == null) {
            return false;
        }

        for (Task existingTask : getPrioritizedTasks()) {
            if (existingTask.getStartTime() != null) {
                LocalDateTime start1 = newTask.getStartTime();
                LocalDateTime end1 = newTask.getEndTime();
                LocalDateTime start2 = existingTask.getStartTime();
                LocalDateTime end2 = existingTask.getEndTime();

                if (start1.isBefore(end2) && start2.isBefore(end1)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public Task addTask(Task task) {
        task.setId(getNextID());
        validateAndAddTask(task);
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic addEpic(Epic epic) {
        epic.setId(getNextID());
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicID());
        if (epic == null) {
            return null;
        }
        subtask.setId(getNextID());
        validateAndAddTask(subtask);
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtask(subtask);
        updateEpicStatus(epic);
        updateEpicTime(epic);
        return subtask;
    }

    private void updateEpicTime(Epic epic) {
        epic.setStartTime(epic.getStartTime());
        epic.setDuration(epic.getDuration());
    }

    @Override
    public Task updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            return null;
        }
        prioritizedTasks.remove(tasks.get(task.getId()));
        validateAndAddTask(task);
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            return null;
        }
        final Epic savedEpic = epics.get(epic.getId());
        savedEpic.setName(epic.getName());
        savedEpic.setDescription(epic.getDescription());
        return savedEpic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            return null;
        }
        Epic epic = epics.get(subtask.getEpicID());
        if (epic == null) {
            return null;
        }
        prioritizedTasks.remove(subtasks.get(subtask.getId()));
        validateAndAddTask(subtask);
        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(epic);
        updateEpicTime(epic);
        return subtask;
    }

    @Override
    public Task getTaskByID(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpicByID(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtaskByID(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Subtask> getEpicSubtasks(Epic epic) {
        return subtasks.values().stream()
                .filter(subtask -> subtask.getEpicID() == epic.getId())
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTasks() {
        for (Integer taskId : tasks.keySet()) {
            historyManager.remove(taskId);
        }
        tasks.clear();
    }

    @Override
    public void deleteEpics() {
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
            for (Subtask subtask : epic.getSubtaskList()) {
                historyManager.remove(subtask.getId());
            }
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
        }
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            updateEpicStatus(epic);
        }
    }

    @Override
    public Task deleteTaskByID(int id) {
        Task removedTask = tasks.remove(id);
        if (removedTask != null) {
            historyManager.remove(id);
            prioritizedTasks.remove(removedTask);
        }
        return removedTask;
    }

    @Override
    public Epic deleteEpicByID(int id) {
        Epic removedEpic = epics.remove(id);
        if (removedEpic != null) {
            historyManager.remove(id);
            for (Subtask subtask : removedEpic.getSubtaskList()) {
                subtasks.remove(subtask.getId());
                historyManager.remove(subtask.getId());
            }
        }
        return removedEpic;
    }

    @Override
    public Subtask deleteSubtaskByID(int id) {
        Subtask removedSubtask = subtasks.remove(id);
        if (removedSubtask != null) {
            Epic epic = epics.get(removedSubtask.getEpicID());
            epic.getSubtaskList().remove(removedSubtask);
            updateEpicStatus(epic);
            historyManager.remove(id);
        }
        return removedSubtask;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void updateEpicStatus(Epic epic) {
        if (epic.getSubtaskList().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        int newCount = 0;
        int doneCount = 0;

        for (Subtask subtask : epic.getSubtaskList()) {
            if (subtask.getStatus() == Status.NEW) {
                newCount++;
            } else if (subtask.getStatus() == Status.DONE) {
                doneCount++;
            }
        }

        if (doneCount == epic.getSubtaskList().size()) {
            epic.setStatus(Status.DONE);
        } else if (newCount == epic.getSubtaskList().size()) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}