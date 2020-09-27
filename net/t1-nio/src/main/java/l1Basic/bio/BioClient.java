package l1Basic.bio;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.Socket;

public class BioClient {
    public static void main(String[] args) throws IOException {
        int port = 8808;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (Exception e) {

            }
        }
        Socket socket = null;
        BufferedReader bufferedReader = null;
        PrintWriter printWriter = null;
        try {
            socket = new Socket("127.0.0.1", port);
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            os.write("GET / HTTP/1.1 \n".getBytes());
            System.out.println(IOUtils.toString(is));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (printWriter != null) {
                printWriter.close();
            }
        }

    }
}
