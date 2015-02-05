package TCPServer;

import java.io.*;
import java.net.*;


public class TCPServer  {
    static final int TIMEOUT = 5000;
    static int COMMAND_LINE = 6;

    //static String config;
    static InetAddress IP;
    static String directory;
    static int port;

    public static void main(String args[]) {
        TCPServer tcp = new TCPServer();
        try {
            tcp.ConfigServer(args);
            tcp.StartServer();
        }
        catch (IOException e) {
            System.out.println("There was an error with the configuration " + e.getMessage());
        }
    }
    public void StartServer() throws IOException{
        ServerSocket welcomeSocket = new ServerSocket(port, 0, IP);
        System.out.println("The server is running on: IP " + IP + " PORT: " + port);
        while (true) {
            Socket connectionSocket;
            try {
                connectionSocket = welcomeSocket.accept();
                Communication req = new Communication(connectionSocket);
            } catch (IOException e) {
                System.out.println("There was an error with the configuration " + e.getMessage());
            }
        }
    }

    private static void ConfigServer(String args[]) throws IOException {

        if (args.length != COMMAND_LINE) {
            String parts[] = new String[6];
            parts[1] = "127.0.0.1";
            parts[3] = "8080";
            if ((args[1].charAt(0) != '/')) {
                System.out.println("Invalid directory. Your directory must start with a /");
                System.exit(-1);
            }
            parts[5] = args[1];
            ReadConfig(parts);

        } else if (args.length == COMMAND_LINE) {
            ReadConfig(args);
        } else {
            System.out.println("The command is invalid.");
            System.exit(-1);
        }

    }

    private static void ReadConfig(String[] args)  throws IOException {
        IP = GetIPAddress(args[1]);
        port = Integer.parseInt(args[3]);
        if (port < 1024 || port > 65535) {
            System.out.println("Invalid port number: " + port);
            System.exit(-1);
        }
        if ((args[5].charAt(0) != '/')) {
            System.out.println("Invalid directory. Your directory must start with a /");
            System.exit(-1);
        }
        directory = args[5];
        Transaction fm = Transaction.getInstance(directory);
        //fm.exit();
    }

    private static InetAddress GetIPAddress (String IP) throws IOException {
        InetAddress inetAddress = null;
        String[] StrArr = IP.split("\\.");
        if (StrArr.length != 4) {
            System.out.println("Unknown IP address: " + IP);
            return null;
        }
        else {
            byte[] ByteAddr = {(byte)Integer.parseInt(StrArr[0]), (byte)Integer.parseInt(StrArr[1]),
                    (byte)Integer.parseInt(StrArr[2]), (byte)Integer.parseInt(StrArr[3])};
            try {
                inetAddress = InetAddress.getByAddress(ByteAddr);
                if (!inetAddress.isReachable(TIMEOUT)) {
                    System.out.println("Unknown IP address: " + IP);
                    return null;
                }
            } catch (UnknownHostException uhe) {
                System.out.println("Unknown IP address: " + IP);
                return null;
            } catch (IOException ioe) {
                System.out.println("I/O Error: " + ioe.getMessage());
                return null;
            }
        }

        return inetAddress;
    }

}