package com.solutions.y25.day3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Batteries {

    static long maxJoltage(byte[] data, int start, int len) {
        final int K = 12;
        int pos = start;
        int end = start + len;
        long value = 0;

        for (int picked = 0; picked < K; picked++) {
            int maxIdx = pos;
            int limit = end - (K - picked) + 1;

            for (int i = pos; i < limit; i++) {
                if (data[i] > data[maxIdx]) {
                    maxIdx = i;
                }
            }

            value = value * 10 + (data[maxIdx] - '0');
            pos = maxIdx + 1;
        }

        return value;
    }

    public long solve2(Path file) {
        try {
            byte[] data = Files.readAllBytes(file);
            long sum = 0;
            int i = 0;

            while (i < data.length) {
                int start = i;
                while (i < data.length && data[i] != '\n') i++;
                int len = i - start;

                if (len >= 12) {
                    sum += maxJoltage(data, start, len);
                }

                i++; // skip newline
            }

            return sum;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long solve1(Path file) {
        try {
            byte[] data = Files.readAllBytes(file);
            int i = 0, n = data.length, sum = 0;
            while (i < n) {
                byte currMax = 0, currMin = 0;
                while (i<n && !(data[i] < '0' || data[i] > '9')){
                    byte c = (byte) (data[i++]-'0');
                    if(c > currMax && (i<n&&data[i]!='\n') ){currMax = c; currMin = 0;}
                    else if(c > currMin) currMin = c;
                }
                i++; // avoid new line
                sum += currMax*10+currMin;
                System.out.println((currMax*10+currMin)+" - "+sum);
            }

            return sum;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ArithmeticException ae) {
            throw new RuntimeException("Overflow", ae);
        }
    }

    public static void main(String[] args) {
        try {
            Batteries id = new Batteries();
            long start = System.nanoTime();
            long result = id.solve2(Path.of("./src/main/java/com/solutions/y25/day3/data.txt"));
            long end = System.nanoTime();
            System.out.println("Result: " + result);
            System.out.println("Time: " + (end - start) / 1_000_000.0 + " ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
