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