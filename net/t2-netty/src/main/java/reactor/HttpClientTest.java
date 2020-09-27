package reactor;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.TcpClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiConsumer;

import static reactor.netty.resources.ConnectionProvider.DEFAULT_POOL_ACQUIRE_TIMEOUT;
import static reactor.netty.resources.ConnectionProvider.DEFAULT_POOL_MAX_CONNECTIONS;

public class HttpClientTest {
    static String url = "http://localhost:8808/app/orders/commodityInPurchaseCustomers?channelId=1";
    static ConnectionProvider pool = ConnectionProvider.builder("TEST")
            .maxConnections(1)
            .pendingAcquireMaxCount(500)
            .pendingAcquireTimeout(Duration.ofMillis(DEFAULT_POOL_ACQUIRE_TIMEOUT))
            .build(); // 设置Pool连接的空闲时间为1分钟

    static TcpClient tcpClient = TcpClient.create(pool) // 使用默认的配置 ConnectionProvider
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
            .doOnConnected(connection ->
                    connection.addHandlerLast(new ReadTimeoutHandler(10))
                            .addHandlerLast(new WriteTimeoutHandler(10)));
    public static void main(String[] args) throws InterruptedException {
        //test01();
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
        String res1 = HttpClient.from(tcpClient)
                .get()
                .uri(url)
                .responseContent()
                .aggregate()
                .asString()
                .block();
        System.out.println(res1);
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

    public static void simulateConcurrencyTasks(int count, BiConsumer<CountDownLatch, CountDownLatch> taskCommand) throws InterruptedException {
        int COUNT = 10;
        List<Thread> threadList = new ArrayList<>();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch countDownLatch = new CountDownLatch(COUNT);
        threadList.clear();
        for (int i = 0; i < COUNT; i++) {
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
