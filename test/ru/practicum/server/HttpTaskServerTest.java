package ru.practicum.server;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import ru.practicum.manager.InMemoryTaskManager;
import ru.practicum.manager.TaskManager;

import java.io.IOException;

public class HttpTaskServerTest {

    protected TaskManager manager;
    protected HttpTaskServer taskServer;
    protected Gson gson;

    @BeforeEach
    public void setUp() {
        try {
            manager = new InMemoryTaskManager();
            taskServer = new HttpTaskServer(manager);
            gson = HttpTaskServer.getGson();
            taskServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    public void tearDown() {
        taskServer.stop();
    }
}