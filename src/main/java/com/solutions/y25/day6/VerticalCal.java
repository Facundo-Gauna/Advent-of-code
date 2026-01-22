package com.solutions.y25.day6;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class VerticalCal {

    public long solve2(Path file) {
        try {
            byte[] data = Files.readAllBytes(file);
            int n = data.length, i = n-2, width = 0;
            while (data[width] != '\n') width++;
            width++;
            boolean[] valid = new boolean[64];
            valid[' '] = true;
            valid['*'] = true;
            valid['+'] = true;

            long total = 0;
            while (valid[data[i]]){
                int num_len = i;
                while (data[i] == ' ') i--;
                num_len = num_len-i;
                char op = (char) (data[i]);
                long val = 0;

                for(int k = i+num_len; k>=i; k--){
                    long v = 0;
                    int j = k % width;
                    while (j<k) {
                        if(data[j]!=' ')v = v*10+(data[j]-'0');
                        j += width;
                    }
                    //System.out.println(v);
                    val = (op == '+') ? v+val : Math.max(val,1)*v;
                    //System.out.println("val -> "+val);
                }

                i-=2;
                total += val;
            }

            return total;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long solve1(Path file) {
        try {
            byte[] data = Files.readAllBytes(file);
            int n = data.length, i = n-2 , width = 0;
            while (data[width] != '\n') width++;
            width++;

            long total = 0;
            while (data[i] != '\n'){
                while (data[i] == ' ') i--;
                char op = (char) (data[i]);
                long val = 0;
                for(int k = i-width; k>=0 ; k-=width){
                    long v = 0;
                    int j = k;
                    while (data[j] == ' ') j++;
                    while (data[j] != ' ' && data[j] != '\n') v = v*10+(data[j++]-'0');
                    val = (op == '+') ? v+val : Math.max(val,1)*v;
                }

                i--;
                total += val;
            }

            return total;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        try {
            VerticalCal id = new VerticalCal();
            long start = System.nanoTime();
            long result = id.solve2(Path.of("./src/main/java/com/solutions/y25/day6/data.txt"));
            long end = System.nanoTime();
            System.out.println("Result: " + result);
            System.out.println("Time: " + (end - start) / 1_000_000.0 + " ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
