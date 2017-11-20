package ru.nt202;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Train train = new Train();
        train.run();

        Test test = new Test(train);
        test.run();
    }
}
