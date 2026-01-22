package com.solutions.y25.day2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

public class IdChecker {

    static final long[] POW10 = new long[]{
            1L, 10L, 100L, 1000L, 10000L, 100000L, 1000000L, 10000000L, 100000000L, 1000000000L,
            10000000000L, 100000000000L, 1000000000000L, 10000000000000L, 100000000000000L,
            1000000000000000L, 10000000000000000L, 100000000000000000L, 1000000000000000000L
    };

    // helper: ceilDiv for positive longs
    private static long ceilDiv(long a, long b) {
        return (a + b - 1) / b;
    }

    public long solve(Path file) {
        try {
            byte[] data = Files.readAllBytes(file);
            long invalidSum = 0L;
            int i = 0, n = data.length;

            while (i < n) {

                while (i < n && (data[i] < '0' || data[i] > '9')) i++;
                if (i >= n) break;

                long left = 0L;
                while (i < n && (data[i] >= '0' && data[i] <= '9')) {
                    left = left * 10L + (data[i] - '0');
                    i++;
                }

                // skip non-digit separators between left and right (should hit '-' normally)
                while (i < n && (data[i] < '0' || data[i] > '9')) i++;
                if (i >= n) break;

                long right = 0L;
                while (i < n && (data[i] >= '0' && data[i] <= '9')) {
                    right = right * 10L + (data[i] - '0');
                    i++;
                }

                invalidSum += sequence_range(left,right);
            }

            return invalidSum;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ArithmeticException ae) {
            throw new RuntimeException("Overflow", ae);
        }
    }

    private long sequence_range(long left, long right) {
        if (left > right) return 0L;
        long sum = 0L;
        HashSet<Long> seen = new HashSet<>();

        final int MAX_TOTAL_DIGITS = 18; // POW10 covers up to 10^18
        final int MAX_K = 9; // block length up to 9 (k * r <= MAX_TOTAL_DIGITS)

        for (int k = 1; k <= MAX_K; k++) {
            long minH = POW10[k - 1];
            long maxH = POW10[k] - 1;

            int maxR = MAX_TOTAL_DIGITS / k;
            for (int r = 2; r <= maxR; r++) { // include r==2 here
                // compute denom = sum_{j=0..r-1} 10^{k*j}
                long denom = 0L;
                for (int j = 0; j < r; j++) {
                    denom = Math.addExact(denom, POW10[k * j]);
                }

                // allowable h range so that N = h * denom is in [left, right]
                long hLo = ceilDiv(left, denom);
                if (hLo < minH) hLo = minH;
                long hHi = right / denom;
                if (hHi > maxH) hHi = maxH;

                if (hLo > hHi) continue;

                // iterate h individually and dedupe N's (safe, simple)
                for (long h = hLo; h <= hHi; h++) {
                    long N = Math.multiplyExact(h, denom);
                    // sanity check (should be in range by construction)
                    if (N < left || N > right) continue;
                    if (seen.add(N)) {
                        sum = Math.addExact(sum, N);
                    }
                }
            }
        }
        return sum;
    }

    public static void main(String[] args) {
        try {
            IdChecker id = new IdChecker();
            long start = System.nanoTime();
            long result = id.solve(Path.of("./src/main/java/com/solutions/y25/day2/data.txt"));
            long end = System.nanoTime();
            System.out.println("Result: " + result);
            System.out.println("Time: " + (end - start) / 1_000_000.0 + " ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
