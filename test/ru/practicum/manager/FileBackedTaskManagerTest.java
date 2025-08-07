package ru.practicum.manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.enums.Status;
import ru.practicum.task.Epic;
import ru.practicum.task.Subtask;
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
        tempFile = File.createTempFile("test_tasks", ".csv");
        taskManager = new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    void tearDown() {
        tempFile.delete();
    }

    @Test
    void shouldCorrectlySaveAndLoadComplexState() {
        Task task = new Task("Задача 1", "Описание задачи 1");
        task.setStatus(Status.IN_PROGRESS);
        taskManager.addTask(task);

        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Подзадача 1.1", "Описание подзадачи 1.1", epic.getId());
        subtask.setStatus(Status.DONE);
        taskManager.addSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertNotNull(loadedManager.getTasks(), "Список задач не должен быть null.");
        assertEquals(1, loadedManager.getTasks().size(), "Количество задач не совпадает.");

        Task loadedTask = loadedManager.getTaskByID(task.getId());
        assertEquals(task.getName(), loadedTask.getName(), "Имя задачи не совпадает.");
        assertEquals(task.getDescription(), loadedTask.getDescription(), "Описание задачи не совпадает.");
        assertEquals(Status.IN_PROGRESS, loadedTask.getStatus(), "Статус задачи не совпадает.");

        assertNotNull(loadedManager.getEpics(), "Список эпиков не должен быть null.");
        assertEquals(1, loadedManager.getEpics().size(), "Количество эпиков не совпадает.");

        Epic loadedEpic = loadedManager.getEpicByID(epic.getId());
        assertEquals(epic.getName(), loadedEpic.getName(), "Имя эпика не совпадает.");

        List<Subtask> subtasksOfEpic = loadedManager.getEpicSubtasks(loadedEpic);
        assertNotNull(subtasksOfEpic, "Список подзадач у эпика не должен быть null.");
        assertEquals(1, subtasksOfEpic.size(), "Количество подзадач у эпика не совпадает.");

        Subtask loadedSubtask = subtasksOfEpic.get(0);
        assertEquals(subtask.getId(), loadedSubtask.getId(), "ID подзадачи не совпадает.");
        assertEquals(subtask.getName(), loadedSubtask.getName(), "Имя подзадачи не совпадает.");
        assertEquals(Status.DONE, loadedSubtask.getStatus(), "Статус подзадачи не совпадает.");
        assertEquals(epic.getId(), loadedSubtask.getEpicID(), "ID эпика у подзадачи не совпадает.");
    }

    @Test
    void shouldLoadFromEmptyFile() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loadedManager.getTasks().isEmpty(), "Задачи должны отсутствовать.");
        assertTrue(loadedManager.getEpics().isEmpty(), "Эпики должны отсутствовать.");
    }
}