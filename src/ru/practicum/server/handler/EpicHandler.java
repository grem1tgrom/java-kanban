package ru.practicum.server.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.manager.TaskManager;
import ru.practicum.task.Epic;
import ru.practicum.task.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EpicHandler extends BaseHttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public EpicHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();

            switch (method) {
                case "GET":
                    if (query != null && query.contains("id=")) {
                        int id = Integer.parseInt(query.substring(query.indexOf("id=") + 3));
                        Epic epic = taskManager.getEpicByID(id);
                        sendText(exchange, gson.toJson(epic), 200);
                    } else if (path.matches("^/epics/\\d+/subtasks$")) {
                        int epicId = parseIdFromPath(exchange);
                        if (epicId != -1) {
                            List<Subtask> subtasks = taskManager.getEpicSubtasks(epicId);
                            sendText(exchange, gson.toJson(subtasks), 200);
                        } else {
                            sendNotFound(exchange, "Некорректный ID эпика в пути.");
                        }
                    } else if (path.equals("/epics")) {
                        List<Epic> epics = taskManager.getEpics();
                        sendText(exchange, gson.toJson(epics), 200);
                    } else {
                        sendNotFound(exchange, "Эндпоинт не найден");
                    }
                    break;
                case "POST":
                    InputStream is = exchange.getRequestBody();
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    Epic epic = gson.fromJson(body, Epic.class);
                    if (epic.getId() == 0) {
                        Epic createdEpic = taskManager.addEpic(epic);
                        sendText(exchange, gson.toJson(createdEpic), 201);
                    } else {
                        taskManager.updateEpic(epic);
                        sendText(exchange, gson.toJson(epic), 201);
                    }
                    break;
                case "DELETE":
                    if (query != null && query.contains("id=")) {
                        int id = Integer.parseInt(query.substring(query.indexOf("id=") + 3));
                        taskManager.deleteEpicByID(id);
                        sendText(exchange, "Эпик удален", 201);
                    } else {
                        taskManager.deleteEpics();
                        sendText(exchange, "Все эпики удалены", 201);
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