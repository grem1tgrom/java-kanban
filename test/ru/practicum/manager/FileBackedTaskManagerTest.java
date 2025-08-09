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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private File tempFile;

    @Override
    @BeforeEach
    void setUp() {
        try {
            tempFile = File.createTempFile("test_tasks", ".csv");
            tempFile.delete();
            taskManager = new FileBackedTaskManager(tempFile);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать временный файл для тестов", e);
        }
    }

    @AfterEach
    void tearDown() {
        if (tempFile != null) {
            tempFile.delete();
        }
    }

    @Test
    void shouldCorrectlySaveAndLoadComplexState() {
        Task task = new Task("Задача 1", "Описание задачи 1");
        task.setStatus(Status.IN_PROGRESS);
        task.setStartTime(LocalDateTime.of(2025, 8, 10, 10, 0));
        task.setDuration(Duration.ofMinutes(90));
        taskManager.addTask(task);

        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Подзадача 1.1", "Описание подзадачи 1.1", epic.getId());
        subtask.setStatus(Status.DONE);
        subtask.setStartTime(LocalDateTime.of(2025, 8, 10, 12, 0));
        subtask.setDuration(Duration.ofMinutes(60));
        taskManager.addSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertNotNull(loadedManager.getTasks(), "Список задач не должен быть null.");
        assertEquals(1, loadedManager.getTasks().size(), "Количество задач не совпадает.");

        Task loadedTask = loadedManager.getTaskByID(task.getId());
        assertEquals(task.getName(), loadedTask.getName(), "Имя задачи не совпадает.");
        assertEquals(task.getDescription(), loadedTask.getDescription(), "Описание задачи не совпадает.");
        assertEquals(Status.IN_PROGRESS, loadedTask.getStatus(), "Статус задачи не совпадает.");
        assertEquals(task.getStartTime(), loadedTask.getStartTime(), "Время начала задачи не совпадает.");
        assertEquals(task.getDuration(), loadedTask.getDuration(), "Продолжительность задачи не совпадает.");
        assertEquals(task.getEndTime(), loadedTask.getEndTime(), "Время окончания задачи не совпадает.");

        assertNotNull(loadedManager.getEpics(), "Список эпиков не должен быть null.");
        assertEquals(1, loadedManager.getEpics().size(), "Количество эпиков не совпадает.");

        Epic loadedEpic = loadedManager.getEpicByID(epic.getId());
        assertEquals(epic.getName(), loadedEpic.getName(), "Имя эпика не совпадает.");
        assertEquals(subtask.getEndTime(), loadedEpic.getEndTime(), "Время окончания эпика не совпадает.");

        List<Subtask> subtasksOfEpic = loadedManager.getEpicSubtasks(loadedEpic.getId());
        assertNotNull(subtasksOfEpic, "Список подзадач у эпика не должен быть null.");
        assertEquals(1, subtasksOfEpic.size(), "Количество подзадач у эпика не совпадает.");

        List<Task> prioritized = loadedManager.getPrioritizedTasks();
        assertNotNull(prioritized, "Список приоритетных задач не должен быть null.");
        assertEquals(2, prioritized.size(), "Количество задач в списке приоритетов не совпадает.");
        assertEquals(task.getId(), prioritized.get(0).getId(), "Первой в списке приоритетов должна быть task.");
    }

    @Test
    void shouldLoadFromEmptyFile() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loadedManager.getTasks().isEmpty(), "Задачи должны отсутствовать.");
        assertTrue(loadedManager.getEpics().isEmpty(), "Эпики должны отсутствовать.");
    }
}