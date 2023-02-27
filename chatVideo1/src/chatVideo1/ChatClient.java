package chatVideo1;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import javax.swing.*;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
//import org.opencv.videoio.VideoWriter.fourcc;
//import org.opencv.videoio.VideoWriter.fourcc;
import org.opencv.videoio.Videoio;

public class ChatClient extends JFrame {
    private JTextField messageBox;
    private JTextArea messageArea;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private VideoCapture camera;
    private VideoWriter videoWriter;
    
    public ChatClient(String host, int port) {
        super("Chat Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLayout(new BorderLayout());
        
        messageBox = new JTextField();
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        
        add(messageBox, BorderLayout.SOUTH);
        add(new JScrollPane(messageArea), BorderLayout.CENTER);
        
        connectToServer(host, port);
        setupCamera();
        
        messageBox.addActionListener(e -> {
            out.println(messageBox.getText());
            messageBox.setText("");
        });
        
        setVisible(true);
        
        new Thread(() -> {
            while (true) {
                try {
                    String line = in.readLine();
                    if (line == null) {
                        break;
                    }
                    messageArea.append(line + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
        
        /*new Thread(() -> {
            while (true) {
                Mat frame = new Mat();
                if (camera.read(frame)) {
                    videoWriter.write(frame);
                    HighGui.imshow("Camera", frame);
                    HighGui.waitKey(30);
                }
            }
        }).start();*/
    }
    
    private void connectToServer(String host, int port) {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void setupCamera() {
    	new Thread(() -> {
            while (true) {
                Mat frame = new Mat();
                if (camera.read(frame)) {
                    videoWriter.write(frame);
                    HighGui.imshow("Camera", frame);
                    HighGui.waitKey(30);
                }
            }
        }).start();
    	
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        camera = new VideoCapture(0);
        Size frameSize = new Size((int) camera.get(Videoio.CAP_PROP_FRAME_WIDTH), (int) camera.get(Videoio.CAP_PROP_FRAME_HEIGHT));
        videoWriter = new VideoWriter("output.avi", VideoWriter.fourcc('M','J','P','G'), 25, frameSize, true);
    }
    
    public static void main(String[] args) {
        new ChatClient("localhost", 12345);
    }
}

