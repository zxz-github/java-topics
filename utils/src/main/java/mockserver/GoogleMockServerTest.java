package mockserver;

import com.google.mockwebserver.Dispatcher;
import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;
import com.google.mockwebserver.RecordedRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GoogleMockServerTest {
    private static final Logger logger = LoggerFactory.getLogger(GoogleMockServerTest.class);
    static MockWebServer server;
    public static void main(String[] args) throws IOException {
        server = new MockWebServer();
        String content = "Hello world";
        MockResponse response = new MockResponse().setResponseCode(200).setHeader("Content-type", "text/plain")
                .setBody(content);
        server.enqueue(response);
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                String path = request.getPath();
                if ("/login".equals(path)) {
                    return new MockResponse()
                            .setResponseCode(200)
                            .setHeader("Content-type", "text/plain")
                            .setBody("login ok");
                }else {
                    return new MockResponse()
                            .setResponseCode(200)
                            .setHeader("Content-type", "text/plain")
                            .setBody("mock server");
                }

            }
        });
        server.play(8080);
    }
}
