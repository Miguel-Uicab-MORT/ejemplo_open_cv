package chatVideo1;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;

public class VideoServer {
    private ServerSocket serverSocket;
    private List<Socket> clients = new ArrayList<>();
    private VideoCapture camera;

    public VideoServer(int port) {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            camera = new VideoCapture(0);
            serverSocket = new ServerSocket(port);
            System.out.println("Video Server started on port " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            while (true) {
                try {
                    Mat frame = new Mat();
                    if (camera.read(frame)) {
                        BufferedImage image = Mat2BufferedImage(frame);
                        for (Socket client : clients) {
                            OutputStream outputStream = client.getOutputStream();
                            ImageIO.write(image, "jpg", outputStream);
                        }
                        HighGui.imshow("Camera", frame);
                        HighGui.waitKey(30);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);
                clients.add(clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new VideoServer(12345);
    }

    private static BufferedImage Mat2BufferedImage(Mat matrix) {
        int cols = matrix.cols();
        int rows = matrix.rows();
        int elemSize = (int) matrix.elemSize();
        byte[] data = new byte[cols * rows * elemSize];
        int type;
        matrix.get(0, 0, data);
        switch (matrix.channels()) {
            case 1:
                type = BufferedImage.TYPE_BYTE_GRAY;
                break;
            case 3:
                type = BufferedImage.TYPE_3BYTE_BGR;
                // bgr to rgb
                byte b;
                for (int i = 0; i < data.length; i = i + 3) {
                    b = data[i];
                    data[i] = data[i + 2];
                    data[i + 2] = b;
                }
                break;
            default:
                return null;
        }
        BufferedImage image = new BufferedImage(cols, rows, type);
        image.getRaster().setDataElements(0, 0, cols, rows, data);
        return image;
    }
}
