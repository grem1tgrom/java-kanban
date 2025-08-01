package ru.practicum.manager;

import ru.practicum.exception.ManagerSaveException;
import ru.practicum.task.Epic;
import ru.practicum.task.Subtask;
import ru.practicum.task.Task;
import ru.practicum.task.TaskType;
import ru.practicum.enums.Status;

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

            for (Task task : getTasks()) {
                writer.write(toString(task) + "\n");
            }
            for (Epic epic : getEpics()) {
                writer.write(toString(epic) + "\n");
            }
            for (Subtask subtask : getSubtasks()) {
                writer.write(toString(subtask) + "\n");
            }

            writer.write("\n");

            writer.write(historyToString(historyManager));

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

    private static String historyToString(HistoryManager manager) {
        List<String> ids = new ArrayList<>();
        for (Task task : manager.getHistory()) {
            ids.add(String.valueOf(task.getId()));
        }
        return String.join(",", ids);
    }

    private static List<Integer> historyFromString(String value) {
        List<Integer> ids = new ArrayList<>();
        if (value != null && !value.isEmpty()) {
            String[] split = value.split(",");
            for (String id : split) {
                ids.add(Integer.parseInt(id));
            }
        }
        return ids;
    }

    private Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];
        int epicId = -1;
        if (parts.length == 6) {
            epicId = Integer.parseInt(parts[5]);
        }

        Task task = null;
        if (type == TaskType.TASK) {
            task = new Task(id, name, description, status);
        } else if (type == TaskType.EPIC) {
            task = new Epic(id, name, description, status);
        } else if (type == TaskType.SUBTASK) {
            task = new Subtask(id, name, description, status, epicId);
        }
        return task;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");
            int maxId = 0;

            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                if (line.isEmpty()) {
                    if (i + 1 < lines.length) {
                        List<Integer> historyIds = historyFromString(lines[i + 1]);
                        for (Integer id : historyIds) {
                            if (manager.tasks.containsKey(id)) {
                                manager.historyManager.add(manager.tasks.get(id));
                            } else if (manager.epics.containsKey(id)) {
                                manager.historyManager.add(manager.epics.get(id));
                            } else if (manager.subtasks.containsKey(id)) {
                                manager.historyManager.add(manager.subtasks.get(id));
                            }
                        }
                    }
                    break;
                }

                Task task = manager.fromString(line);
                if (task.getId() > maxId) {
                    maxId = task.getId();
                }

                if (task instanceof Epic) {
                    manager.epics.put(task.getId(), (Epic) task);
                } else if (task instanceof Subtask) {
                    manager.subtasks.put(task.getId(), (Subtask) task);
                    Epic epic = manager.epics.get(((Subtask) task).getEpicID());
                    if (epic != null) {
                        epic.addSubtask((Subtask) task);
                    }
                } else {
                    manager.tasks.put(task.getId(), task);
                }
            }
            manager.nextID = maxId + 1;

        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось прочитать задачи из файла: " + file.getName(), e);
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
    public Task deleteTaskByID(int id) {
        Task removed = super.deleteTaskByID(id);
        save();
        return removed;
    }

    @Override
    public Epic deleteEpicByID(int id) {
        Epic removed = super.deleteEpicByID(id);
        save();
        return removed;
    }

    @Override
    public Subtask deleteSubtaskByID(int id) {
        Subtask removed = super.deleteSubtaskByID(id);
        save();
        return removed;
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
    public Task getTaskByID(int id) {
        Task task = super.getTaskByID(id);
        save();
        return task;
    }

    @Override
    public Epic getEpicByID(int id) {
        Epic epic = super.getEpicByID(id);
        save();
        return epic;
    }

    @Override
    public Subtask getSubtaskByID(int id) {
        Subtask subtask = super.getSubtaskByID(id);
        save();
        return subtask;
    }

    public static void main(String[] args) throws IOException {
        File tempFile = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);

        System.out.println("--- Создаем задачи ---");
        Task task1 = manager.addTask(new Task("Задача 1", "Описание 1"));
        Epic epic1 = manager.addEpic(new Epic("Эпик 1", "Описание Эпика 1"));
        Subtask subtask1 = manager.addSubtask(new Subtask("Подзадача 1.1", "Описание 1.1", epic1.getId()));

        System.out.println("--- Просматриваем задачи для истории ---");
        manager.getTaskByID(task1.getId());
        manager.getEpicByID(epic1.getId());

        System.out.println("Текущие задачи в менеджере:");
        System.out.println(manager.getTasks());
        System.out.println(manager.getEpics());
        System.out.println("История: " + manager.getHistory());

        System.out.println("\n--- Загружаем менеджер из файла ---");
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        System.out.println("Задачи в загруженном менеджере:");
        System.out.println(loadedManager.getTasks());
        System.out.println(loadedManager.getEpics());
        System.out.println("История в загруженном менеджере: " + loadedManager.getHistory());

        System.out.println("\n--- Проверка ---");
        System.out.println("Менеджеры идентичны? " + manager.getTasks().equals(loadedManager.getTasks()));
        System.out.println("История идентична? " + manager.getHistory().equals(loadedManager.getHistory()));

        tempFile.deleteOnExit();
    }
}