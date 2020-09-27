package connPoolFeign;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.TcpClient;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiConsumer;

import static reactor.netty.resources.ConnectionProvider.DEFAULT_POOL_ACQUIRE_TIMEOUT;
import static reactor.netty.resources.ConnectionProvider.DEFAULT_POOL_MAX_CONNECTIONS;
import static utils.HttpPoolTestUtils.simulateConcurrencyTasks;

public class RxNettyHttpClientPool {
    static String url1 = "http://localhost:8808/app/orders/commodityInPurchaseCustomers?channelId=1";
    static String url = "http://www.baidu.com";
    static ConnectionProvider pool = ConnectionProvider.builder("TEST")
            .maxConnections(100)
            .pendingAcquireMaxCount(500)
            .pendingAcquireTimeout(Duration.ofMillis(DEFAULT_POOL_ACQUIRE_TIMEOUT))
            .build(); // 设置Pool连接的空闲时间为1分钟

    static TcpClient tcpClient = TcpClient.create() // 使用默认的配置 ConnectionProvider
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
            .doOnConnected(connection ->
                    connection.addHandlerLast(new ReadTimeoutHandler(1000))
                            .addHandlerLast(new WriteTimeoutHandler(1000)));
    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNextInt()) {
            int count = scanner.nextInt();
            CountDownLatch countDownLatch = new CountDownLatch(count);
            CyclicBarrier cyclicBarrier = new CyclicBarrier(count);
            ExecutorService executorService = Executors.newFixedThreadPool(count);
            for (int i = 0; i < count; i++) {
                executorService.execute(() -> {
                    try {
                        cyclicBarrier.await();
                        long start = System.currentTimeMillis();
                        String res1 = HttpClient.create()
                                .get()
                                .uri(url)
                                .responseContent()
                                .aggregate()
                                .asString()
                                .block();
                        long stop = System.currentTimeMillis();
                        System.out.println(stop - start);
                        countDownLatch.countDown();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                });
            }
            countDownLatch.await();
        }
        scanner.close();

        simulateConcurrencyTasks(20, (startLatch, countDownLatch) -> {
            try {
                test02(startLatch, countDownLatch);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Thread.sleep(2000);
        System.out.println("======");
        simulateConcurrencyTasks(10, (startLatch, countDownLatch) -> {
            try {
                test02(startLatch, countDownLatch);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
    public static void test02(CountDownLatch startLatch, CountDownLatch countDownLatch) throws InterruptedException {
        startLatch.await();
        long start = System.currentTimeMillis();
        String res1 = HttpClient.create()
                .get()
                .uri(url)
                .responseContent()
                .aggregate()
                .asString()
                .block();
        long stop = System.currentTimeMillis();
        System.out.println(stop - start);
        countDownLatch.countDown();
    }


    public static void test01() throws InterruptedException {
        String res1 = HttpClient.from(tcpClient)
                .get()
                .uri(url)
                .responseContent()
                .aggregate()
                .asString()
                .block();
        System.out.println(res1);

        Thread.sleep(900);

        String res2 = HttpClient.from(tcpClient)
                .get()
                .uri(url)
                .responseContent()
                .aggregate()
                .asString()
                .block();
        System.out.println(res2);

        Thread.sleep(1500);

        String res3 = HttpClient.from(tcpClient)
                .get()
                .uri(url)
                .responseContent()
                .aggregate()
                .asString()
                .block();
        System.out.println(res3);
    }


}
