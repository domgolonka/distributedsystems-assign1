package TCPServer;
import java.io.*;
import java.util.*;

public class Transaction {
    String directory;
    static int nextTid;
    private static Transaction ts = null;


    public static synchronized Transaction getInstance(String dir) {
        if (ts == null) {
            ts = new Transaction(dir);
            ts.Start();
        }
        return ts;
    }


    public Vector<Integer> HandleCommit(int tid, int num) throws IOException {
        String filename = new String();
        String txtfile = tid + ".txn.txt";
        File fileFromTxn = new File(directory, txtfile);
        FileInputStream inFromClient = new FileInputStream(fileFromTxn);
        int ch;
        int cstate = inFromClient.read();
        //System.out.println("TESTING HERE: " + cstate);
        if (cstate == 0)

        {
            while ((ch = inFromClient.read()) != -1) {
                filename += (char) ch;
            }
            inFromClient.close();
            ArrayList<byte[]> content = new ArrayList<byte[]>();
            Vector<Integer> vi = new Vector<Integer>();
            for (int i = 1; i < num; i++)
            {
                String writefile = new String();
                writefile += tid + "_" + i + ".write.txt";
                File pFile = new File(directory, writefile);
                if (pFile.exists() == false) {
                    vi.add(i);
                    return vi;
                }
                FileInputStream pin = new FileInputStream(pFile);
                content.add(readContent(pin));
                pin.close();
            }
            File dest = new File(directory, filename);
            if (dest.exists() == false) {
                dest.createNewFile();
            }
            boolean append = true;
            FileOutputStream dout = new FileOutputStream(dest, append);
            for (int i = 0; i < content.size(); i++) {
                dout.write(content.get(i));
            }
            dout.flush();
            dout.close();
            FileOutputStream outToTxn = new FileOutputStream(fileFromTxn);
            outToTxn.write(1);
            outToTxn.flush();
            outToTxn.close();

        } else if (cstate == 1)
        {
            return new Vector<Integer>();

        } else if (cstate == 2)
        {
            Vector<Integer> ve = new Vector<Integer>();
            ve.add(-1);
            return ve;
        } else {
            return new Vector<Integer>();
        }
        return new Vector<Integer>();
    }
    public int HandleWrite(String data, int tid, int seq) throws IOException {
        String txtfile = tid + ".txn.txt";
        File txnFile = new File(directory, txtfile);
        FileInputStream inFromClient = new FileInputStream(txnFile);

        int cstate = inFromClient.read();
        //System.out.println("TESTING HERE: " + cstate);
        inFromClient.close();
        if (cstate != 0) {
            return -1;
        }
        String writefile = tid + "_" + seq + ".write.txt";
        File f = new File(directory, writefile);
        f.createNewFile();
        FileOutputStream outToClient = new FileOutputStream(f);

        outToClient.write(data.getBytes("US-ASCII"));
        outToClient.flush();
        outToClient.close();
        return 0;
    }




    public void HandleAbort(int tid, int num) throws IOException {

        String txtfile = tid + ".txn.txt";
        File txnFile = new File(directory, txtfile);
        FileInputStream inFromClient = new FileInputStream(txnFile);

        int cstate = inFromClient.read();
        inFromClient.close();
        if (cstate == 2) {
            return;
        } else {
            for (int i = 1; i < num; i++)
            {
                String writefile = new String();
                writefile += tid + "_" + i + ".write.txt";
                File pFile = new File(directory, writefile);
                if (pFile.exists() == false) {
                    continue;
                } else {
                    pFile.delete();
                }
                FileOutputStream outToClient = new FileOutputStream(txnFile);
                cstate = 2;
                outToClient.write(2);
                outToClient.flush();
                outToClient.close();
            }
        }
    }

    private byte[] readContent(FileInputStream inFromClient) {
        int len;
        try {
            len = inFromClient.available();
            byte[] a = new byte[len];
            inFromClient.read(a);
            return a;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    private void Start() {
        String txtfile = ".log";
        File f = new File(directory, txtfile);
        if (f.exists())
        {
            try {
                FileInputStream inFromFile = new FileInputStream(f);
                String startingtid = new String();
                int a;
                while ((a = inFromFile.read()) != -1) {
                    startingtid += (char) a;
                }
                nextTid = Integer.parseInt(startingtid);
                nextTid++;
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            try {
                f.createNewFile();
                FileOutputStream outToFile = new FileOutputStream(f);
                nextTid = 0;
                outToFile.write('0');
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private Transaction(String directory) {
        this.directory = directory;
    }
    public String getDirectory() {
            return this.directory;
    }
    public void HandleNewTxn(String filename, int tid) throws IOException {
        String txnfile = tid + ".txn.txt";
        File f = new File(directory, txnfile);

        f.createNewFile();
        FileOutputStream outToClient = new FileOutputStream(f);
        outToClient.write(0);
        outToClient.write(filename.getBytes("US-ASCII"));

        outToClient.flush();
        outToClient.close();

    }



    public int getNextTid() {
        return nextTid++;
    }
    public boolean checkFile(String filepath) {
        File f = new File(directory, filepath);
        if(f.exists() && !f.isDirectory()) {
            return true;
        }
        return false;
    }
    public boolean checkTid(int tid) {
        String txtfile =  tid + ".txn.txt";
        File txnFile = new File(directory, txtfile);
        return txnFile.exists();

    }
    public void exit() {
        long filelengthtime = 15 * 60 * 1000;
        File f = new File(this.getDirectory());
        if (!f.exists())
            return;
        File[] fileList = f.listFiles();
        long currenttime = System.currentTimeMillis();
        for (int i = 0; i < fileList.length; i++) {
            long lastmodified = fileList[i].lastModified();
            if (Math.abs(lastmodified - currenttime) > filelengthtime) {
                deleteDirectory(fileList[i]);
            }
        }
    }
    public void deleteDirectory(File path) {
        try {
            File[] sub = path.listFiles();
            for (File file : sub) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                    file.delete();
                } else {
                    file.delete();
                }
            }
            path.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}