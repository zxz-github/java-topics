package atomic;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程安全的计数器：通过AtomicInteger
 * 执行多次后count的值 = 2000
 */
public class Atomic01Count {
    private static AtomicInteger count = new AtomicInteger();
    private static Thread[] threads = new Thread[10000];
    private static CountDownLatch countDownLatch = new CountDownLatch(threads.length);
    static {
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                    countDownLatch.countDown();
                    countIncreament(1);
            });
        }
    }
    public static void main(String[] args) throws IOException, InterruptedException {

        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
        countDownLatch.await(); /*所有线程结束*/
        System.out.println("count: " + count);
    }

    private static void countIncreament(int inc) {
        count.addAndGet(inc);
    }
}
