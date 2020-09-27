package connPool.httpClientPool01;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.*;

public class TimeoutExctutorHttpClient {
    private static final Logger logger = LoggerFactory.getLogger(TimeoutExctutorHttpClient.class);
    private static TimeoutExctutorHttpClient instance;
    private static final ConcurrentHashMap<String, CloseableHttpClient> clientCache = new ConcurrentHashMap<>();
    protected HttpClientConnectionManager connectionManager;
    private static ThreadPoolExecutor executor = createExcutorInternal();

    private TimeoutExctutorHttpClient(Properties config) {
        this.connectionManager = createHttpClientConnectionManagerInternal();
        //定时清理线程
        IdleConnectionReaper.registerConnectionManager(this.connectionManager);
    }

    private static ThreadPoolExecutor createExcutorInternal() {
        int processors = Runtime.getRuntime().availableProcessors();
        executor = new ThreadPoolExecutor(
                processors * 5,
                processors * 10,
                60L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(processors),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    private HttpClientConnectionManager createHttpClientConnectionManagerInternal() {
        ConnectionSocketFactory plainSocketFactory = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder
                .<ConnectionSocketFactory>create()
                .register("http", plainSocketFactory)
                .register("https", sslSocketFactory)
                .build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
                registry);
        connectionManager.setDefaultMaxPerRoute(10);
        connectionManager.setMaxTotal(5);
        connectionManager.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(5).setTcpNoDelay(true).build());
        return connectionManager;
    }

    public synchronized static CloseableHttpClient getClient(final String hostName) {
        RequestConfig config = RequestConfig.DEFAULT;
        if (null == instance) {
            instance = new TimeoutExctutorHttpClient(new Properties());
        }
        return instance.getCloseableHttpClientInternal(hostName, config);
    }

    private CloseableHttpClient getCloseableHttpClientInternal(String hostName, RequestConfig config) {
        CloseableHttpClient client = clientCache.get(hostName);
        if (null == client) {
            client = HttpClients.custom().setConnectionManager(this.connectionManager).build();
            clientCache.put(hostName, client);
        }
        return clientCache.get(hostName);
    }

    public synchronized void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    logger.warn("TimeoutExctutorHttpClient shutdown fail");
                }
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        this.connectionManager.shutdown();
        logger.info("TimeoutExctutorHttpClient shutdown success");
    }

    @Override
    protected void finalize() throws Throwable {
        this.shutdown();
        super.finalize();
    }

    public static Future<CloseableHttpResponse> syncRequest(final HttpUriRequest request, final HttpClientContext context) {
        return executor.submit(new HttpRequestTask(request, context));
    }

    public static void asyncRequest(final HttpUriRequest request,
                                    HandleRequestFunction<? super HttpUriRequest, ? super CloseableHttpResponse, ? super HttpClientContext, Throwable> handler) {
        HttpClientContext ctx = HttpClientContext.create();
        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return getClient(request.getURI().getHost()).execute(request, ctx);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }, executor)
                .handle((response, throwable) -> {
                    handler.apply(request, response, ctx, throwable);
                    return response;
                });
    }

    @FunctionalInterface
    public interface HandleRequestFunction<T1, T2, T3, T4> {
        void apply(T1 t1, T2 t2, T3 t3, T4 t4);
    }


    static class HttpRequestTask implements Callable<CloseableHttpResponse> {
        private HttpUriRequest httpRequest;
        private HttpClientContext httpContext;
        public CloseableHttpResponse response;

        public HttpRequestTask(HttpUriRequest httpRequest, HttpClientContext httpContext) {
            this.httpRequest = httpRequest;
            this.httpContext = httpContext;
        }

        @Override
        public CloseableHttpResponse call() throws Exception {
            this.response = getClient(httpRequest.getURI().getHost()).execute(httpRequest, httpContext);
            return this.response;
        }
    }
}
