package com.solutions.y25.day7;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;

public class Splitter {

    byte[] data;
    int n, width;

    public long solve2(Path file) {
        try {
            data = Files.readAllBytes(file);
            n = data.length;
            width = 0;
            int start = -1;
            while (data[width] != '\n') {
                if (data[width] == 'S') start = width;
                width++;
            }
            width++; // include newline

            long[] cur = new long[n];
            cur[start] = 1L;

            long totalTimelines = 0L;
            long[] next = new long[n];

            // iterate until no active timelines
            boolean any;
            do {
                Arrays.fill(next, 0L);
                any = false;

                for (int pos = 0; pos < n; pos++) {
                    long c = cur[pos];
                    if (c == 0) continue;
                    int j = pos + width;
                    if (j >= n) {
                        // beam leaves the bottom -> add all timelines
                        totalTimelines += c;
                        continue;
                    }
                    if (data[j] == '\n') {
                        continue;
                    }

                    if (data[j] == '^') {
                        int left = j - 1;
                        int right = j + 1;
                        if (left >= 0 && data[left] != '\n') {
                            next[left] += c;
                            any = true;
                        }
                        if (right < n && data[right] != '\n') {
                            next[right] += c;
                            any = true;
                        }
                    } else {
                        next[j] += c;
                        any = true;
                    }
                }

                long[] tmp = cur;
                cur = next;
                next = tmp;

            } while (any);

            return totalTimelines;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int solve1(Path file) {
        try {
            data = Files.readAllBytes(file);
            ArrayDeque<Integer> q = new ArrayDeque<>(124);
            n = data.length;
            width = 0;
            int i = 0;
            while (data[width] != '\n') {
                if (data[width] == 'S') i = width;
                width++;
            }
            width++;
            q.add(i);

            int total = 0;
            while (!q.isEmpty()) {
                int j = q.poll() + width;
                if (j < n) {
                    if (data[j] == '^') {
                        boolean separated = false;
                        if (j - 1 >= 0 && data[j - 1] != '|') {
                            data[j - 1] = '|';
                            q.add(j - 1);
                            separated = true;
                        }
                        if (j + 1 < n && data[j + 1] == '.') {
                            data[j + 1] = '|';
                            q.add(j + 1);
                            separated = true;
                        }
                        if (separated) total++;
                    } else if (data[j] != '|') q.offer(j);
                }
                //show_data(data,width,n);
                //System.out.println("Curr : "+total);
            }


            return total;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        try {
            Splitter id = new Splitter();
            long start = System.nanoTime();
            long result = id.solve2(Path.of("./src/main/java/com/solutions/y25/day7/data.txt"));
            long end = System.nanoTime();
            System.out.println("Result: " + result);
            System.out.println("Time: " + (end - start) / 1_000_000.0 + " ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
