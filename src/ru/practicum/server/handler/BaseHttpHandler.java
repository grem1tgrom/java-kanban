package ru.practicum.server.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.exception.ManagerSaveException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.TaskValidationException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {

    protected void handleExceptions(HttpExchange exchange, Exception e) throws IOException {
        if (e instanceof NotFoundException) {
            sendNotFound(exchange, e.getMessage());
        } else if (e instanceof TaskValidationException) {
            sendHasOverlaps(exchange, e.getMessage());
        } else if (e instanceof ManagerSaveException) {
            sendInternalError(exchange, e.getMessage());
        } else {
            sendInternalError(exchange, "Внутренняя ошибка сервера.");
            e.printStackTrace();
        }
    }

    protected void sendText(HttpExchange h, String text, int statusCode) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(statusCode, resp.length);
        try (OutputStream os = h.getResponseBody()) {
            os.write(resp);
        }
        h.close();
    }

    protected void sendNotFound(HttpExchange h, String message) throws IOException {
        sendText(h, message, 404);
    }

    protected void sendHasOverlaps(HttpExchange h, String message) throws IOException {
        sendText(h, message, 406);
    }

    protected void sendInternalError(HttpExchange h, String message) throws IOException {
        sendText(h, message, 500);
    }

    protected int parseIdFromPath(HttpExchange exchange) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            return Integer.parseInt(pathParts[2]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return -1;
        }
    }
}