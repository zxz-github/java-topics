package connPool;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ClientTest {
    public static void main(String[] args) throws IOException {
        RequestConfig config = RequestConfig.custom().setConnectTimeout(1000).setSocketTimeout(2000).build();
        AtomicLong index = new AtomicLong(1);
        Executors.newScheduledThreadPool(1000).scheduleAtFixedRate(() -> {
            HttpEntity entity2 = new StringEntity("", ContentType.APPLICATION_JSON);
            RequestBuilder.post("http://localhost:5001/soa/message/send").setConfig(config).addHeader("Content-type", "application/json").setCharset(StandardCharsets.UTF_8).setEntity(entity2);
            HttpUriRequest request = RequestBuilder
                    .get("http://localhost:5001/testClient?index=" + index.getAndIncrement())
                    .setConfig(config)
                    .build();
            TimeoutExctutorHttpClient.asyncRequest(request, HttpClientContext.create()).handle((response, throwable) -> {
                HttpEntity entity = response.getEntity();
                if (null != entity) {
                    try {
                        String s = IOUtils.toString(entity.getContent());
                        System.out.println(request.getURI().getQuery() + ":" + s);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            });
            System.out.println("thread ==" + Thread.currentThread().getName());
        }, 10, 1, TimeUnit.MILLISECONDS);
    }
}
