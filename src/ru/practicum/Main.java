package ru.practicum;

import ru.practicum.manager.TaskManager;
import ru.practicum.manager.Managers;
import ru.practicum.task.Task;
import ru.practicum.task.Epic;
import ru.practicum.task.Subtask;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

        System.out.println("--- Создаем задачи ---");
        Task task1 = taskManager.addTask(new Task("Задача 1", "Описание 1"));
        Task task2 = taskManager.addTask(new Task("Задача 2", "Описание 2"));
        Epic epic1 = taskManager.addEpic(new Epic("Эпик 1", "Описание Эпика 1"));

        Subtask subtask11 = taskManager.addSubtask(new Subtask("Подзадача 1.1", "Описание 1.1", epic1.getId()));
        Subtask subtask12 = taskManager.addSubtask(new Subtask("Подзадача 1.2", "Описание 1.2", epic1.getId()));
        Subtask subtask13 = taskManager.addSubtask(new Subtask("Подзадача 1.3", "Описание 1.3", epic1.getId()));
        Epic epic2 = taskManager.addEpic(new Epic("Эпик 2", "Эпик без подзадач"));

        System.out.println("Создано: 2 задачи, 2 эпика (один с 3 подзадачами)");

        System.out.println("--- Наполняем историю просмотров ---");
        taskManager.getTaskByID(task1.getId());

        System.out.println("Просмотрена Задача 1. История: " + taskManager.getHistory());
        taskManager.getEpicByID(epic1.getId());

        System.out.println("Просмотрен Эпик 1. История: " + taskManager.getHistory());
        taskManager.getSubtaskByID(subtask11.getId());

        System.out.println("Просмотрена Подзадача 1.1. История: " + taskManager.getHistory());
        taskManager.getTaskByID(task2.getId());

        System.out.println("Просмотрена Задача 2. История: " + taskManager.getHistory());
        taskManager.getEpicByID(epic2.getId());

        System.out.println("Просмотрен Эпик 2. История: " + taskManager.getHistory());

        System.out.println("--- Проверяем отсутствие дублей ---");
        taskManager.getTaskByID(task1.getId());

        System.out.println("Повторно просмотрена Задача 1. История: " + taskManager.getHistory());
        taskManager.getEpicByID(epic1.getId());

        System.out.println("Повторно просмотрен Эпик 1. История: " + taskManager.getHistory());

        System.out.println("--- Удаляем задачу из истории ---");
        taskManager.deleteTaskByID(task2.getId());

        System.out.println("Удалена Задача 2. История: " + taskManager.getHistory());

        System.out.println("--- Удаляем эпик с подзадачами ---");
        taskManager.deleteEpicByID(epic1.getId());

        System.out.println("Удален Эпик 1 (с ним должны удалиться и его подзадачи). История: " + taskManager.getHistory());
    }
}