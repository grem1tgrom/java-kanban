package ru.practicum.server.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.manager.TaskManager;
import ru.practicum.task.Task;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TaskHandler extends BaseHttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public TaskHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();

            switch (method) {
                case "GET":
                    if (query == null) {
                        List<Task> tasks = taskManager.getTasks();
                        String response = gson.toJson(tasks);
                        sendText(exchange, response, 200);
                    } else {
                        int id = Integer.parseInt(query.substring(query.indexOf("id=") + 3));
                        Task task = taskManager.getTaskByID(id);
                        String response = gson.toJson(task);
                        sendText(exchange, response, 200);
                    }
                    break;
                case "POST":
                    InputStream is = exchange.getRequestBody();
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    Task task = gson.fromJson(body, Task.class);

                    if (task.getId() == 0) {
                        Task createdTask = taskManager.addTask(task);
                        sendText(exchange, gson.toJson(createdTask), 201);
                    } else {
                        Task updatedTask = taskManager.updateTask(task);
                        sendText(exchange, gson.toJson(updatedTask), 201);
                    }
                    break;
                case "DELETE":
                    if (query != null) {
                        int id = Integer.parseInt(query.substring(query.indexOf("id=") + 3));
                        taskManager.deleteTaskByID(id);
                        sendText(exchange, "Задача удалена", 201);
                    } else {
                        taskManager.deleteTasks();
                        sendText(exchange, "Все задачи удалены", 201);
                    }
                    break;
                default:
                    sendText(exchange, "Неверный метод", 405);
            }
        } catch (Exception e) {
            handleExceptions(exchange, e);
        }
    }
}