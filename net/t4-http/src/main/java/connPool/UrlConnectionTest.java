package connPool;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.concurrent.*;

public class UrlConnectionTest {
    static ExecutorService executorService = Executors.newFixedThreadPool(500);
    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNextInt()) {
            int count = scanner.nextInt();
            CountDownLatch countDownLatch = new CountDownLatch(count);
            CyclicBarrier cyclicBarrier = new CyclicBarrier(count);

            for (int i = 0; i < count; i++) {
                executorService.execute(() -> {
                    try {
                        cyclicBarrier.await();
                        long start = System.currentTimeMillis();
                        test01();
                        long stop = System.currentTimeMillis();
                        System.out.println("start: " + start + ", duration: " + (stop - start) + "ms, ");
                        countDownLatch.countDown();
                    } catch (InterruptedException | BrokenBarrierException | IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            countDownLatch.await();
        }
        scanner.close();
        executorService.shutdown();
    }
    public static void test01() throws IOException {
        URL url = new URL("http://www.baidu.com");
        URLConnection conn = url.openConnection();
        if (conn instanceof HttpURLConnection) {
            int responseCode = ((HttpURLConnection) conn).getResponseCode();
            System.out.println(responseCode);
        }
    }
}
