package fr.usubelli.compta;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.openimaj.image.DisplayUtilities;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Detect {

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String filename = "D:\\workspace\\compta\\src\\main\\resources\\CFXFDZJYYS.jpg";
        Mat src = Imgcodecs.imread(filename);
        if (src.empty()) {
            System.err.println("Cannot read image: " + filename);
            System.exit(0);
        }

        DisplayUtilities.display((BufferedImage) HighGui.toBufferedImage(src));

        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        DisplayUtilities.display((BufferedImage) HighGui.toBufferedImage(gray));

        Mat blur = new Mat();
        Imgproc.medianBlur(gray, blur,9);

        DisplayUtilities.display((BufferedImage) HighGui.toBufferedImage(blur));

        Mat canny = new Mat();
        Imgproc.Canny(blur, canny, 10, 25);

        DisplayUtilities.display((BufferedImage) HighGui.toBufferedImage(canny));

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(canny, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint contour : contours) {
            final RotatedRect minAreaRect = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
            final Mat box = new Mat();
            Imgproc.boxPoints(minAreaRect, box);
            final double contourArea = Imgproc.contourArea(box);
            final double ratio = contourArea / contours.size();
            if (ratio < 0.015) {
                continue;
            }
            DisplayUtilities.display((BufferedImage) HighGui.toBufferedImage(box));
        }


    }

}
