package ru.practicum.manager;

import org.junit.jupiter.api.BeforeEach;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    @BeforeEach
    public void setUp() {
        taskManager = new InMemoryTaskManager();
    }
}