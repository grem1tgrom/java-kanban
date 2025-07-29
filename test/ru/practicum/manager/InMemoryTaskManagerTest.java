package ru.practicum.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.task.Task;
import ru.practicum.task.Subtask;
import ru.practicum.task.Epic;


import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private TaskManager taskManager;

    @BeforeEach
    public void beforeEach() {
        taskManager = Managers.getDefault();
    }

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
}