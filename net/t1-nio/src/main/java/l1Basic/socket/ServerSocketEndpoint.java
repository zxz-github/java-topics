package l1Basic.socket;

import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerSocketEndpoint {
    protected volatile boolean running = false;
    protected volatile boolean paused = false;
    private volatile ServerSocket serverSocket;
    public Socket serverSocketAccept() throws IOException {
        return this.serverSocket.accept();
    }

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket();
            SocketAddress addr = new InetSocketAddress("127.0.0.1", 8080);
            serverSocket.bind(addr);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                InetAddress clientAddr = socket.getInetAddress();
                int port = socket.getPort();
                System.out.println("client ip & port: " + clientAddr.getHostAddress() + ":" + port);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Acceptor implements Runnable {
        protected volatile AcceptorState state = AcceptorState.NEW;
        private final ServerSocketEndpoint endpoint;
        public Acceptor(ServerSocketEndpoint endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public void run() {
            while (endpoint.isRunning()) {
                while (endpoint.isPaused() && endpoint.isRunning()) {
                    state = AcceptorState.PAUSED;
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {}
                }
                if (!endpoint.isRunning()) {
                    break;
                }

                state = AcceptorState.RUNNING;
                Socket socket = null;
                try {
                    socket = endpoint.serverSocketAccept();
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        }

        public enum AcceptorState {
            NEW, RUNNING, PAUSED, ENDED;
        }
    }

    public static class SocketHandler {
        public static final Map<String, Socket> clientConns = new ConcurrentHashMap<>();

    }

    public boolean isRunning() {
        return running;
    }
    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}
