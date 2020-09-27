package lock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Condition 必须在获取锁 reentrantLock.lock() 后执行
 */
public class Lock05Condition {
    private static ReentrantLock reentrantLock = new ReentrantLock();
    private static Condition condition = reentrantLock.newCondition();
    public static void main(String[] args) {
        new Thread(Lock05Condition::conditonWait).start();
        new Thread(Lock05Condition::conditionsignalAll).start();
    }

    private static void conditonWait() {
        try {
            reentrantLock.lock();
            System.out.println("wait signal"); // 1
            condition.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("get signal"); // 4
        reentrantLock.unlock();
    }

    private static void conditionsignalAll() {
        reentrantLock.lock();
        System.out.println("get lock"); // 2
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        condition.signalAll();
        System.out.println("send signal ~ "); // 3
        reentrantLock.unlock();
    }
}
