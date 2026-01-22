package com.solutions.y25.day9;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Tile {
    private static class Int2MinMax {
        private final HashMap<Integer, int[]> map = new HashMap<>();

        void put(int col, int row) {
            int[] mm = map.get(col);
            if (mm == null) {
                map.put(col, new int[]{row, row});
            } else {
                if (row < mm[0]) mm[0] = row;
                if (row > mm[1]) mm[1] = row;
            }
        }

        int size() {
            return map.size();
        }

        int[] keys() {
            int s = map.size();
            int[] k = new int[s];
            int idx = 0;
            for (Integer key : map.keySet()) k[idx++] = key;
            Arrays.sort(k);
            return k;
        }

        int[] mins(int[] k) {
            int[] res = new int[k.length];
            for (int i = 0; i < k.length; i++) res[i] = map.get(k[i])[0];
            return res;
        }

        int[] maxs(int[] k) {
            int[] res = new int[k.length];
            for (int i = 0; i < k.length; i++) res[i] = map.get(k[i])[1];
            return res;
        }
    }

    public long solve1(Path file) {
        try {
            byte[] data = Files.readAllBytes(file);
            int n = data.length, i = 0;
            Int2MinMax map = new Int2MinMax();

            while (i < n) {
                int col = 0;
                while (i < n && data[i] != ',') col = col * 10 + (data[i++] - '0');
                i++; // skip ','
                int row = 0;
                while (i < n && data[i] != '\n') row = row * 10 + (data[i++] - '0');
                while (i < n && data[i] < '0') i++; // ASCII values before '0'

                map.put(col, row);
            }

            int m = map.size();
            int[] cols = map.keys();
            int[] minY = map.mins(cols);
            int[] maxY = map.maxs(cols);

            long maxArea = 0L;

            // Iterate over all pairs of different columns
            for (int a = 0; a < m; a++) {
                int x1 = cols[a];
                int min1 = minY[a];
                int max1 = maxY[a];

                long heightSame = max1 - min1 + 1L;
                maxArea = Math.max(maxArea, heightSame); // width = 1

                for (int b = a + 1; b < m; b++) {
                    int x2 = cols[b];
                    int min2 = minY[b];
                    int max2 = maxY[b];

                    long width = (long) Math.abs(x1 - x2) + 1L;
                    long height = Math.max(Math.abs(max1 - min2), Math.abs(max2 - min1)) + 1L;
                    long area = width * height;
                    if (area > maxArea) maxArea = area;
                }
            }

            return maxArea;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /// Solution part 2, i failed to solve it alone, i read another solutions
    /// and i need to use IA to understand a solve the problem.

    record Point(int y, int x) {
    }

    static final long MAX_CELLS_SAFE = 50_000_000L;

    private List<Point> parsePoints(Path p) throws IOException {
        byte[] data = Files.readAllBytes(p);
        List<Point> points = new ArrayList<>(512);
        int n = data.length, i = 0;

        while (i < n) {
            int col = 0;
            while (i < n && data[i] != ',') col = col * 10 + (data[i++] - '0');
            i++;
            int row = 0;
            while (i < n && data[i] != '\n') row = row * 10 + (data[i++] - '0');
            while (i < n && data[i] < '0') i++;
            points.add(new Point(col, row));
        }

        return points;
    }

    public long solve2(Path file) {
        try {
            List<Point> reds = parsePoints(file);

            // 1) Collect coordinates for compression.
            // We add endpoints and endpoint+1 for correct interval representation.
            TreeSet<Integer> xsSet = new TreeSet<>();
            TreeSet<Integer> ysSet = new TreeSet<>();

            int n = reds.size();

            // Add each red coordinate and its +1 sentinel
            for (Point p : reds) {
                xsSet.add(p.x);
                xsSet.add(p.x + 1);
                ysSet.add(p.y);
                ysSet.add(p.y + 1);
            }

            // Add coordinates used by segments between consecutive red points
            for (int i = 0; i < n; i++) {
                Point a = reds.get(i);
                Point b = reds.get((i + 1) % n);
                if (a.x == b.x) {
                    // Vertical segment: covers y in inclusive [minY..maxY]
                    int y1 = Math.min(a.y, b.y);
                    int y2 = Math.max(a.y, b.y);
                    ysSet.add(y1);
                    ysSet.add(y2 + 1);
                    xsSet.add(a.x);
                    xsSet.add(a.x + 1);
                } else if (a.y == b.y) {
                    // Horizontal segment
                    int x1 = Math.min(a.x, b.x);
                    int x2 = Math.max(a.x, b.x);
                    xsSet.add(x1);
                    xsSet.add(x2 + 1);
                    ysSet.add(a.y);
                    ysSet.add(a.y + 1);
                } else {
                    // Diagonal segments are not allowed, but this guards against invalid input
                    throw new IllegalArgumentException("Consecutive red tiles must be aligned horizontally or vertically.");
                }
            }

            // Convert sets to sorted lists
            List<Integer> xs = new ArrayList<>(xsSet);
            List<Integer> ys = new ArrayList<>(ysSet);
            int W = xs.size() - 1; // number of x intervals
            int H = ys.size() - 1; // number of y intervals

            if (W <= 0 || H <= 0) {
                System.out.println("0");
                return 0;
            }
            if ((long) W * H > MAX_CELLS_SAFE) {
                System.out.println("Too big");
                return 0;
            }

            // Mapping original coordinate -> interval index (0..W-1 / 0..H-1)
            // indexOf returns k such that xs[k] <= coord < xs[k+1]
            record Mapper(List<Integer> vals) {
                int indexOf(int coord) {
                    int lo = 0, hi = vals.size() - 1;
                    while (lo <= hi) {
                        int mid = (lo + hi) >>> 1;
                        if (vals.get(mid) <= coord) lo = mid + 1;
                        else hi = mid - 1;
                    }
                    return Math.max(0, Math.min(vals.size() - 2, hi));
                }
            }

            Mapper mapX = new Mapper(xs);
            Mapper mapY = new Mapper(ys);

            // 2) Create grid using byte[][] to minimize memory.
            // Values:
            // 0 = unknown / invalid
            // 1 = GREEN
            // 2 = RED
            byte[][] grid = new byte[H][W];

            // 3) Draw segments (green) and mark red points
            for (int i = 0; i < n; i++) {
                Point a = reds.get(i);
                Point b = reds.get((i + 1) % n);
                if (a.x == b.x) {
                    int ix = mapX.indexOf(a.x);
                    int y1 = Math.min(a.y, b.y);
                    int y2 = Math.max(a.y, b.y);
                    int iy1 = mapY.indexOf(y1);
                    int iy2 = mapY.indexOf(y2);
                    for (int iy = iy1; iy <= iy2; iy++) {
                        grid[iy][ix] = 1; // GREEN
                    }
                } else {
                    int iy = mapY.indexOf(a.y);
                    int x1 = Math.min(a.x, b.x);
                    int x2 = Math.max(a.x, b.x);
                    int ix1 = mapX.indexOf(x1);
                    int ix2 = mapX.indexOf(x2);
                    for (int ix = ix1; ix <= ix2; ix++) {
                        grid[iy][ix] = 1; // GREEN
                    }
                }
            }

            // Mark red cells (point locations)
            for (Point p : reds) {
                int ix = mapX.indexOf(p.x);
                int iy = mapY.indexOf(p.y);
                grid[iy][ix] = 2;
            }

            // 4) Flood fill from the boundary to mark exterior invalid cells
            boolean[][] visited = new boolean[H][W];
            ArrayDeque<int[]> dq = new ArrayDeque<>();

            // Push boundary cells that are currently unknown (0)
            for (int ix = 0; ix < W; ix++) {
                if (grid[0][ix] == 0 && !visited[0][ix]) {
                    visited[0][ix] = true;
                    dq.add(new int[]{0, ix});
                }
                if (grid[H - 1][ix] == 0 && !visited[H - 1][ix]) {
                    visited[H - 1][ix] = true;
                    dq.add(new int[]{H - 1, ix});
                }
            }
            for (int iy = 0; iy < H; iy++) {
                if (grid[iy][0] == 0 && !visited[iy][0]) {
                    visited[iy][0] = true;
                    dq.add(new int[]{iy, 0});
                }
                if (grid[iy][W - 1] == 0 && !visited[iy][W - 1]) {
                    visited[iy][W - 1] = true;
                    dq.add(new int[]{iy, W - 1});
                }
            }

            final int[][] dirs = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            while (!dq.isEmpty()) {
                int[] cur = dq.poll();
                int y = cur[0], x = cur[1];
                for (int[] d : dirs) {
                    int ny = y + d[0], nx = x + d[1];
                    if (ny < 0 || ny >= H || nx < 0 || nx >= W) continue;
                    if (visited[ny][nx]) continue;
                    if (grid[ny][nx] == 0) {
                        visited[ny][nx] = true;
                        dq.add(new int[]{ny, nx});
                    }
                }
            }

            // 5) Any cell not visited and not RED is interior and becomes GREEN
            for (int iy = 0; iy < H; iy++) {
                for (int ix = 0; ix < W; ix++) {
                    if (!visited[iy][ix] && grid[iy][ix] != 2) {
                        grid[iy][ix] = 1;
                    }
                }
            }

            // 6) Build prefix sum array: bad = 1 if cell is invalid (visited), else 0
            int[][] badPrefix = new int[H + 1][W + 1];
            for (int iy = 0; iy < H; iy++) {
                int rowSum = 0;
                for (int ix = 0; ix < W; ix++) {
                    int bad = visited[iy][ix] ? 1 : 0;
                    rowSum += bad;
                    badPrefix[iy + 1][ix + 1] = badPrefix[iy][ix + 1] + rowSum;
                }
            }

            // 7) Map each red point to compressed indices and store original coordinates
            int R = reds.size();
            int[] redIx = new int[R], redIy = new int[R], redX = new int[R], redY = new int[R];
            for (int i = 0; i < R; i++) {
                Point p = reds.get(i);
                redX[i] = p.x;
                redY[i] = p.y;
                redIx[i] = mapX.indexOf(p.x);
                redIy[i] = mapY.indexOf(p.y);
            }

            // 8) For each pair of red points, check rectangle validity using prefix sums
            long maxArea = 0L;

            // If there are many red points, O(R^2) may be slow
            for (int i = 0; i < R; i++) {
                for (int j = i + 1; j < R; j++) {
                    if (redX[i] == redX[j] || redY[i] == redY[j]) continue;

                    int lx = Math.min(redIx[i], redIx[j]);
                    int rx = Math.max(redIx[i], redIx[j]);
                    int ly = Math.min(redIy[i], redIy[j]);
                    int ry = Math.max(redIy[i], redIy[j]);

                    int badCount =
                            badPrefix[ry + 1][rx + 1]
                                    - badPrefix[ly][rx + 1]
                                    - badPrefix[ry + 1][lx]
                                    + badPrefix[ly][lx];

                    if (badCount == 0) {
                        long area =
                                (long) (Math.abs(redX[i] - redX[j]) + 1) *
                                        (Math.abs(redY[i] - redY[j]) + 1);
                        if (area > maxArea) maxArea = area;
                    }
                }
            }

            return maxArea;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        try {
            Tile t = new Tile();
            long start = System.nanoTime();
            long result = t.solve2(Path.of("./src/main/java/com/solutions/y25/day9/data.txt"));
            long end = System.nanoTime();
            System.out.println("Result: " + result);
            System.out.println("Time: " + (end - start) / 1_000_000.0 + " ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
