package ru.practicum.server;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import ru.practicum.task.Task;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpTaskServerHistoryTest extends HttpTaskServerTest {

    public HttpTaskServerHistoryTest() throws IOException {
    }

    @Test
    public void testGetHistory() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Desc 1");
        manager.addTask(task1);
        manager.getTaskByID(task1.getId());

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type taskListType = new TypeToken<List<Task>>() {}.getType();
        List<Task> history = gson.fromJson(response.body(), taskListType);

        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals(task1.getId(), history.get(0).getId());
    }
}