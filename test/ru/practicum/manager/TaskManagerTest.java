package ru.practicum.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.enums.Status;
import ru.practicum.task.Epic;
import ru.practicum.task.Subtask;
import ru.practicum.task.Task;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;

    @BeforeEach
    abstract void setUp();

    @Test
    void addNewTask() {
        final Task task = taskManager.addTask(new Task("Test addNewTask", "Test addNewTask description"));
        final Task savedTask = taskManager.getTaskByID(task.getId());
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");
    }

    @Test
    public void deleteTaskByIdShouldRemoveFromHistory() {
        Task task = taskManager.addTask(new Task("Task", "Description"));
        taskManager.getTaskByID(task.getId());
        assertEquals(1, taskManager.getHistory().size(), "История должна содержать 1 элемент.");

        taskManager.deleteTaskByID(task.getId());
        assertTrue(taskManager.getHistory().isEmpty(), "История должна быть пуста после удаления задачи.");
    }

    @Test
    public void deleteEpicByIdShouldRemoveEpicAndSubtasksFromHistory() {
        Epic epic = taskManager.addEpic(new Epic("Epic", "Epic desc"));
        Subtask sub1 = taskManager.addSubtask(new Subtask("Sub1", "Sub1 desc", epic.getId()));
        Subtask sub2 = taskManager.addSubtask(new Subtask("Sub2", "Sub2 desc", epic.getId()));

        taskManager.getEpicByID(epic.getId());
        taskManager.getSubtaskByID(sub1.getId());
        taskManager.getSubtaskByID(sub2.getId());

        assertEquals(3, taskManager.getHistory().size(), "История должна содержать 3 элемента.");

        taskManager.deleteEpicByID(epic.getId());
        assertTrue(taskManager.getHistory().isEmpty(), "История должна быть пуста после удаления эпика.");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Список подзадач должен быть пуст.");
    }

    @Test
    void epicStatusShouldBeNewWithAllNewSubtasks() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.addEpic(epic);
        Subtask sub1 = new Subtask("Подзадача 1", "...", epic.getId());
        sub1.setStatus(Status.NEW);
        taskManager.addSubtask(sub1);

        assertEquals(Status.NEW, taskManager.getEpicByID(epic.getId()).getStatus());
    }

    @Test
    void epicStatusShouldBeDoneWithAllDoneSubtasks() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.addEpic(epic);
        Subtask sub1 = new Subtask("Подзадача 1", "...", epic.getId());
        sub1.setStatus(Status.DONE);
        taskManager.addSubtask(sub1);
        Subtask sub2 = new Subtask("Подзадача 2", "...", epic.getId());
        sub2.setStatus(Status.DONE);
        taskManager.addSubtask(sub2);

        assertEquals(Status.DONE, taskManager.getEpicByID(epic.getId()).getStatus());
    }

    @Test
    void epicStatusShouldBeInProgressWithMixedSubtasks() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.addEpic(epic);
        Subtask sub1 = new Subtask("Подзадача 1", "...", epic.getId());
        sub1.setStatus(Status.NEW);
        taskManager.addSubtask(sub1);
        Subtask sub2 = new Subtask("Подзадача 2", "...", epic.getId());
        sub2.setStatus(Status.DONE);
        taskManager.addSubtask(sub2);

        assertEquals(Status.IN_PROGRESS, taskManager.getEpicByID(epic.getId()).getStatus());
    }

    @Test
    void shouldNotAddTaskIfOverlapping() {
        Task task1 = new Task("Задача 1", "Описание 1");
        task1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setDuration(Duration.ofHours(1));
        taskManager.addTask(task1);

        Task task2 = new Task("Задача 2", "Описание 2");
        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 30));
        task2.setDuration(Duration.ofHours(1));

        assertThrows(IllegalArgumentException.class, () -> taskManager.addTask(task2));
    }
}