package lock;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class Lock06ObejctWait {
    private final Object lock = new Object();
    public AtomicInteger limit = new AtomicInteger(3);
    public volatile boolean isDone = false;

    public static void main(String[] args) throws InterruptedException {
        Semaphore maxConcurrency = new Semaphore(0, false);
        long start = System.currentTimeMillis();
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        scheduledThreadPoolExecutor.scheduleAtFixedRate(() -> {
            maxConcurrency.release(5);
        }, 1, 1000, TimeUnit.MILLISECONDS);

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        CountDownLatch countDownLatch = new CountDownLatch(20);
        Lock06ObejctWait lock06ObejctWait = new Lock06ObejctWait();
        for (int i = 0; i < 20; i++) {
            executorService.execute(() -> {
                try {
                    maxConcurrency.acquire();
                    lock06ObejctWait.waitDuration(Duration.of(10, ChronoUnit.SECONDS));
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    lock06ObejctWait.unlock();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        executorService.shutdownNow();
        scheduledThreadPoolExecutor.shutdownNow();
        System.out.println("======" + (System.currentTimeMillis() - start));
    }

    public static void test1() {
        ArrayList<Lock06ObejctWait> tasks = new ArrayList<>();
        Lock06ObejctWait lock06ObejctWait = new Lock06ObejctWait();

        for (int i = 0; i < 12; i++) {
            new Thread(() -> {
                lock06ObejctWait.waitDuration(Duration.of(10, ChronoUnit.SECONDS));
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lock06ObejctWait.unlock();
            }, "thread-" + i).start();
        }
//        Executors.newScheduledThreadPool(1)
//                .scheduleAtFixedRate(() -> {
//                    lock06ObejctWait.unlock();
//                }, 1, 10, TimeUnit.MILLISECONDS);
    }

    public Optional<Object> waitDuration(Duration duration) {
        final Instant deadline = Instant.now().plus(duration);
        synchronized (lock) {
            while (true) {
                long timeout = Duration.between(Instant.now(), deadline).toMillis();
                if (timeout < 0) {
                    return Optional.empty();
                }
                if (limit.get() > 0) {
                    int currentLimit = limit.decrementAndGet();
                    System.out.println(Thread.currentThread().getName() + ", currrent limit:" + currentLimit + ", timout:" + timeout);
                    isDone = true;
                    return Optional.of(new Object());
                }
                try {
                    System.out.println(Thread.currentThread().getName() + " wait");
                    lock.wait(timeout);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return Optional.empty();
                }
            }
        }
    }

    public void unlock() {
        synchronized (lock) {
            System.out.println(Thread.currentThread().getName() + " unlock");
            limit.incrementAndGet();
            lock.notifyAll();
        }
    }
}
