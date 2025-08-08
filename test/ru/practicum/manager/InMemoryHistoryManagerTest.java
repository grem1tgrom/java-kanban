package ru.practicum.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.task.Task;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    public void beforeEach() {
        historyManager = Managers.getDefaultHistory();
        task1 = new Task(1, "Task 1", "Desc 1", ru.practicum.enums.Status.NEW);
        task2 = new Task(2, "Task 2", "Desc 2", ru.practicum.enums.Status.NEW);
        task3 = new Task(3, "Task 3", "Desc 3", ru.practicum.enums.Status.NEW);
    }

    @Test
    public void addShouldKeepTasksWithoutDuplicates() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать 2 элемента.");
        assertEquals(task2, history.get(0), "Первым элементом должен быть task2.");
        assertEquals(task1, history.get(1), "Вторым элементом должен быть task1 (последний добавленный).");
    }

    @Test
    public void removeShouldDeleteFromBeginning() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(task1.getId());
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertFalse(history.contains(task1));
    }

    @Test
    public void removeShouldDeleteFromMiddle() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(task2.getId());
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertFalse(history.contains(task2));
        assertEquals(task1, history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    public void removeShouldDeleteFromEnd() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(task3.getId());
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertFalse(history.contains(task3));
    }

    @Test
    public void getHistoryShouldReturnEmptyListIfNoTasksAdded() {
        assertTrue(historyManager.getHistory().isEmpty());
    }
}