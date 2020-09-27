package connPool.httpClientPool02;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import utils.HttpPoolTestUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

@Slf4j
public class FeignHttpClientPool {
    private final static Timer connectionManagerTimer = new Timer("HttpClientConnectionManager.connectionManagerTimer", true);
    private final static HttpClientConnectionManager connectionManager =
            connectionManager(false,
                    10000,
                    10000,
                    60,
                    TimeUnit.SECONDS,
                    null);
    private static final CloseableHttpClient httpClient = HttpClientBuilder
            .create()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(
                    RequestConfig
                            .custom()
                            .setConnectTimeout(1000)
                            .setRedirectsEnabled(false)
                            .build())
            .build();

    static String url = "http://localhost:8808/app/orders/commodityInPurchaseCustomers?channelId=1&offset=-1&needVirtual=true";
    static String urlbaidu = "http://www.baidu.com";
    static String urldev= "http://192.168.42.22:8808/app/orders/commodityInPurchaseCustomers?channelId=1&offset=-1&needVirtual=true";
    static String urlNetty = "http://localhost:10086/hello";
    public static void main1(String[] args) throws IOException, InterruptedException {
        HttpPoolTestUtils.simulateConcurrencyTasks(20, (startLatch, countLatch) -> {
            try {
                test01(startLatch);
                countLatch.countDown();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });
    }
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
                        HttpGet httpGet = new HttpGet(url);
                        httpGet.setHeaders(new Header[]{
                                new BasicHeader("x-qqw-channel-no","KJLJC2J7I9"),
                                new BasicHeader("x-qqw-client-version","android-KJLJC2J7I9-3.1.1"),
                                new BasicHeader("x-qqw-temp","r0Mhe6yc4nlpWAgp"),
                                new BasicHeader("x-qqw-token-customer","96417"),
                                //new BasicHeader("Connection","close"),
                                new BasicHeader("Connection","keep-alive"),
                        });
                        cyclicBarrier.await();
                        long start = System.currentTimeMillis();
                        CloseableHttpResponse response = httpClient.execute(httpGet);
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

    public static void test01(CountDownLatch startLatch) throws IOException, InterruptedException {

        HttpGet httpGet = new HttpGet(urldev);
        httpGet.setHeaders(new Header[]{
                new BasicHeader("x-qqw-channel-no","KJLJC2J7I9"),
                new BasicHeader("x-qqw-client-version","android-KJLJC2J7I9-3.1.1"),
                new BasicHeader("x-qqw-temp","r0Mhe6yc4nlpWAgp"),
                new BasicHeader("x-qqw-token-customer","96417"),
                new BasicHeader("keep-alive","300")
        });
        startLatch.await();
        long start = System.currentTimeMillis();
        CloseableHttpResponse response = httpClient.execute(httpGet);
        //String res = IOUtils.toString(response.getEntity().getContent());
        long stop = System.currentTimeMillis();
        System.out.println("start: " + start + ", duration: " + (stop - start) + "ms, ");
    }


    public static HttpClientConnectionManager connectionManager(boolean disableSslValidation,
                                                                int maxTotalConnections, int maxConnectionsPerRoute, long timeToLive,
                                                                TimeUnit timeUnit, RegistryBuilder registryBuilder) {
        if (registryBuilder == null) {
            registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE);
        }
        if (disableSslValidation) {
            try {
                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null,
                        new TrustManager[] { new DisabledValidationTrustManager() },
                        new SecureRandom());
                registryBuilder.register("https", new SSLConnectionSocketFactory(
                        sslContext, NoopHostnameVerifier.INSTANCE));
            }
            catch (NoSuchAlgorithmException | KeyManagementException e) {
                log.warn("Error creating SSLContext", e);
            }
        }
        else {
            registryBuilder.register("https",
                    SSLConnectionSocketFactory.getSocketFactory());
        }
        final Registry<ConnectionSocketFactory> registry = registryBuilder.build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
                registry, null, null, null, timeToLive, timeUnit);
        connectionManager.setMaxTotal(maxTotalConnections);
        connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);
        connectionManagerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                connectionManager.closeExpiredConnections();
            }
        }, 1, 1); //ms
        return connectionManager;
    }

    static class DisabledValidationTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

    }
}
