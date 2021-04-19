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


    private void allToLargest() throws ParserConfigurationException, IOException, SAXException {
        String largestServerType = "";
        File file = new File("./ds-system.xml");
        //an instance of factory that gives a document builder
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        //an instance of builder to parse the specified xml file
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();

        NodeList serverList = doc.getElementsByTagName("server");

        Node tempNode = serverList.item(0);
        Element tempElement = (Element) tempNode;
        int largestCore = Integer.parseInt(tempElement.getAttribute("coreCount"));

        for (int i = 0; i < serverList.getLength(); i++) {
            Node currentNode = serverList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                Element currentElement = (Element) currentNode;
                int cores = Integer.parseInt(currentElement.getAttribute("coreCount"));
                if (cores > largestCore) {
                    largestCore = cores;
                    largestServerType = currentElement.getAttribute("type");
                }
            }
        }
        largestServer = largestServerType;
    }



    public static void main(String[] args) {
        try {
            String name = System.getProperty("user.name");

            Client client = new Client("127.0.0.1", 50000);
            String cmd = "";

            client.sendCmd("HELO");
            cmd = client.getCmd();

            client.sendCmd("AUTH " + name);
            cmd = client.getCmd();

            client.sendCmd("REDY");
            cmd = client.getCmd();
            client.allToLargest();

            // Schedule job
            while(!cmd.equals("NONE")) {
                if (cmd.startsWith("JOBN") || cmd.startsWith("JOBP")) {
                    cmd = res(client, cmd);
                    client.sendCmd("SCHD " + jobID + " " + client.largestServer + " " + "0");
                }
                else if (cmd.startsWith("JCPL")){
                    client.sendCmd("REDY");
                }
                else if (cmd.startsWith("RESF")){
                    client.sendCmd("SCHD " + jobID + " " + client.largestServer + " " + "0");

                    }
                else if (cmd.startsWith("RESR")){
                    client.sendCmd("SCHD " + jobID + " " + client.largestServer + " " + "0");

                }
                cmd = client.getCmd();
                client.sendCmd("REDY");
                cmd = client.getCmd();
            }


            client.sendCmd("QUIT");

            client.input.close();
            client.output.close();
            client.socket.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}