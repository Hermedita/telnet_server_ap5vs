package utb.fai;

import java.io.*;
import java.net.*;
import java.util.Set;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class ClientThread extends Thread implements Runnable{

    private Socket socket;

    protected Set<Socket> activeSocket;

    public ClientThread(Socket socket, Set<Socket> activeSocket) {
        this.socket = socket;
        this.activeSocket = activeSocket;
    }

    @Override
    public void run() {
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);

            String line;
            while((line = in.readLine()) != null){
                System.out.println("Zprava od klienta: " + line);
                out.println(line);
            }
        } catch (IOException e){
            System.err.println("Doslo k chybe v komunikaci s klientem "+ e.getMessage());
        } finally{
            activeSocket.remove(socket);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Klient (" + this.socket.getInetAddress() + ") se odpojil od serveru.");
        }
    }
}
