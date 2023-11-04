package edu.purdue.cs.fast;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class DrawCircleTest {

    @Test
    public void drawCircle() {
        int[][] grid = new int[20][20];
        int x = 10;
        int y = 10;
        int r = 5;

        for (int i = -r - 1; i <= r; i++) {
            for (int j = -r - 1; j <= r; j++) {
                int d = i * i + j * j;
                if (d < r * r)
                    grid[y + i][x + j] = 1;
            }
        }
        printGrid(grid);
    }

    private static void printGrid(int[][] grid) {
        for (int[] ints : grid) {
            System.out.println(Arrays.toString(ints));
        }
    }

}
