package com.solutions.y25.day8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Cords3d {

    final int MAX_N_CONNECTIONS = 1000;

    private static class Point {
        int x, y, z;

        Point(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private static class Edge {
        int u, v;
        long w; // distance

        Edge(int u, int v, long w) {
            this.u = u;
            this.v = v;
            this.w = w;
        }
    }

    private static class DSU {
        int[] parent;
        int[] rank;

        DSU(int size) {
            parent = new int[size];
            rank = new int[size];
            for (int i = 0; i < size; i++) {
                parent[i] = i;
                rank[i] = 1;
            }
        }

        int find(int x) {
            if (parent[x] != x) parent[x] = find(parent[x]);
            return parent[x];
        }

        void union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);
            if (rootX == rootY) return;

            if (rank[rootX] > rank[rootY]) {
                parent[rootY] = rootX;
            } else if (rank[rootX] < rank[rootY]) {
                parent[rootX] = rootY;
            } else {
                parent[rootY] = rootX;
                rank[rootX]++;
            }
        }
    }

    //  euclidean distance 3d without square
    private static long distance(Point p1, Point p2) {
        return ((long) (p2.x - p1.x) * (p2.x - p1.x)) + ((long) (p2.y - p1.y) * (p2.y - p1.y)) + (((long) p2.z - p1.z) * (p2.z - p1.z));
    }

    private ArrayList<Point> parseInput(Path file) throws IOException {
        byte[] data = Files.readAllBytes(file);
        int n = data.length, i = 0;
        ArrayList<Point> points = new ArrayList<>();
        // x , y,  z
        while (i < n) {
            int x = 0;
            while (data[i] != ',') x = x * 10 + (data[i++] - '0');
            i++;
            int y = 0;
            while (data[i] != ',') y = y * 10 + (data[i++] - '0');
            i++;
            int z = 0;
            while (i < n && data[i] != '\n') z = z * 10 + (data[i++] - '0');
            i++;
            points.add(new Point(x, y, z));
        }
        return points;
    }

    private ArrayList<Edge> buildEdges(ArrayList<Point> points, int points_size){
        ArrayList<Edge> edges = new ArrayList<>(points_size*points_size);
        for(int i = 0; i<points_size ; i++){
            Point curr = points.get(i);
            for(int j = i+1 ; j<points_size; j++){
                edges.add(new Edge(i,j,distance(curr,points.get(j))));
            }
        }

        edges.sort((a,b) -> a.w < b.w ? -1 : (a.w == b.w ? 0 : 1));
        return edges;
    }

    private void makeUnions(DSU dsu,ArrayList<Edge> edges , int edge_size){
        for(int i = 0; i<edge_size ; i++){
            Edge e = edges.get(i);
            if (dsu.find(e.u) != dsu.find(e.v)) {
                dsu.union(e.u, e.v);
            }
        }
    }

    private ArrayList<Integer> getJunctionBoxesSizes(DSU dsu, int points_size){
        Map<Integer, Integer> treeMap = new HashMap<>();
        for (int i = 0; i < points_size; i++) {
            int root = dsu.find(i);
            treeMap.put(root, treeMap.getOrDefault(root,0) + 1);
        }

        ArrayList<Integer> roots = new ArrayList<>(treeMap.values());
        roots.sort(Comparator.reverseOrder());
        return roots;
    }

    // Solutions ........
    public long solve2(Path file) {
        try {
            ArrayList<Point> points = parseInput(file);

            int points_size = points.size();
            ArrayList<Edge> edges = buildEdges(points,points_size);

            DSU dsu = new DSU(points_size);

            int rest_connections = points_size-1;
            long total = 0;
            for (Edge e : edges) {
                if (dsu.find(e.u) != dsu.find(e.v)) {
                    if (rest_connections - 1 == 0) {
                        total = (long) points.get(e.u).x * points.get(e.v).x;
                        break;
                    }
                    dsu.union(e.u, e.v);
                    rest_connections--;
                }
            }

            return total;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long solve1(Path file) {
        try {
            ArrayList<Point> points = parseInput(file);

            int points_size = points.size();
            ArrayList<Edge> edges = buildEdges(points,points_size);

            DSU dsu = new DSU(points_size);
            makeUnions(dsu,edges,Math.min(MAX_N_CONNECTIONS,edges.size()));

            ArrayList<Integer> boxesSize = getJunctionBoxesSizes(dsu,points_size);

            long total = 1;
            for(int i = 0 ; i<Math.min(3,boxesSize.size()); i++) total *= boxesSize.get(i);

            return total;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        try {
            Cords3d id = new Cords3d();
            long start = System.nanoTime();
            long result = id.solve2(Path.of("./src/main/java/com/solutions/y25/day8/data.txt"));
            long end = System.nanoTime();
            System.out.println("Result: " + result);
            System.out.println("Time: " + (end - start) / 1_000_000.0 + " ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
