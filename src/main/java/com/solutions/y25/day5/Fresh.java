package com.solutions.y25.day5;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Fresh {

    static class Overlap {
        private final ArrayList<long[]> ranges = new ArrayList<>();

        public void insert(long left, long right) {
            if (left > right) {
                long tmp = left; left = right; right = tmp;
            }

            int size = ranges.size();
            if (size == 0) {
                ranges.add(new long[]{left, right});
                return;
            }

            int lo = 0, hi = size;
            while (lo < hi) {
                int mid = (lo + hi) >>> 1;
                if (ranges.get(mid)[0] <= left) lo = mid + 1;
                else hi = mid;
            }
            int idx = lo;

            long newL = left;
            long newR = right;
            if (idx > 0) {
                long[] prev = ranges.get(idx - 1);
                if (prev[1] + 1 >= left) {
                    idx = idx - 1;
                    newL = Math.min(prev[0], newL);
                    newR = Math.max(prev[1], newR);
                    ranges.remove(idx);
                    size--;
                }
            }

            while (idx < size) {
                long[] cur = ranges.get(idx);
                long curL = cur[0];
                long curR = cur[1];

                if (newR != Long.MAX_VALUE && curL <= newR + 1) {
                    newL = Math.min(newL, curL);
                    newR = Math.max(newR, curR);
                    ranges.remove(idx);
                    size--;
                } else {
                    break;
                }
            }

            ranges.add(idx, new long[]{newL, newR});
        }

        public boolean contains(long val) {
            int lo = 0, hi = ranges.size() - 1;
            while (lo <= hi) {
                int mid = (lo + hi) >>> 1;
                long[] r = ranges.get(mid);
                if (val < r[0]) {
                    hi = mid - 1;
                } else if (val > r[1]) {
                    lo = mid + 1;
                } else {
                    return true;
                }
            }
            return false;
        }

        // ∑ (right - left + 1)
        public long totalCovered() {
            long total = 0L;
            for (long[] r : ranges) {
                total += (r[1] - r[0] + 1L);
            }
            return total;
        }

        public List<long[]> getRanges() {
            return new ArrayList<>(ranges);
        }
    }

    public long solve(Path file) {
        try {
            byte[] data = Files.readAllBytes(file);
            Overlap overlap = new Overlap();
            int n = data.length, i = 0;
            long total = 0;

            while(i<n && data[i] != '\n'){
                long l = 0;
                while (data[i] != '-') l = l*10 + (data[i++]-'0');
                i++;
                long r = 0;
                while (i<n && data[i] != '\n') r = r*10 + (data[i++]-'0');
                i++;
                overlap.insert(l,r);
            }
// Part 1:
//            i++;
//            while (i < n){
//                long v = 0;
//                while (i<n && data[i] != '\n') v = v*10 + (data[i++]-'0');
//                i++;
//                if(overlap.contains(v)) total++;
//            }

            total += overlap.totalCovered();
            return total;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        try {
            Fresh id = new Fresh();
            long start = System.nanoTime();
            long result = id.solve(Path.of("./src/main/java/com/solutions/y25/day5/data.txt"));
            long end = System.nanoTime();
            System.out.println("Result: " + result);
            System.out.println("Time: " + (end - start) / 1_000_000.0 + " ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
