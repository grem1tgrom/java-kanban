package ru.practicum.server.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.manager.TaskManager;
import ru.practicum.task.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public SubtaskHandler(TaskManager taskManager, Gson gson) {
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
                        List<Subtask> subtasks = taskManager.getSubtasks();
                        sendText(exchange, gson.toJson(subtasks), 200);
                    } else {
                        int id = Integer.parseInt(query.substring(query.indexOf("id=") + 3));
                        Subtask subtask = taskManager.getSubtaskByID(id);
                        sendText(exchange, gson.toJson(subtask), 200);
                    }
                    break;
                case "POST":
                    InputStream is = exchange.getRequestBody();
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    Subtask subtask = gson.fromJson(body, Subtask.class);

                    if (subtask.getId() == 0) {
                        Subtask createdSubtask = taskManager.addSubtask(subtask);
                        sendText(exchange, gson.toJson(createdSubtask), 201);
                    } else {
                        Subtask updatedSubtask = taskManager.updateSubtask(subtask);
                        sendText(exchange, gson.toJson(updatedSubtask), 201);
                    }
                    break;
                case "DELETE":
                    if (query != null) {
                        int id = Integer.parseInt(query.substring(query.indexOf("id=") + 3));
                        taskManager.deleteSubtaskByID(id);
                        sendText(exchange, "Подзадача удалена", 201);
                    } else {
                        taskManager.deleteSubtasks();
                        sendText(exchange, "Все подзадачи удалены", 201);
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