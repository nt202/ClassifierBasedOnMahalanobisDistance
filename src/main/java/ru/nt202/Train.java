package ru.nt202;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

public class Train {
    private static final int TRAIN_SET_NUMBER = 9; // for each classes
    private static final int PARAMETERS_QUANTITY = 5; // for each classes

    // Parameters:
    // 0=Red, 1=Green, 2=Blue, 3=Hue, 4=Saturation

    private LinkedList<Integer[]> class1Pixels = new LinkedList<Integer[]>();
    private LinkedList<Integer[]> class2Pixels = new LinkedList<Integer[]>();

    private double[] class1MeanValues = new double[PARAMETERS_QUANTITY];
    private double[] class2MeanValues = new double[PARAMETERS_QUANTITY];

    private double[][] class1CovarianceMatrix = new double[PARAMETERS_QUANTITY][PARAMETERS_QUANTITY];
    private double[][] class2CovarianceMatrix = new double[PARAMETERS_QUANTITY][PARAMETERS_QUANTITY];

    public double[][] getClass1CovarianceMatrix() {
        return class1CovarianceMatrix;
    }

    public double[][] getClass2CovarianceMatrix() {
        return class2CovarianceMatrix;
    }

    public double[] getClass1MeanValues() {
        return class1MeanValues;
    }

    public double[] getClass2MeanValues() {
        return class2MeanValues;
    }

    public void run() {
        setClassPixels("/train/class1/", class1Pixels, ".png");
        setClassPixels("/train/class2/", class2Pixels, ".jpg");
        measureMeanValues(class1Pixels);
        measureMeanValues(class2Pixels);
        calculateCovarianceMatrices(class1Pixels);
        calculateCovarianceMatrices(class2Pixels);
    }

    private void setClassPixels(final String path, final LinkedList<Integer[]> classPixels, final String extension) {
        for (int n = 0; n < TRAIN_SET_NUMBER; n++) {
            BufferedImage trainImage = null;
            int height = 0;
            int width = 0;
            try {
                trainImage = ImageIO.read(Main.class.getClass().
                        getResourceAsStream(path + n + extension));
                height = trainImage.getHeight();
                width = trainImage.getWidth();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    Color pixelColor = new Color(trainImage.getRGB(j, i), true);
                    if (pixelColor.getAlpha() != 0) {
                        Integer[] values = new Integer[PARAMETERS_QUANTITY];
                        values[0] = pixelColor.getRed();
                        values[1] = pixelColor.getGreen();
                        values[2] = pixelColor.getBlue();
                        float[] hsv = new float[3];
                        Color.RGBtoHSB(values[0], values[1], values[2], hsv);
                        values[3] = (int) (360 * hsv[0]);
                        values[4] = (int) (100 * hsv[1]);
                        classPixels.add(values);
                    }
                }
            }
        }
    }

    private void measureMeanValues(final LinkedList<Integer[]> classPixels) {
        long[] sums = new long[PARAMETERS_QUANTITY];
        for (Integer[] values : classPixels) {
            for (int n = 0; n < PARAMETERS_QUANTITY; n++) {
                sums[n] += values[n];
            }
        }
        if (classPixels.equals(class1Pixels)) {
            for (int n = 0; n < PARAMETERS_QUANTITY; n++) {
                class1MeanValues[n] = (double) (sums[n] / classPixels.size());
            }
        } else {
            for (int n = 0; n < PARAMETERS_QUANTITY; n++) {
                class2MeanValues[n] = (double) (sums[n] / classPixels.size());
            }
        }
    }

    private void calculateCovarianceMatrices(final LinkedList<Integer[]> classPixels) {
        long[][] sums = new long[PARAMETERS_QUANTITY][PARAMETERS_QUANTITY];
        if (classPixels.equals(class1Pixels)) {
            for (Integer[] values : classPixels) {
                for (int i = 0; i < PARAMETERS_QUANTITY; i++) {
                    for (int j = 0; j < PARAMETERS_QUANTITY; j++) {
                        sums[i][j] += (long) ((values[i] - class1MeanValues[i]) * (values[j] * class1MeanValues[j]));
                    }
                }
            }
            for (int i = 0; i < PARAMETERS_QUANTITY; i++) {
                for (int j = 0; j < PARAMETERS_QUANTITY; j++) {
                    class1CovarianceMatrix[i][j] = (double) (sums[i][j] / classPixels.size());
                }
            }
        } else {
            for (Integer[] values : classPixels) {
                for (int i = 0; i < PARAMETERS_QUANTITY; i++) {
                    for (int j = 0; j < PARAMETERS_QUANTITY; j++) {
                        sums[i][j] += (long) ((values[i] - class2MeanValues[i]) * (values[j] * class2MeanValues[j]));
                    }
                }
            }
            for (int i = 0; i < PARAMETERS_QUANTITY; i++) {
                for (int j = 0; j < PARAMETERS_QUANTITY; j++) {
                    class2CovarianceMatrix[i][j] = (double) (sums[i][j] / classPixels.size());
                }
            }
        }
    }
}