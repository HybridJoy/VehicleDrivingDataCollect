package com.hybrid.tripleldc.util.io;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Author: Joy
 * Created Time: 2022/3/16-20:40
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2022/3/16 )
 * <p>
 * Describe:
 */
public class AsyncTaskRunner {
    private static final String TAG = "AsyncTaskRunner";

    private ExecutorService executor;

    public static class TaskResultInfo {
        public boolean success;
        public long startTime;
        public long endTime;

        public TaskResultInfo(boolean success, long startTime, long endTime) {
            this.success = success;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public float timeConsuming() {
            return (float)(endTime - startTime) / 1000.f;
        }
    }

    public interface OnCompletedCallback<T> {
        void onComplete(@Nullable T result);
    }

    public AsyncTaskRunner() {
        executor = Executors.newSingleThreadExecutor();
    }

    public AsyncTaskRunner(int threadNum) {
        executor = Executors.newFixedThreadPool(threadNum);
    }

    public void executeRunnable(Runnable runnable) {
        executor.execute(runnable);
    }

    public <T> void executeCallable(@NonNull Callable<T> callable, @Nullable OnCompletedCallback<T> callback) {
        executor.execute(() -> {
            T result = null;
            try {
                result = callable.call();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (callback != null) {
                    callback.onComplete(result);
                }
            }
        });
    }

    private ReentrantLock lock = new ReentrantLock();

    public <T> void executeTasks(@NonNull final Set<Callable<T>> callables, @Nullable final OnCompletedCallback<List<T>> callback) {
        LogUtil.d(TAG, String.format(Locale.ENGLISH, "task size: %d", callables.size()));

        final int taskSize = callables.size();
        final List<T> results = new ArrayList<>();
        for (Callable<T> callable : callables) {
            executeCallable(callable, result -> {
                lock.lock();
                results.add(result);
                if (results.size() == taskSize) {
                    if (callback != null) {
                        callback.onComplete(results);
                    }
                }
                lock.unlock();
            });
        }
    }

    public void cancel() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}
