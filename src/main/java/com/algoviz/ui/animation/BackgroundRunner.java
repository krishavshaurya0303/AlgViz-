package com.algoviz.ui.animation;

import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Runs potentially expensive trace generation off the JavaFX thread.
 * <p>
 * Each submission gets a ticket. Only the result of the most recent ticket is
 * delivered (on the FX thread); results of superseded requests — e.g. the
 * user pressed Reset or picked another algorithm meanwhile — are discarded.
 * The work itself never touches JavaFX nodes.
 */
public final class BackgroundRunner {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "algoviz-trace-builder");
        t.setDaemon(true);
        return t;
    });

    private long ticket;

    public <T> void submit(Supplier<T> work, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        long myTicket = ++ticket;
        Task<T> task = new Task<>() {
            @Override
            protected T call() {
                return work.get();
            }
        };
        task.setOnSucceeded(e -> {
            if (myTicket == ticket) {
                onSuccess.accept(task.getValue());
            }
        });
        task.setOnFailed(e -> {
            if (myTicket == ticket) {
                onError.accept(task.getException());
            }
        });
        EXECUTOR.submit(task);
    }

    /** Invalidates any pending request. Must be called on the FX thread. */
    public void cancelPending() {
        ticket++;
    }
}
