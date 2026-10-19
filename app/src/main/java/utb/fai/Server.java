package utb.fai;

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

public class Server {
    private final int port;
    private final ExecutorService executorService;
    private final Set<Socket> activeSockets = ConcurrentHashMap.newKeySet();
    private volatile ServerSocket serverSocket;

    public Server(int port, int max_threads){
        this.port = port;
        this.max_threads = max_threads;
        this.executorService = Executors.newFixedThreadPool(max_threads);
    }

    public void start(){
        serverTerminalHandler();

        try(ServerSocket serverSocket = new ServerSocket(port)){
            this.serverSocket = serverSocket;
            System.out.println("Server bezi na portu: " + serverSocket.getLocalPort());

            while(!this.executorService.isShutdown()){
                Socket clientSocket = serverSocket.accept();

                if (activeSockets.size() >= max_threads) {
                    System.out.println("Server neprijima dalsi klienty.");
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                this.activeSockets.add(clientSocket);
                System.out.println("Pripojil se novy klinet: " + clientSocket.getInetAddress());

                this.executorService.execute(new ClientThread(clientSocket, this.activeSockets));

            } catch (IOException e) {

                if (this.serverSocket == null || this.serverSocket.isClosed()) {
                    System.out.println("Server uspesne ukoncen");
                } else {
                    System.err.println("Chyba na serveru " + e.getMessage());
                }
        } finally {
            this.close();
        }
    }

    public void close(){
        try{
            if(serverSocket != null && !serverSocket.isClosed()){
                serverSocket.close();
            }
        } catch(IOException e){
            e.printStackTrace();
        }

        for (Socket socket : activeSocket){
            try{
                socket.close();
            } catch (IOException ignored){}
        }

        this.executorService.shutdown();

        try{
            if(!this.executorService.awaitTermination(30,TimeUnit.SECONDS)){
                this.executorService.shutdown();
            }
        } catch (InterruptedException e) {
            this.executorService.shutdown();
            Thread.currentThread().interrupt();
        }
    }

    private void serverTerminalHandler(){
        Thread thread = new Thread(() -> {
            try(Scanner scanner = new Scanner(System.in)){
                while(scanner.hasNextLine()){
                    if(scanner.nextLine().trim().equalsIgnoreCase("quit")){
                        System.out.println("Ukoncuji server");
                        close();
                        break;
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
