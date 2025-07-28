package ru.practicum.task;

import org.junit.jupiter.api.Test;
import ru.practicum.enums.Status;
import ru.practicum.task.Subtask;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    @Test
    public void subtasksWithEqualIdShouldBeEqual() {
        Subtask subtask1 = new Subtask(10, "Купить хлеб", "В Дикси у дома", Status.NEW, 5);
        Subtask subtask2 = new Subtask(10, "Купить молоко", "В Пятерочке", Status.DONE, 5);
        assertEquals(subtask1, subtask2, "Ошибка! Наследники класса Task должны быть равны друг другу, если равен их id;");
    }
}