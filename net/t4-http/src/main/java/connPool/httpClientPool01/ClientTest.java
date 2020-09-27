package connPool.httpClientPool01;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ClientTest {
    public static void main(String[] args) {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(3000) //连接建立的时间
                .setSocketTimeout(6000) //数据读取延时
                .setConnectionRequestTimeout(1000) //从连接池获取连接的最长时间
                .build();
        AtomicLong index = new AtomicLong(1);
        ScheduledFuture<?> scheduledFuture = Executors.newScheduledThreadPool(1000).scheduleAtFixedRate(() -> {
            HttpUriRequest request = RequestBuilder
                    .get("http://localhost:5001/testClient?index=" + index.getAndIncrement())
                    .setConfig(config)
                    .build();
            TimeoutExctutorHttpClient.asyncRequest(request, (req, resp, ctx, throwable) -> {
                if (null != throwable) {
                    throwable.printStackTrace();
                }
                HttpEntity entity = resp.getEntity();
                if (null != entity) {
                    try {
                        String s = IOUtils.toString(entity.getContent());
                        System.out.println(request.getURI().getQuery() + ":" + s);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            System.out.println("thread ==" + Thread.currentThread().getName());
        }, 10, 1, TimeUnit.MILLISECONDS);
        System.out.println("============main============");

    }
    public static void main1(String[] args) throws IOException {
        RequestConfig config = RequestConfig.custom().setConnectTimeout(1000).setSocketTimeout(2000).build();
        AtomicLong index = new AtomicLong(1);
        Executors.newScheduledThreadPool(1000).scheduleAtFixedRate(() -> {
            HttpEntity entity2 = new StringEntity("", ContentType.APPLICATION_JSON);
            RequestBuilder request2 = RequestBuilder
                    .post("http://localhost:5001/soa/message/send")
                    .setConfig(config)
                    .addHeader("Content-type", "application/json")
                    .setCharset(StandardCharsets.UTF_8).setEntity(entity2);
            HttpUriRequest request = RequestBuilder
                    .get("http://localhost:5001/testClient?index=" + index.getAndIncrement())
                    .setConfig(config)
                    .build();
            TimeoutExctutorHttpClient.asyncRequest(request, (httpUriRequest, response, httpClientContext, throwable) -> {
                try {
                    System.out.println(IOUtils.toString(response.getEntity().getContent()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            System.out.println("thread ==" + Thread.currentThread().getName());
        }, 10, 1, TimeUnit.MILLISECONDS);
    }

    private static void handleResponse(HttpUriRequest request, HttpEntity entity) {
        if (null != entity) {
            try {
                String s = IOUtils.toString(entity.getContent());
                System.out.println(request.getURI().getQuery() + ":" + s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
