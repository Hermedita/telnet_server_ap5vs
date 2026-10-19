package utb.fai;

import java.io.*;
import java.net.*;

public class TelnetClient {

    private String serverIp;
    private int port;
    private Socket socket;
    private volatile boolean running = true;

    public TelnetClient(String serverIp, int port) {
        this.serverIp = serverIp;
        this.port = port;
    }

    public void run() throws InterruptedException {
        try (Socket s = new Socket(serverIp, port)) {
            this.socket = s;

            Thread readThread = new Thread(this::readFromClient);
            Thread serverThread = new Thread(this::readFromServer);

            readThread.setDaemon(true);

            readThread.start();
            serverThread.start();

            serverThread.join();

        } catch (IOException e) {
            System.err.println("Nepodarilo se pripojit: " + e.getMessage());
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    
        System.out.println("Klient ukoncen");
    }

    public void readFromClient(){
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            String line;
            while(running && (line = reader.readLine()) != null){
                if(line.trim().equalsIgnoreCase("/QUIT")){
                    clientEnd();
                    break;
                }
                writer.println(line);
            }
        }catch(IOException e){
            if (running){
                e.printStackTrace();
            }
        }
    }

    public void readFromServer(){
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
            String line;
            while((line = reader.readLine()) != null){
                System.out.println(line);
            }
        }catch(IOException e){
            if(running){
                e.printStackTrace();
            }
        } finally{
            running = false;
        }
    }


    public void clientEnd(){
        running = false;
        try{
            if (socket != null && !socket.isClosed()){
                socket.close();
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}