package l1Basic.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

public class SocketTest {
    public static void main(String[] args) {
        InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 8080);
        Socket socket = null;
        try {
            socket = new Socket(addr.getAddress(), addr.getPort());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            socket.sendUrgentData(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SocketAddress localSocketAddress = socket.getLocalSocketAddress();
        try {
            socket.setKeepAlive(true);
            socket.setSoTimeout(0);
            socket.setSoLinger(true, 0);
            socket.setSendBufferSize(1024);
            socket.setReceiveBufferSize(1024);
            socket.setTcpNoDelay(true);
            socket.setTrafficClass(200);
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }


}
