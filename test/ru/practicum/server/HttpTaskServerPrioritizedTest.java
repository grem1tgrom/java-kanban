package ru.practicum.server;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import ru.practicum.enums.Status;
import ru.practicum.task.Task;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpTaskServerPrioritizedTest extends HttpTaskServerTest {

    public HttpTaskServerPrioritizedTest() throws IOException {
    }
    @Test
    public void testGetPrioritizedTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Desc 1", Status.NEW,
                LocalDateTime.of(2025, 1, 1, 12, 0), Duration.ofHours(1));
        manager.addTask(task1);
        Task task2 = new Task("Task 2", "Desc 2", Status.NEW,
                LocalDateTime.of(2025, 1, 1, 10, 0), Duration.ofHours(1));
        manager.addTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type taskListType = new TypeToken<List<Task>>() {}.getType();
        List<Task> prioritized = gson.fromJson(response.body(), taskListType);

        assertNotNull(prioritized);
        assertEquals(2, prioritized.size());
        assertEquals(task2.getId(), prioritized.get(0).getId(), "Задачи должны быть отсортированы по времени начала");
        assertEquals(task1.getId(), prioritized.get(1).getId());
    }
}