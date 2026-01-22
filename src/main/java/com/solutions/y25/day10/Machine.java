package com.solutions.y25.day10;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Machine {

    // O(2^n * lights)
    public static int bfs(List<Integer> bottoms, int target) {
        int size = bottoms.size();
        ArrayDeque<int[]> q = new ArrayDeque<>();
        int loop = 1;
        for (int i = 0; i < size; i++) q.offer(new int[]{i, 0});

        while (!q.isEmpty()) {
            for (int currSize = q.size(); currSize > 0; currSize--) {
                int[] curr = q.poll();
                int c = curr[1] ^ bottoms.get(curr[0]);
                if (c == target) return loop;

                for (int j = curr[0] + 1; j < size; j++) {
                    q.offer(new int[]{j, c});
                }
            }
            loop++;
        }

        return 0;
    }

    public long solve1(Path file) {
        try {
            byte[] data = Files.readAllBytes(file);
            int n = data.length, sum = 0, i = 0;

            while (i < n) {
                i++;
                int target = 0;
                int lightsLen = i;
                while (data[i] != ']') {
                    if (data[i] == '#') target |= (1 << (i - lightsLen));
                    i++;
                }
                lightsLen = i - lightsLen;

                List<Integer> bottoms = new ArrayList<>();
                while (true) {
                    while (i < n && data[i] != '(' && data[i] != '{') i++;
                    if (i == n || data[i] == '{') break;
                    while (data[i] != '{') {
                        i++;
                        int bottom = 0;
                        while (data[i] != ' ') {
                            byte val = 0;
                            while (data[i] > ',') val = (byte) (val * 10 + (data[i++] - '0'));
                            bottom |= (1 << val);
                            i++;
                        }
                        bottoms.add(bottom);
                        i++;
                    }
                }

                sum += bfs(bottoms, target);

                while (i < n && data[i] != '\n') i++;
                i++;
            }

            return sum;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ---------- PART 2 (joltage) ----------
    /// So hard to me, i solved it thanks to [[Redit solution](https://www.reddit.com/r/adventofcode/comments/1pk87hl/2025_day_10_part_2_bifurcate_your_way_to_victory)]
    public static int solveSingle(List<boolean[]> coeffs, int[] goalArr) {
        IntVec goal = new IntVec(goalArr);
        Map<IntVec, Map<IntVec, Integer>> patternCosts = buildPatterns(coeffs);
        Map<IntVec, Integer> memo = new HashMap<>();

        return solveAux(goal, patternCosts, memo);
    }

    private static int solveAux(IntVec goal, Map<IntVec, Map<IntVec, Integer>> patternCosts, Map<IntVec, Integer> memo) {
        if (goal.isAllZero()) return 0;
        Integer cached = memo.get(goal);
        if (cached != null) return cached;

        IntVec parity = goal.parityKey();
        Map<IntVec, Integer> candidates = patternCosts.get(parity);
        if (candidates == null) {
            // No pattern with that parity -> inf
            memo.put(goal, Integer.MAX_VALUE / 4);
            return Integer.MAX_VALUE / 4;
        }

        int best = Integer.MAX_VALUE / 4;
        for (Map.Entry<IntVec, Integer> e : candidates.entrySet()) {
            IntVec pattern = e.getKey();
            int costPattern = e.getValue();
            // we need pattern <= goal
            if (!pattern.leq(goal)) continue;
            IntVec remaining = goal.minus(pattern);
            // per parity, remaining[i] is odd -> divide per 2 is safer
            IntVec newGoal = remaining.divideBy2();
            int sub = solveAux(newGoal, patternCosts, memo);
            if (sub >= Integer.MAX_VALUE / 8) continue;
            int cand = costPattern + 2 * sub;
            if (cand < best) best = cand;
        }

        memo.put(goal, best);
        return best;
    }

    private static Map<IntVec, Map<IntVec, Integer>> buildPatterns(List<boolean[]> coeffs) {
        int numButtons = coeffs.size();
        int numVars = coeffs.getFirst().length;

        // init all keys of parity possible
        Map<IntVec, Map<IntVec, Integer>> out = new HashMap<>();

        int subsets = 1 << numButtons;
        for (int mask = 0; mask < subsets; mask++) {
            int[] pattern = new int[numVars];
            int countButtons = Integer.bitCount(mask);
            for (int b = 0; b < numButtons; b++) {
                if ((mask & (1 << b)) != 0) {
                    boolean[] cb = coeffs.get(b);
                    for (int v = 0; v < numVars; v++) if (cb[v]) pattern[v]++;
                }
            }
            IntVec patternVec = new IntVec(pattern);
            IntVec parity = patternVec.parityKey();
            out.computeIfAbsent(parity, k -> new HashMap<>());
            Map<IntVec, Integer> map = out.get(parity);
            // save low cost patterns (num of bottoms)
            Integer prev = map.get(patternVec);
            if (prev == null || countButtons < prev) {
                map.put(patternVec, countButtons);
            }
        }
        return out;
    }

    public long solve2(Path file) {
        try {
            byte[] data = Files.readAllBytes(file);
            int n = data.length, sum = 0, i = 0;
            while (i < n) {
                while (data[i] != '(') i++;

                List<List<Byte>> bottoms = new ArrayList<>();
                while (true) {
                    while (i < n && data[i] != '(' && data[i] != '{') i++;
                    if (i == n || data[i] == '{') break;
                    while (data[i] != '{') {
                        i++;
                        List<Byte> bottom = new ArrayList<>();
                        while (data[i] != ' ') {
                            byte val = 0;
                            while (data[i] > ',') val = (byte) (val * 10 + (data[i++] - '0'));
                            bottom.add(val);
                            i++;
                        }
                        bottoms.add(bottom);
                        i++;
                    }
                }
                i++;

                List<Integer> voltajes = new ArrayList<>();
                while (i < n && data[i] != '\n') {
                    int val = 0;
                    while (data[i] != ',' && data[i] != '}')
                        val = (val * 10 + (data[i++] - '0'));
                    voltajes.add(val);
                    i++;
                }

                int numVars = voltajes.size();
                List<boolean[]> coeffs = new ArrayList<>();
                for (List<Byte> bottom : bottoms) {
                    boolean[] b = new boolean[numVars];
                    for (Byte idx : bottom) b[idx] = true;
                    coeffs.add(b);
                }

                int[] goalArr = new int[numVars];
                for (int j = 0; j < numVars; j++) goalArr[j] = voltajes.get(j);

                int minimal = solveSingle(coeffs, goalArr);
                sum += minimal;

                i++;
            }

            return sum;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        try {
            Machine t = new Machine();
            MachineMultithreading t2 = new MachineMultithreading();
            Path path = Path.of("./src/main/java/com/solutions/y25/day10/data.txt");
            int threads = Runtime.getRuntime().availableProcessors();

            long start = System.nanoTime();

            long result = t.solve2(path);
            //long result = t2.solve2(path, threads);

            long end = System.nanoTime();


            System.out.println("Result: " + result);
            System.out.println("Time: " + (end - start) / 1_000_000.0 + " ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


class IntVec {
    final int[] a;
    final int hash;

    IntVec(int[] a) {
        this.a = a;
        this.hash = Arrays.hashCode(a);
    }

    boolean isAllZero() {
        for (int v : a) if (v != 0) return false;
        return true;
    }

    IntVec minus(IntVec other) {
        int n = a.length;
        int[] out = new int[n];
        for (int i = 0; i < n; i++) out[i] = a[i] - other.a[i];
        return new IntVec(out);
    }

    IntVec divideBy2() {
        int n = a.length;
        int[] out = new int[n];
        for (int i = 0; i < n; i++) out[i] = a[i] / 2;
        return new IntVec(out);
    }

    boolean leq(IntVec other) { // this <= other componentwise
        for (int i = 0; i < a.length; i++) if (a[i] > other.a[i]) return false;
        return true;
    }

    IntVec parityKey() {
        int n = a.length;
        int[] out = new int[n];
        for (int i = 0; i < n; i++) out[i] = a[i] & 1;
        return new IntVec(out);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IntVec v)) return false;
        return Arrays.equals(this.a, v.a);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return Arrays.toString(a);
    }
}


class MachineMultithreading {
    AtomicInteger sum = null;
    ExecutorService executor = null;
    ArrayList<Integer> lines = null;
    int n;
    byte[] data;

    public long solve1(Path file, int numThreads) {
        try {
            data = Files.readAllBytes(file);
            executor = Executors.newFixedThreadPool(numThreads);
            n = data.length;
            sum = new AtomicInteger(0);
            lines = new ArrayList<>();

            lines.add(0);
            for (int j = 0; j < n; j++) {
                if (data[j] == '\n')
                    lines.add(j + 1);
            }

            for (int line : lines) {
                executor.submit(() -> {
                    int i = line + 1, target = 0, lightsLen = i;

                    while (data[i] != ']') {
                        if (data[i] == '#') target |= (1 << (i - lightsLen));
                        i++;
                    }
                    //lightsLen = i-lightsLen;

                    ArrayList<Integer> bottoms = new ArrayList<>();
                    while (true) {
                        while (i < n && data[i] != '(' && data[i] != '{') i++;
                        if (i == n || data[i] == '{') break;
                        while (data[i] != '{') {
                            i++;
                            int bottom = 0;
                            while (data[i] != ' ') {
                                byte val = 0;
                                while (data[i] > ',') val = (byte) (val * 10 + (data[i++] - '0'));
                                bottom |= (1 << val);
                                i++;
                            }
                            bottoms.add(bottom);
                            i++;
                        }
                    }

                    sum.addAndGet(Machine.bfs(bottoms, target));

                });
            }

            executor.shutdown();

            return sum.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public long solve2(Path file, int numThreads) {
        try {
            data = Files.readAllBytes(file);
            executor = Executors.newFixedThreadPool(numThreads);
            n = data.length;
            sum = new AtomicInteger(0);
            lines = new ArrayList<>();

            lines.add(0);
            for (int j = 0; j < n; j++) {
                if (data[j] == '\n')
                    lines.add(j + 1);
            }

            for (int line : lines) {
                executor.submit(() -> {
                    int i = line;
                    while (data[i] != '(') i++;

                    List<List<Byte>> bottoms = new ArrayList<>();
                    while (true) {
                        while (i < n && data[i] != '(' && data[i] != '{') i++;
                        if (i == n || data[i] == '{') break;
                        while (data[i] != '{') {
                            i++;
                            List<Byte> bottom = new ArrayList<>();
                            while (data[i] != ' ') {
                                byte val = 0;
                                while (data[i] > ',') val = (byte) (val * 10 + (data[i++] - '0'));
                                bottom.add(val);
                                i++;
                            }
                            bottoms.add(bottom);
                            i++;
                        }
                    }
                    i++;

                    List<Integer> voltajes = new ArrayList<>();
                    while (i < n && data[i] != '\n') {
                        int val = 0;
                        while (data[i] != ',' && data[i] != '}')
                            val = (val * 10 + (data[i++] - '0'));
                        voltajes.add(val);
                        i++;
                    }

                    int numVars = voltajes.size();
                    List<boolean[]> coeffs = new ArrayList<>();
                    for (List<Byte> bottom : bottoms) {
                        boolean[] b = new boolean[numVars];
                        for (Byte idx : bottom) b[idx] = true;
                        coeffs.add(b);
                    }

                    int[] goalArr = new int[numVars];
                    for (int j = 0; j < numVars; j++) goalArr[j] = voltajes.get(j);

                    int minimal = Machine.solveSingle(coeffs, goalArr);

                    sum.addAndGet(minimal);

                });
            }

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);
            return sum.get();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
