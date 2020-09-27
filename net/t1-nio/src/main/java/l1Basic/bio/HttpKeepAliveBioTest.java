package l1Basic.bio;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.locks.LockSupport;

import static sun.nio.ch.IOStatus.EOF;

public class HttpKeepAliveBioTest {
    public static void main(String[] args) throws IOException {
        testKeepAlive();
    }

    public static void main1(String[] args) throws IOException, InterruptedException {
        testKeepAlive();
        Thread.sleep(100);
        testKeepAlive();
        Thread.sleep(200);
        testKeepAlive();
        Thread.sleep(300);
        testKeepAlive();
    }

    public static void testKeepAlive() throws IOException {
        String req = "GET /app/orders/commodityInPurchaseCustomers?channelId=1&offset=99 HTTP/1.1";
        String host = "Host: localhost:8808";
        String header = "Host: localhost:8808\r\n" +
                "Connection: keep-alive\r\n" +
                "Cache-Control: max-age=0\r\n" +
                "Upgrade-Insecure-Requests: 1\r\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36\r\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9\r\n" +
                "Sec-Fetch-Site: none\r\n" +
                "Sec-Fetch-Mode: navigate\r\n" +
                "Sec-Fetch-User: 1\r\n" +
                "Sec-Fetch-Dest: document\r\n" +
                "Accept-Encoding: gzip, deflate, br\r\n" +
                "Accept-Language: zh-CN,zh;q=0.9\r\n";
        Socket socket = null;
        PrintWriter printWriter = null;
        try {
            socket = new Socket("localhost", 8808);
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            printWriter = new PrintWriter(os, true);
            printWriter.println(req);
            printWriter.println(host);
            printWriter.println();

            StringBuilder str = new StringBuilder();
            int c = 0;

            byte[] buf = new byte[128];
            int size = 0;
            while (is.read() > 0) {
                str.append(new String(buf, 0, size));
            }
            System.out.println(str.toString());
            str = new StringBuilder();
            System.out.println("==========================");
            printWriter.println(req);
            printWriter.println(host);
            printWriter.println();

            while ((size = is.read(buf,0,buf.length)) != -1) {
                str.append(new String(buf, 0, size));
            }
            System.out.println("+++++++++++++++++++++++++++++");
            is.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (Objects.nonNull(socket)) {
                socket.close();
            }
        }
    }

    public static void testKeepAlive2() throws IOException {
        String req = "GET /app/orders/commodityInPurchaseCustomers?channelId=1 HTTP/1.1\n";
        Socket socket = null;
        PrintWriter printWriter = null;
        BufferedReader bufferedReader = null;
        try {
            socket = new Socket("127.0.0.1", 8808);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printWriter = new PrintWriter(socket.getOutputStream(), true);
            printWriter.print(req);
            printWriter.flush();
            while (true) {
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String readLine = bufferedReader.readLine();
                System.out.println(readLine);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (Objects.nonNull(socket)) {
                socket.close();
            }
            if (Objects.nonNull(printWriter)) {
                printWriter.close();
            }
            if (Objects.nonNull(bufferedReader)) {
                bufferedReader.close();
            }
        }
    }

}
