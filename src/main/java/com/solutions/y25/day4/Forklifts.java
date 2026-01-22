package com.solutions.y25.day4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Forklifts {

    public long solve(Path file) {
        try {
            byte[] data = Files.readAllBytes(file);
            int n = data.length, width = 0, total = 0;
            byte[] newData = new byte[n];

            while (width<n && data[width] != '\n') width++;
            width++;

            while (true) {
                int sum = 0, i = 0 , height = 0;
                while (i < n) {
                    switch (data[i]) {
                        case '\n': {
                            newData[i++] = '\n';
                            height++;
                            break;
                        }
                        case '.': {
                            newData[i++] = '.';
                            break;
                        }
                        case '@': {
                            byte cnt = 0;
                            boolean existLeft = i - 1 > -1 && data[i - 1] != '\n';
                            boolean existRight = i + 1 < n && data[i + 1] != '\n';
                            boolean existUp = height > 0;
                            boolean existDown = i + width < n;

                            if (existLeft) {
                                if (data[i - 1] == '@') cnt++;
                                if (existUp && data[i - 1 - width] == '@') cnt++;
                                if (existDown && data[i + width - 1] == '@') cnt++;
                            }
                            if (existRight) {
                                if (data[i + 1] == '@') cnt++;
                                if (existUp && data[i - width + 1] == '@') cnt++;
                                if (existDown && data[i + width + 1] == '@') cnt++;
                            }
                            if (existUp && data[i - width] == '@') cnt++;
                            if (existDown && data[i + width] == '@') cnt++;
                            newData[i++] = (byte)((cnt < 4) ? '.' : '@');
                            sum += (cnt < 4) ? 1 : 0;
                        }
                    }
                }
                if(sum == 0) break;
                total += sum;
                data = newData;
            }

            return total;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        try {
            Forklifts id = new Forklifts();
            long start = System.nanoTime();
            long result = id.solve(Path.of("./src/main/java/com/solutions/y25/day4/data.txt"));
            long end = System.nanoTime();
            System.out.println("Result: " + result);
            System.out.println("Time: " + (end - start) / 1_000_000.0 + " ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
