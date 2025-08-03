package ru.practicum.manager;

import ru.practicum.enums.Status;
import ru.practicum.exception.ManagerSaveException;
import ru.practicum.task.Epic;
import ru.practicum.task.Subtask;
import ru.practicum.task.Task;
import ru.practicum.task.TaskType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;
    private static final String HEADER = "id,type,name,status,description,epic\n";

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(HEADER);

            List<Task> allTasks = new ArrayList<>();
            allTasks.addAll(tasks.values());
            allTasks.addAll(epics.values());
            allTasks.addAll(subtasks.values());

            for (Task task : allTasks) {
                writer.write(toString(task) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось сохранить задачи в файл: " + file.getName(), e);
        }
    }

    private String toString(Task task) {
        String epicId = "";
        TaskType type;
        if (task instanceof Epic) {
            type = TaskType.EPIC;
        } else if (task instanceof Subtask) {
            type = TaskType.SUBTASK;
            epicId = String.valueOf(((Subtask) task).getEpicID());
        } else {
            type = TaskType.TASK;
        }
        return String.join(",",
                String.valueOf(task.getId()),
                type.toString(),
                task.getName(),
                task.getStatus().toString(),
                task.getDescription(),
                epicId
        );
    }

    private Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];

        if (type == TaskType.SUBTASK) {
            int epicId = Integer.parseInt(parts[5]);
            return new Subtask(id, name, description, status, epicId);
        } else if (type == TaskType.EPIC) {
            return new Epic(id, name, description, status);
        } else {
            return new Task(id, name, description, status);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            int maxId = 0;

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isEmpty()) continue;

                Task task = manager.fromString(line);
                if (task.getId() > maxId) {
                    maxId = task.getId();
                }

                if (task instanceof Epic) {
                    manager.epics.put(task.getId(), (Epic) task);
                } else if (task instanceof Subtask) {
                    manager.subtasks.put(task.getId(), (Subtask) task);
                } else {
                    manager.tasks.put(task.getId(), task);
                }
            }

            for(Subtask subtask : manager.subtasks.values()){
                Epic epic = manager.epics.get(subtask.getEpicID());
                if(epic != null){
                    epic.addSubtask(subtask);
                }
            }
            manager.nextID = maxId + 1;
        } catch (IOException e) {
        }
        return manager;
    }

    @Override
    public Task addTask(Task task) {
        super.addTask(task);
        save();
        return task;
    }

    @Override
    public Epic addEpic(Epic epic) {
        super.addEpic(epic);
        save();
        return epic;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
        return subtask;
    }

    @Override
    public Task updateTask(Task task) {
        super.updateTask(task);
        save();
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
        return epic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
        return subtask;
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    @Override
    public Task deleteTaskByID(int id) {
        Task removedTask = super.deleteTaskByID(id);
        save();
        return removedTask;
    }

    @Override
    public Epic deleteEpicByID(int id) {
        Epic removedEpic = super.deleteEpicByID(id);
        save();
        return removedEpic;
    }

    @Override
    public Subtask deleteSubtaskByID(int id) {
        Subtask removedSubtask = super.deleteSubtaskByID(id);
        save();
        return removedSubtask;
    }
}