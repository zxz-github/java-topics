package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

public class HttpPoolTestUtils {
    public static void simulateConcurrencyTasks(int count, BiConsumer<CountDownLatch, CountDownLatch> taskCommand) throws InterruptedException {
        List<Thread> threadList = new ArrayList<>();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch countDownLatch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            threadList.add(new Thread(() -> {
                taskCommand.accept(startLatch, countDownLatch);
            }, "t"+i));
        }
        for (Thread thread : threadList) {
            thread.start();
        }
        startLatch.countDown();
        countDownLatch.await();
        System.out.println("===end===");
    }
}
