package TCPServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Arrays;

public class Communication extends Thread {
    OutputStream outToClient;
    InputStream inFromClient;
    PrintStream out;
    public Communication(Socket socket) {
                 Socket connectionSocket = socket;
            try {
                    inFromClient = connectionSocket.getInputStream();
                    outToClient = connectionSocket.getOutputStream();
                    out = new PrintStream(connectionSocket.getOutputStream(), false);
                    this.start();
            }
            catch (IOException e) {
                System.out.println("There is an issue with the server: " + e.getMessage());
            }
        }

    public void sendMsg(Message msg){

        try {
            System.out.println(msg.print());
            out.print(msg.print());
        } catch (Exception e) {
            System.out.println("There was an issue with send Message" + e.getMessage());
        }
    }
    private void ProcessRequest(InputStream input)throws IOException{
        try{
            String method = new String();
            String startTid = new String();
            String sequence = new String();
            String conlen = new String();
            String data = new String();
            int tid, seq, len;
            byte bytez;
            while(!isSpaceSeq(bytez=(byte)input.read()))
            {
                method+=(char)bytez; // uppercase or lower case
            }
            while(!isSpaceSeq(bytez=(byte)input.read()))
            {
                startTid+=(char)bytez;
            }
            tid=Integer.parseInt(startTid);
            while(!isSpaceSeq(bytez=(byte)input.read()))
            {
                sequence+=(char)bytez;
            }
            seq=Integer.parseInt(sequence);
            while(!isSpaceSeq(bytez=(byte)input.read()))
            {
                conlen+=(char)bytez;
            }
            len=Integer.parseInt(conlen);
            if(len>0)
            { input.skip(3);
                for(int i=0;i<len;i++)
                {  bytez=(byte)input.read();
                    data+=(char)bytez;
                }
                input.skip(4);
            }
            else
                input.skip(5);
            Message.ResponseMessage(this, method, tid, seq, len, data);
        }
        catch(NumberFormatException ne)
        {
            ReqMessage emsg=new ReqMessage("ERROR", -1,-1,"203", "");
            this.sendMsg(emsg);
        }


    }
    private boolean isSpaceSeq(byte bytez)
    {
        if(bytez==32||bytez==10||bytez==13)
            return true;
        else
            return false;
    }
    public void run() {
        while(true) {
            try {
                ProcessRequest(inFromClient);
            }catch(IOException e)
            {
                e.printStackTrace();
                return;

            }

        }
    }

}
