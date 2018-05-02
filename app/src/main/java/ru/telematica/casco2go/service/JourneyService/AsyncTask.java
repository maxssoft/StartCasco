package ru.telematica.casco2go.service.JourneyService;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Прошлый век, но пока пусть живёт. Сейчас на этом работает {@link TcpClientAsync}
 */
class AsyncTask {
    Executor mTaskExecutor;

    AsyncTask() {
        mTaskExecutor = Executors.newFixedThreadPool(10);
    }
}
