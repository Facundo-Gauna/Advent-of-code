package com.solutions.y25.day12;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class TreeFarm {

    public long solve1(Path p) {
        final int START_LINE = 31;      // hardcode position to avoid blocks parsing
        final int PIECE_SIZE = 7;

        try (Stream<String> lines = Files.lines(p)) {
            return lines
                    .skip(START_LINE)
                    .filter(line -> !line.isEmpty())
                    .map(String::trim)
                    .mapToInt(line -> {
                        try {
                            // split into "WxH" and "counts"
                            String[] sides = line.split(":", 2);
                            String[] dims  = sides[0].split("x", 2);

                            int w = Integer.parseInt(dims[0].trim());
                            int h = Integer.parseInt(dims[1].trim());

                            long space = (long) w * h; // use long to avoid overflow
                            long used = 0L;

                            if (sides.length > 1) {
                                String[] counts = sides[1].trim().split(" ");
                                for (String c : counts) {
                                    if (c.isEmpty()) continue;
                                    // accumulate used space; stop early if already >= space
                                    used += Integer.parseInt(c) * (long) PIECE_SIZE;
                                    if (used >= space) break;
                                }
                            }

                            return (space - used) > 0 ? 1 : 0;
                        } catch (Exception ex) {
                            return 0;
                        }
                    })
                    .sum();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void main(String[] args) {
        try {
            TreeFarm t = new TreeFarm();
            long start = System.nanoTime();
            long result = t.solve1(Path.of("./src/main/java/com/solutions/y25/day12/data.txt"));
            long end = System.nanoTime();
            System.out.println("Result: " + result);
            System.out.println("Time: " + (end - start) / 1_000_000.0 + " ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
