package ru.nt202;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.inverse.InvertMatrix;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Test {
    private static final int PARAMETERS_QUANTITY = 5; // for each classes

    // Parameters:
    // 0=Red, 1=Green, 2=Blue, 3=Hue, 4=Saturation

    private INDArray class1CovarianceMatrix;
    private INDArray class2CovarianceMatrix;
    private INDArray class1MeanValues;
    private INDArray class2MeanValues;

    public Test(Train train) {
        class1CovarianceMatrix = Nd4j.create(train.getClass1CovarianceMatrix());
        class2CovarianceMatrix = Nd4j.create(train.getClass2CovarianceMatrix());
        class1MeanValues = Nd4j.create(train.getClass1MeanValues(), new int[]{PARAMETERS_QUANTITY, 1});
        class2MeanValues = Nd4j.create(train.getClass2MeanValues(), new int[]{PARAMETERS_QUANTITY, 1});
    }

    private BufferedImage testImage = null;
    private BufferedImage class1Result = null;
    private BufferedImage class2Result = null;

    private int height = 0;
    private int width = 0;

    public void run() {
        loadTestImage();
        setClassImages();
        showImage(testImage);
        showImage(class1Result);
        showImage(class2Result);
    }

    private void loadTestImage() {
        try {
            testImage = ImageIO.read(Main.class.getClass().
                    getResourceAsStream("/test/0.jpg"));
            height = testImage.getHeight();
            width = testImage.getWidth();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setClassImages() {
        class1Result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        class2Result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Color pixelColor = new Color(testImage.getRGB(j, i), false);
                int[] values = new int[PARAMETERS_QUANTITY];
                values[0] = pixelColor.getRed();
                values[1] = pixelColor.getGreen();
                values[2] = pixelColor.getBlue();
                float[] hsv = new float[3];
                Color.RGBtoHSB(values[0], values[1], values[2], hsv);
                values[3] = (int) (360 * hsv[0]);
                values[4] = (int) (100 * hsv[1]);
                if (chooseClass(values) == 1) {
                    class1Result.setRGB(j, i, pixelColor.getRGB());
                } else {
                    class2Result.setRGB(j, i, pixelColor.getRGB());
                }
            }
        }
    }

    private int chooseClass(int[] values) {
        double[] theValues = new double[values.length];
        for (int n = 0; n < values.length; n++) {
            theValues[n] = values[n];
        }
        if (calculateMahalanobisDistance(theValues, class1CovarianceMatrix) >
                calculateMahalanobisDistance(theValues, class2CovarianceMatrix)) {
            return 2;
        } else {
            return 1;
        }
    }

    private double calculateMahalanobisDistance(double[] theValues, INDArray classCovarianceMatrix) {
        INDArray values = Nd4j.create(theValues, new int[]{PARAMETERS_QUANTITY, 1});
        INDArray multiplier1;
        INDArray multiplier2;
        if (classCovarianceMatrix.equals(class1CovarianceMatrix)) {
            multiplier1 = values.sub(class1MeanValues).transpose();
            multiplier2 = InvertMatrix.invert(class1CovarianceMatrix, false);
        } else {
            multiplier1 = values.sub(class2MeanValues).transpose();
            multiplier2 = InvertMatrix.invert(class2CovarianceMatrix, false);
        }
        INDArray multiplier3 = multiplier1.transpose();
        INDArray result = multiplier1.mmul(multiplier2).mmul(multiplier3);
        return result.getDouble(0);
    }

    private void showImage(BufferedImage image) {
        try {
            JFrame frame = new JFrame();
            frame.getContentPane().setLayout(new FlowLayout());
            frame.getContentPane().add(new JLabel(new ImageIcon(image)));
            frame.pack();
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
