package ru.practicum.task;

import org.junit.jupiter.api.Test;
import ru.practicum.enums.Status;
import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    public void tasksWithEqualIdShouldBeEqual() {
        Task task1 = new Task(10, "Купить хлеб", "В Дикси у дома", Status.NEW);
        Task task2 = new Task(10, "Купить молоко", "В Пятерочке", Status.DONE);
        assertEquals(task1, task2, "Ошибка! Экземпляры класса Task должны быть равны друг другу, если равен их id;");
    }
}