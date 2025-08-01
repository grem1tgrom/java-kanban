package ru.practicum.manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.task.Epic;
import ru.practicum.task.Task;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private File tempFile;
    private FileBackedTaskManager taskManager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("test-tasks", ".csv");
        taskManager = new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    void tearDown() {
        tempFile.delete();
    }

    @Test
    void shouldSaveAndLoadEmptyFile() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loadedManager.getTasks().isEmpty(), "Список задач должен быть пустым.");
        assertTrue(loadedManager.getEpics().isEmpty(), "Список эпиков должен быть пустым.");
        assertTrue(loadedManager.getHistory().isEmpty(), "История должна быть пустой.");
    }

    @Test
    void shouldSaveAndLoadMultipleTasks() {
        Task task1 = taskManager.addTask(new Task("Task 1", "Desc 1"));
        Epic epic1 = taskManager.addEpic(new Epic("Epic 1", "Epic Desc 1"));
        taskManager.addSubtask(new ru.practicum.task.Subtask("Sub 1", "Sub Desc 1", epic1.getId()));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loadedManager.getTasks().size(), "Должна быть одна задача.");
        assertEquals(1, loadedManager.getEpics().size(), "Должен быть один эпик.");
        assertEquals(1, loadedManager.getSubtasks().size(), "Должна быть одна подзадача.");
        assertEquals(task1.getName(), loadedManager.getTaskByID(task1.getId()).getName());
    }

    @Test
    void shouldSaveAndLoadHistory() {
        Task task1 = taskManager.addTask(new Task("Task 1", "Desc 1"));
        Epic epic1 = taskManager.addEpic(new Epic("Epic 1", "Epic Desc 1"));

        taskManager.getTaskByID(task1.getId());
        taskManager.getEpicByID(epic1.getId());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        List<Task> history = loadedManager.getHistory();

        assertEquals(2, history.size(), "История должна содержать два элемента.");
        assertEquals(task1.getId(), history.get(0).getId());
        assertEquals(epic1.getId(), history.get(1).getId());
    }
}