package ru.practicum.server;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import ru.practicum.enums.Status;
import ru.practicum.task.Epic;
import ru.practicum.task.Subtask;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpTaskServerSubtaskTest extends HttpTaskServerTest {

    public HttpTaskServerSubtaskTest() throws IOException {
    }

    @Test
    public void testAddSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Epic Description");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("Test Subtask", "Subtask Description", epic.getId());
        String subtaskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Subtask> subtasksFromManager = manager.getSubtasks();
        assertNotNull(subtasksFromManager);
        assertEquals(1, subtasksFromManager.size());
        assertEquals("Test Subtask", subtasksFromManager.get(0).getName());
    }

    @Test
    public void testGetSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Epic Description");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("Test Subtask", "Subtask Description", epic.getId());
        manager.addSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type subtaskListType = new TypeToken<List<Subtask>>() {}.getType();
        List<Subtask> subtasks = gson.fromJson(response.body(), subtaskListType);

        assertNotNull(subtasks);
        assertEquals(1, subtasks.size());
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Epic Description");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("Test Subtask", "Subtask Description", epic.getId());
        manager.addSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks?id=" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Subtask receivedSubtask = gson.fromJson(response.body(), Subtask.class);
        assertEquals(subtask.getId(), receivedSubtask.getId());
    }

    @Test
    public void testDeleteSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Epic Description");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("Test Subtask", "Subtask Description", epic.getId());
        manager.addSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks?id=" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        assertEquals(0, manager.getSubtasks().size());
    }
}