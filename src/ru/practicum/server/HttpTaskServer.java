package ru.practicum.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import ru.practicum.manager.Managers;
import ru.practicum.manager.TaskManager;
import ru.practicum.server.adapter.DurationAdapter;
import ru.practicum.server.adapter.LocalDateTimeAdapter;
import ru.practicum.server.handler.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final Gson gson;
    private final TaskManager taskManager;

    public HttpTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.gson = getGson();
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", new TaskHandler(taskManager, gson));
        server.createContext("/subtasks", new SubtaskHandler(taskManager, gson));
        server.createContext("/epics", new EpicHandler(taskManager, gson));
        server.createContext("/history", new HistoryHandler(taskManager, gson));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager, gson));
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    public void start() {
        System.out.println("HTTP-сервер запущен на порту " + PORT);
        server.start();
    }

    public void stop() {
        server.stop(0);
        System.out.println("HTTP-сервер остановлен на порту " + PORT);
    }

    public static void main(String[] args) {
        try {
            HttpTaskServer taskServer = new HttpTaskServer();
            taskServer.start();
        } catch (IOException e) {
            System.out.println("Ошибка при запуске сервера");
            e.printStackTrace();
        }
    }
}