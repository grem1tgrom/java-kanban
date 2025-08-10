package ru.practicum.server;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
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

public class HttpTaskServerEpicTest extends HttpTaskServerTest {

    public HttpTaskServerEpicTest() throws IOException {
    }
    @Test
    public void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Epic Description");
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = manager.getEpics();
        assertNotNull(epicsFromManager);
        assertEquals(1, epicsFromManager.size());
        assertEquals("Test Epic", epicsFromManager.get(0).getName());
    }

    @Test
    public void testGetEpics() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Epic Description");
        manager.addEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type epicListType = new TypeToken<List<Epic>>() {}.getType();
        List<Epic> epics = gson.fromJson(response.body(), epicListType);

        assertNotNull(epics);
        assertEquals(1, epics.size());
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Epic Description");
        manager.addEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics?id=" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Epic receivedEpic = gson.fromJson(response.body(), Epic.class);
        assertEquals(epic.getId(), receivedEpic.getId());
    }

    @Test
    public void testGetEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Epic Description");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask 1", "Subtask Desc", epic.getId());
        manager.addSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId() + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Type subtaskListType = new TypeToken<List<Subtask>>() {}.getType();
        List<Subtask> subtasks = gson.fromJson(response.body(), subtaskListType);
        assertNotNull(subtasks);
        assertEquals(1, subtasks.size());
        assertEquals(subtask.getId(), subtasks.get(0).getId());
    }
}