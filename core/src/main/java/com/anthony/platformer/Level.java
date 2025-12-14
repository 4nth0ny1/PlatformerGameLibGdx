package com.anthony.platformer;

public class Level {

    // Size in tiles
    private final int rows;
    private final int cols;

    // Tile data: 0 = empty, 1 = solid, 2 = red door
    private final int[][] tiles;

    // Tile size in pixels
    private final int tileSize;

    // Where the player should spawn in this level (world coordinates, pixels)
    private final float spawnX;
    private final float spawnY;

    public Level(String[] layout, int tileSize, int spawnRow, int spawnCol) {
        if (layout == null || layout.length == 0) {
            throw new IllegalArgumentException("Layout cannot be null or empty");
        }

        this.tileSize = tileSize;

        this.rows = layout.length;
        this.cols = layout[0].length();

        this.tiles = new int[rows][cols];

        int rowIndex = 0;
        while (rowIndex < rows) {
            String line = layout[rowIndex];

            // Optional safety: all lines must have same length
            if (line.length() != cols) {
                throw new IllegalStateException(
                    "Line " + rowIndex + " length (" + line.length() + ") does not match expected cols (" + cols + ")"
                );
            }

            int colIndex = 0;
            while (colIndex < cols) {
                char c = line.charAt(colIndex);

                if (c == '#') {
                    tiles[rowIndex][colIndex] = 1; // grass
                } else if (c == 'd') {
                    tiles[rowIndex][colIndex] = 4; // dirt
                } else if (c == 'r') {
                    tiles[rowIndex][colIndex] = 5; // right top grass
                } else if (c == 'l') {
                    tiles[rowIndex][colIndex] = 6; // left top grass
                } else if (c == 's') {
                    tiles[rowIndex][colIndex] = 7; // left dirt
                } else if (c == 'f') {
                    tiles[rowIndex][colIndex] = 8; // right dirt
                } else if (c == 'b') {
                    tiles[rowIndex][colIndex] = 9; // bottom dirt
                } else if (c == 'q') {
                    tiles[rowIndex][colIndex] = 10; // right bottom dirt
                } else if (c == 'w') {
                    tiles[rowIndex][colIndex] = 11; // left bottom dirt
                } else if (c == '?') {
                    tiles[rowIndex][colIndex] = 2; // red door
                } else if (c == '>') {
                    tiles[rowIndex][colIndex] = 3; // aqua door
                } else {
                    tiles[rowIndex][colIndex] = 0; // empty
                }

                colIndex = colIndex + 1;
            }

            rowIndex = rowIndex + 1;
        }

        // Convert spawn tile position to world pixels.
        // This uses the same logic you already had:
        // x = col * tileSize
        // y = (row * tileSize) + tileSize (stand on top of the tile)
        this.spawnX = spawnCol * tileSize;
        this.spawnY = (spawnRow * tileSize) + tileSize;
    }

    // -------- Getters --------

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getTileSize() {
        return tileSize;
    }

    public float getSpawnX() {
        return spawnX;
    }

    public float getSpawnY() {
        return spawnY;
    }

    public int getTile(int row, int col) {
        if (row < 0 || row >= rows) {
            return 0;
        }
        if (col < 0 || col >= cols) {
            return 0;
        }
        return tiles[row][col];
    }

    public boolean isSolidTile(int col, int row) {
        int value = getTile(row, col);
        if (value == 1 || value == 5 || value == 6 || value == 7) {
            return true;
        }
        return false;
    }

    public boolean isDoorTile(int col, int row) {
        int value = getTile(row, col);
        return value == 2;
    }

    public boolean isAquaDoorTile(int col, int row) {
        int value = getTile(row, col);
        return value == 3;
    }
}
