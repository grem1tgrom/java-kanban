package ru.practicum.task;

import org.junit.jupiter.api.Test;
import ru.practicum.enums.Status;
import ru.practicum.task.Epic;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    public void epicsWithEqualIdShouldBeEqual() {
        Epic epic1 = new Epic(10, "Сделать ремонт", "Уложиться в 2 миллиона", Status.NEW);
        Epic epic2 = new Epic(10, "Подготовиться к собеседованию", "1 июля в 11:00",
                Status.IN_PROGRESS);
        assertEquals(epic1, epic2, "Ошибка! Наследники класса Task должны быть равны друг другу, если равен их id;");
    }
}