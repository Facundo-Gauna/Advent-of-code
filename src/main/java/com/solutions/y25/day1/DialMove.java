package com.solutions.y25.day1;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DialMove {
    private static final int DIAL_INIT = 50;
    private static final int DIAL_SIZE = 100;

    public int process(Path file) {
        int dial = DIAL_INIT;
        int passwordCounter = 0;
        try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(file))) {
            int b = in.read();
            while (b != -1) {
                int sign = (b == 'L') ? -1 : 1;

                int value = 0;
                while ((b = in.read()) >= '0' && b <= '9') {
                    value = value * 10 + (b - '0');
                }
                while (b != -1 && b != 'L' && b != 'R') {
                    b = in.read();
                }

                dial = Math.floorMod(dial + (sign * value), DIAL_SIZE);

                if (dial == 0) passwordCounter++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return passwordCounter;
    }

    public int process2(Path file) {
        int dial = DIAL_INIT;
        int passwordCounter = 0;
        try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(file))) {
            int b = in.read();
            while (b != -1) {
                char sign = (char)b;

                int value = 0;
                while ((b = in.read()) >= '0' && b <= '9') {
                    value = value * 10 + (b - '0');
                }
                while (b != -1 && b != 'L' && b != 'R') {
                    b = in.read();
                }
                if(value > 99){
                    int d = (int)(value * 0.01f);
                    passwordCounter += d;
                    value -= (d * 100);
                }
                value = sign == 'L' ? -value : value;
                dial += value;

                if(dial == 0) passwordCounter++;
                else if(dial < 0 || dial >= DIAL_SIZE){
                    if(dial != value) passwordCounter++;
                    dial = Math.floorMod(dial, DIAL_SIZE);
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return passwordCounter;
    }


    public static void main(String[] args) {
        long start = System.nanoTime();
        int result = new DialMove().process2(Path.of("./src/main/java/com/solutions/y25/day1/data.txt"));
        long end = System.nanoTime();

        System.out.println("Result: " + result);
        System.out.println("Time: " + (end - start) / 1_000_000.0 + " ms");
    }
}
