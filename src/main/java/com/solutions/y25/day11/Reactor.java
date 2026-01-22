package com.solutions.y25.day11;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Reactor {

    // parse input, build mapping and adjacency list
    private record Graph(int n, Map<String, Integer> nameToIdx, int[][] adj, int outIdx, int idxFft, int idxDac,
                             int idxYou, int idxSvr) {}

    private Graph buildGraph(Path file) throws IOException {
        String[] lines = Files.readString(file).trim().split("\n");
        Map<String,Integer> nameToIdx = new HashMap<>();
        int n = lines.length;

        for(int i = 0; i<n ; i++){
            String left = lines[i].substring(0,lines[i].trim().indexOf(":"));
            nameToIdx.put(left, i);
        }

        nameToIdx.put("out", n);

        int[][] adj = new int[n+1][];
        for (int i = 0; i < n; i++) {
            String[] toks = lines[i].split(" ");
            int len = toks.length;
            int[] arr = new int[len-1];
            for (int j = 1; j < len; j++)
                arr[j - 1] = nameToIdx.get(toks[j]);
            adj[i] = arr;
        }

        int outIdx = nameToIdx.get("out");
        int idxFft = nameToIdx.getOrDefault("fft", -1);
        int idxDac = nameToIdx.getOrDefault("dac", -1);
        int idxYou = nameToIdx.getOrDefault("you", -1);
        int idxSvr = nameToIdx.getOrDefault("svr", -1);

        return new Graph(n+1, nameToIdx, adj, outIdx, idxFft, idxDac, idxYou, idxSvr);
    }

    // PART 1: memoized count of paths from `start` to out
    private long countPathsPart1(Graph g, int start) {
        final int n = g.n;
        final long[] memo = new long[n];
        Arrays.fill(memo, -1L);

        // DFS with memoization
        class DFS {
            long dfs(int u) {
                if (u == g.outIdx) return 1L;
                if (memo[u] != -1L) return memo[u];
                long sum = 0L;
                for (int v : g.adj[u]) {
                    sum += dfs(v);
                }
                memo[u] = sum;
                return sum;
            }
        }
        return new DFS().dfs(start);
    }

    // PART 2: memoized DP with 2-bit mask (bit0 = visited fft, bit1 = visited dac)
    private long countPathsPart2(Graph g, int start, int fftIdx, int dacIdx) {
        final int n = g.n;
        final long[][] memo = new long[n][4];
        for (int i = 0; i < n; i++) Arrays.fill(memo[i], Long.MIN_VALUE);

        final int[] nodeMask = new int[n];
        if (fftIdx >= 0) nodeMask[fftIdx] |= 1;
        if (dacIdx >= 0) nodeMask[dacIdx] |= 2;

        // dfs(u, maskSoFar) where maskSoFar does NOT yet include u; we'll include it at entry
        class DFS {
            long dfs(int u, int maskSoFar) {
                int mask = maskSoFar | nodeMask[u];
                if (memo[u][mask] != Long.MIN_VALUE) return memo[u][mask];
                if (u == g.outIdx) {
                    memo[u][mask] = (mask == 3) ? 1L : 0L;
                    return memo[u][mask];
                }
                long sum = 0L;
                for (int v : g.adj[u]) {
                    sum += dfs(v, mask);
                }
                memo[u][mask] = sum;
                return sum;
            }
        }
        return new DFS().dfs(start, 0);
    }


    public long solve1(Path file) {
        try {
            Graph g = buildGraph(file);
            if (g.idxYou == -1) throw new IllegalStateException("'you' not found in input");
            return countPathsPart1(g, g.idxYou);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long solve2(Path file) {
        try {
            Graph g = buildGraph(file);
            if (g.idxSvr == -1) throw new IllegalStateException("'svr' not found in input");
            if (g.idxFft == -1 || g.idxDac == -1) throw new IllegalStateException("'fft' or 'dac' not found in input");
            return countPathsPart2(g, g.idxSvr, g.idxFft, g.idxDac);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Reactor t = new Reactor();
        try {
            Path p = Path.of("./src/main/java/com/solutions/y25/day11/data.txt");
            long start = System.nanoTime();
            long r1 = t.solve1(p);
            long r2 = t.solve2(p);
            long end = System.nanoTime();
            System.out.println("Part1: " + r1);
            System.out.println("Part2: " + r2);
            System.out.println("Time ms: " + (end - start) / 1_000_000.0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
