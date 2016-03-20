import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JFrame;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

/*
 * Testing using opencv java to read images from ipcamera
 * 
 * First run ipcamera app on android phone then enter the given 
 * ip address below 
 * 
 * Retrieved code from https://sites.google.com/site/pdopencvjava/webcam/02-continually-detect-face-in-webcam
 */

public class Test {
	public static void main(String[] args) throws InterruptedException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		JFrame frame = new JFrame("Ip camera");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		FacePanel facePanel = new FacePanel();
		frame.setSize(400, 400);
		frame.setBackground(Color.BLUE);
		frame.add(facePanel, BorderLayout.CENTER);
		frame.setVisible(true);

		Mat webcam_image = new Mat();
		
		//enter correct ip address here
		VideoCapture camera = new VideoCapture(
				"http://172.28.91.35:8080/video?x.mjpeg");

		if (camera.isOpened()) {
			Thread.sleep(500); // / This one-time delay to initialize camera
			while (true) {
				camera.read(webcam_image);
				if (!webcam_image.empty()) {
					frame.setSize(webcam_image.width() + 40,webcam_image.height() + 60);
					facePanel.matToBufferedImage(webcam_image);
					facePanel.repaint();
				} else {
					break;
				}
			}
		}
		camera.release();

	}

}
