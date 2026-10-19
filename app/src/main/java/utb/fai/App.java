package utb.fai;

import java.net.ServerSocket;

public class App {

    public static void main(String[] args) {
        //int port = 12345;
        //int max_threads = 10;    

        //////////////////////////////////////////////////////////////////////////////////
        if (args.length < 2) {
            System.err.println("Pouzijte mene argumentu(2). <port>,<max_threads>");
            System.exit(1);
        }

        int port; // args[0] - Port, na kterém bude server naslouchat (int)
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Port musi byt cislo.");
            return;
        }

        int max_threads; // args[1] - Maximální počet vláken. V tomto případě číslo bude 
        // i odpovídat maximálnímu počtu klientů, kteří budou moci být v jeden okamžik
        //  připojení. (int)
        try {
            max_threads = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Maximalni pocet vlaken musi byt cislo.");
            return;
        }
        //////////////////////////////////////////////////////////////////////////////////

        
        // Implementation of the main server loop
        new Server(port, max_threads).start();

    }
}
