import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.net.*;
import java.io.*;

public class Client {
    private Socket socket = null;
    private BufferedReader input = null;
    private DataOutputStream output = null;

    private String largestServer = null;

    public Client(String address, int port) throws Exception {
        socket = new Socket(address, port);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new DataOutputStream(socket.getOutputStream());
    }

    private void sendCmd(String cmd) throws IOException {
        byte[] commands = cmd.getBytes();
        output.write(commands, 0, commands.length);
        output.flush();
    }

    private String getCmd() throws Exception {
        StringBuilder cmd = new StringBuilder();
        while (cmd.length() < 1) {
            while (input.ready()) {
                cmd.append((char) input.read());
            }
        }
        String getCmd = cmd.toString();
        return getCmd;
    }

    private static String submitTime = "";
    private static String jobID = "";
    private static String estRuntime = "";
    private static String core = "";
    private static String memory = "";
    private static String disk = "";


    private static String res(Client client, String job) throws Exception {
        String cmd = "";
        String[] readlines = job.split(" ");

        submitTime = readlines[1];
        jobID = readlines[2];
        estRuntime = readlines[3];
        core = readlines[4];
        memory = readlines[5];
        disk = readlines[6].replace("\n", "");

        client.sendCmd("GETS All " + core + " " + memory + " " + disk);
        cmd = client.getCmd();

        while (!cmd.equals(".")) {
            client.sendCmd("OK");
            cmd = client.getCmd();

        }
        return cmd;
    }


    

}
