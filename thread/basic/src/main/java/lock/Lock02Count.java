package lock;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 执行多次后count的值 <= 2000
 */
public class Lock02Count {
    private static int count = 0;
    private static Thread[] threads = new Thread[10000];
    private static CountDownLatch countDownLatch = new CountDownLatch(threads.length);
    private static ReentrantLock lock = new ReentrantLock();
    static {
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                countDownLatch.countDown();
                countIncreament(1);
            });
        }
    }
    public static void main(String[] args) throws InterruptedException {

        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
        countDownLatch.await(); /*所有线程结束*/
        System.out.println("count: " + count);
    }

    private static void countIncreament(int inc) {
        lock.lock();
        try {
            count = count + inc;
        }finally {
            lock.unlock();
        }

    }
}
