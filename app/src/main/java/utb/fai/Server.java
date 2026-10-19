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
    private ServerSocket serverSocket;

    public Server(int port){
        this.port = port;
        this.executorService = Executors.newFixedThreadPool(2);
    }

    public void start(){
        serverTerminalHandler();

        try(ServerSocket serverSocket = new ServerSocket(port)){
            this.serverSocket = serverSocket;
            System.out.println("Server bezi na portu: " + serverSocket.getLocalPort());

            while(!this.executorService.isShutdown()){
                Socket clientSocket = serverSocket.accept();
                this.activeSockets.add(clientSocket);
                System.out.println("Pripojil se novy klinet: " + clientSocket.getInetAddress());

                try{
                    this.executorService.execute(new ClientThread(clientSocket, this.activeSockets));
                } catch (RejectedExecutionException e){
                    System.out.println("Server neprijima dalsi klienty.");
                    e.printStackTrace();

                    this.activeSockets.remove(clientSocket);
                    try{
                        clientSocket.close();
                    } catch(IOException ex){
                        ex.printStackTrace();
                    }
                }
            }
        } catch (IOException exx){
            if(this.serverSocket == null || this.serverSocket.isClosed()){
                System.out.println("Server uspesne ukoncen");
            } else {
                System.out.println("Chyba na serveru " + exx.getMessage());
            }
        } finally{
            this.close();
        }
    }

    public void close(){
        this.executorService.shutdown();
        try{
            if(!this.executorService.awaitTermination(60,TimeUnit.SECONDS)){
                this.executorService.shutdown();
                if(!this.executorService.awaitTermination(60,TimeUnit.SECONDS))
                    System.err.println("Pool did not translate");
            }
        } catch (InterruptedException e){
            this.executorService.shutdown();
            Thread.currentThread().interrupt();
        }
    }

    private void serverTerminalHandler(){
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while(scanner.hasNextLine()){
                String line = scanner.nextLine().trim().toLowerCase();
                if(line.equals("quit")){
                    System.out.println("Ukoncuji server");
                    close();
                    break;
                }
            }
            scanner.close();
        }).start();
    }
    
}
