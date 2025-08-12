package ru.practicum.manager;

import ru.practicum.enums.Status;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.TaskValidationException;
import ru.practicum.task.Epic;
import ru.practicum.task.Subtask;
import ru.practicum.task.Task;

import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final HistoryManager historyManager = Managers.getDefaultHistory();

    protected final Set<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime,
            Comparator.nullsLast(Comparator.naturalOrder())));

    protected int nextID = 1;

    protected int getNextID() {
        return nextID++;
    }

    private void validate(Task task) {
        if (isOverlapping(task)) {
            throw new TaskValidationException("Задача пересекается по времени с существующей!");
        }
    }

    private boolean isOverlapping(Task newTask) {
        if (newTask.getStartTime() == null) {
            return false;
        }

        return prioritizedTasks.stream()
                .filter(existingTask -> existingTask.getId() != newTask.getId() && existingTask.getStartTime() != null)
                .anyMatch(existingTask -> {
                    LocalDateTime start1 = newTask.getStartTime();
                    LocalDateTime end1 = newTask.getEndTime();
                    LocalDateTime start2 = existingTask.getStartTime();
                    LocalDateTime end2 = existingTask.getEndTime();
                    return start1.isBefore(end2) && start2.isBefore(end1);
                });
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public Task addTask(Task task) {
        validate(task);
        task.setId(getNextID());
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
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
            throw new NotFoundException("Эпик с id=" + subtask.getEpicID() + " не найден.");
        }
        validate(subtask);
        subtask.setId(getNextID());
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtask(subtask);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
        updateEpicStatus(epic);
        updateEpicTime(epic);
        return subtask;
    }

    @Override
    public Task updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            throw new NotFoundException("Задача с id=" + task.getId() + " не найдена.");
        }
        validate(task);
        Task oldTask = tasks.get(task.getId());
        if (oldTask.getStartTime() != null) {
            prioritizedTasks.remove(oldTask);
        }
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        final Epic savedEpic = epics.get(epic.getId());
        if (savedEpic == null) {
            throw new NotFoundException("Эпик с id=" + epic.getId() + " не найден.");
        }
        savedEpic.setName(epic.getName());
        savedEpic.setDescription(epic.getDescription());
        return savedEpic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            throw new NotFoundException("Подзадача с id=" + subtask.getId() + " не найдена.");
        }
        Epic epic = epics.get(subtask.getEpicID());
        if (epic == null) {
            throw new NotFoundException("Эпик для подзадачи с id=" + subtask.getId() + " не найден.");
        }
        validate(subtask);
        Subtask oldSubtask = subtasks.get(subtask.getId());
        if (oldSubtask.getStartTime() != null) {
            prioritizedTasks.remove(oldSubtask);
        }
        subtasks.put(subtask.getId(), subtask);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
        updateEpicStatus(epic);
        updateEpicTime(epic);
        return subtask;
    }

    @Override
    public Task getTaskByID(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            throw new NotFoundException("Задача с id=" + id + " не найдена.");
        }
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpicByID(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            throw new NotFoundException("Эпик с id=" + id + " не найден.");
        }
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtaskByID(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new NotFoundException("Подзадача с id=" + id + " не найдена.");
        }
        historyManager.add(subtask);
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
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new NotFoundException("Эпик с id=" + epicId + " не найден.");
        }
        return epic.getSubtaskList();
    }

    @Override
    public void deleteTasks() {
        tasks.values().forEach(task -> {
            if (task.getStartTime() != null) {
                prioritizedTasks.remove(task);
            }
            historyManager.remove(task.getId());
        });
        tasks.clear();
    }

    @Override
    public void deleteEpics() {
        epics.values().forEach(epic -> {
            historyManager.remove(epic.getId());
            epic.getSubtaskList().forEach(subtask -> {
                historyManager.remove(subtask.getId());
                if (subtask.getStartTime() != null) {
                    prioritizedTasks.remove(subtask);
                }
            });
        });
        subtasks.clear();
    }

    @Override
    public void deleteSubtasks() {
        subtasks.values().forEach(subtask -> {
            historyManager.remove(subtask.getId());
            if (subtask.getStartTime() != null) {
                prioritizedTasks.remove(subtask);
            }
        });
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            updateEpicStatus(epic);
            updateEpicTime(epic);
        }
    }

    @Override
    public Task deleteTaskByID(int id) {
        Task removedTask = tasks.remove(id);
        if (removedTask == null) {
            throw new NotFoundException("Задача с id=" + id + " для удаления не найдена.");
        }
        historyManager.remove(id);
        if (removedTask.getStartTime() != null) {
            prioritizedTasks.remove(removedTask);
        }
        return removedTask;
    }

    @Override
    public Epic deleteEpicByID(int id) {
        Epic removedEpic = epics.remove(id);
        if (removedEpic == null) {
            throw new NotFoundException("Эпик с id=" + id + " для удаления не найден.");
        }
        historyManager.remove(id);
        removedEpic.getSubtaskList().forEach(subtask -> {
            subtasks.remove(subtask.getId());
            historyManager.remove(subtask.getId());
            if (subtask.getStartTime() != null) {
                prioritizedTasks.remove(subtask);
            }
        });
        return removedEpic;
    }

    @Override
    public Subtask deleteSubtaskByID(int id) {
        Subtask removedSubtask = subtasks.get(id);
        if (removedSubtask == null) {
            throw new NotFoundException("Подзадача с id=" + id + " для удаления не найдена.");
        }
        Epic epic = epics.get(removedSubtask.getEpicID());
        epic.removeSubtask(removedSubtask);
        subtasks.remove(id);
        updateEpicStatus(epic);
        updateEpicTime(epic);
        historyManager.remove(id);
        if (removedSubtask.getStartTime() != null) {
            prioritizedTasks.remove(removedSubtask);
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

    private void updateEpicTime(Epic epic) {
        epic.updateEpicTime();
    }
}