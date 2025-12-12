package com.anthony.platformer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class PlatformerGame extends ApplicationAdapter {

    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;

    // ---------------- TILE & WORLD SETTINGS ----------------

    private static final int TILE_SIZE = 16;

    private Level currentLevel;

    private int worldWidthPixels;
    private int worldHeightPixels;

    private int currentLevelNumber = 1;
    private boolean wasTouchingDoorLastFrame = false;
    private boolean wasTouchingAquaDoorLastFrame = false;


    // ---------------- PLAYER SETTINGS ----------------

    private float playerWidth = 16f;
    private float playerHeight = 16f;

    private float playerX;
    private float playerY;

    private float moveSpeed = 150f;
    private float gravity = -800f;
    private float velocityY = 0f;
    private float jumpVelocity = 300f;

    // Double jump
    private int maxJumps = 2;
    private int jumpsUsed = 0;
    private boolean isOnGround = false;





    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();

        // Start with level 1
        currentLevelNumber = 1;
        currentLevel = createLevel1();
        applyCurrentLevelSettings();

        // Camera
        float viewportWidth = 400f;
        float viewportHeight = 240f;

        camera = new OrthographicCamera(viewportWidth, viewportHeight);
        camera.position.set(viewportWidth / 2f, viewportHeight / 2f, 0f);
        camera.update();
    }


    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        updatePlayer(deltaTime);
        updateCamera();

        float red = 0.05f;
        float green = 0.05f;
        float blue = 0.18f;
        float alpha = 1.0f;

        Gdx.gl.glClearColor(red, green, blue, alpha);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawTiles();
        drawPlayer();
        shapeRenderer.end();
    }

    // ----------------------- LEVEL BUILDING -----------------------

    private Level createLevel1() {
        return new Level(Levels.LEVEL_1, TILE_SIZE, 55, 2);
    }

    private Level createLevel2() {
        return new Level(Levels.LEVEL_2, TILE_SIZE, 55, 2);
    }

    private Level createLevel3() {
        return new Level(Levels.LEVEL_3, TILE_SIZE, 5, 2);
    }

    private Level createLevel4() {
        return new Level(Levels.LEVEL_4, TILE_SIZE, 5, 2);
    }

    private Level createLevel5() {
        return new Level(Levels.LEVEL_5, TILE_SIZE, 5, 2);
    }

    private Level createLevel6() {
        return new Level(Levels.LEVEL_6, TILE_SIZE, 5, 2);
    }

    // Utility you can reuse after switching levels
    private void applyCurrentLevelSettings() {
        worldWidthPixels = currentLevel.getCols() * TILE_SIZE;
        worldHeightPixels = currentLevel.getRows() * TILE_SIZE;

        playerX = currentLevel.getSpawnX();
        playerY = currentLevel.getSpawnY();
        velocityY = 0f;
    }




    // ----------------------- UPDATE LOGIC -----------------------

    private void updatePlayer(float deltaTime) {
        // Horizontal movement
        float deltaX = 0f;

        boolean leftPressed = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean rightPressed = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

        if (leftPressed) {
            deltaX = deltaX - moveSpeed * deltaTime;
        }
        if (rightPressed) {
            deltaX = deltaX + moveSpeed * deltaTime;
        }

        if (deltaX != 0f) {
            moveHorizontal(deltaX);
        }

        // Jump / double jump
        handleJumpInput();

        // Gravity
        velocityY = velocityY + gravity * deltaTime;

        // Vertical movement
        float deltaY = velocityY * deltaTime;

        if (deltaY != 0f) {
            moveVertical(deltaY);
        }

        // Keep inside world bounds horizontally
        if (playerX < 0f) {
            playerX = 0f;
        }
        if (playerX + playerWidth > worldWidthPixels) {
            playerX = worldWidthPixels - playerWidth;
        }

        // Prevent falling below bottom of world
        if (playerY < 0f) {
            playerY = 0f;
            velocityY = 0f;
            isOnGround = true;
            jumpsUsed = 0;
        }

        boolean touchingDoorNow = isTouchingRedDoor();
        boolean touchingAquaDoorNow = isTouchingAquaDoor();


        if (touchingDoorNow && !wasTouchingDoorLastFrame) {
            System.out.println("Collision with red door.");

            if (currentLevelNumber == 1) {
                currentLevelNumber = 2;
                currentLevel = createLevel2();
                applyCurrentLevelSettings();
            } else if (currentLevelNumber == 2) {
                currentLevelNumber = 3;
                currentLevel = createLevel3();
                applyCurrentLevelSettings();
            } else if (currentLevelNumber == 3) {
                currentLevelNumber = 1;
                currentLevel = createLevel1();
                applyCurrentLevelSettings();
            }
        } else if (touchingAquaDoorNow && !wasTouchingAquaDoorLastFrame) {
            System.out.println("Collision with aqua door.");

            if (currentLevelNumber == 3) {
                currentLevelNumber = 4;
                currentLevel = createLevel4();
                applyCurrentLevelSettings();
            } else if (currentLevelNumber == 4) {
                currentLevelNumber = 5;
                currentLevel = createLevel5();
                applyCurrentLevelSettings();
            } else if (currentLevelNumber == 5) {
                currentLevelNumber = 6;
                currentLevel = createLevel6();
                applyCurrentLevelSettings();
            } else if (currentLevelNumber == 6) {
                currentLevelNumber = 1;
                currentLevel = createLevel1();
                applyCurrentLevelSettings();
            }
        }
        wasTouchingDoorLastFrame = touchingDoorNow;
        wasTouchingAquaDoorLastFrame = touchingAquaDoorNow;

    }



    private boolean isTouchingAquaDoor() {
        float playerLeft = playerX;
        float playerRight = playerX + playerWidth;
        float playerBottom = playerY;
        float playerTop = playerY + playerHeight;

        int rows = currentLevel.getRows();
        int cols = currentLevel.getCols();
        int tileSize = currentLevel.getTileSize();

        int row = 0;
        while (row < rows) {
            int col = 0;
            while (col < cols) {

                if (currentLevel.isAquaDoorTile(col, row)) {
                    float x = col * tileSize;
                    float y = row * tileSize;

                    float tileLeft = x;
                    float tileRight = x + tileSize;
                    float tileBottom = y;
                    float tileTop = y + tileSize;

                    boolean overlapX = playerRight > tileLeft && playerLeft < tileRight;
                    boolean overlapY = playerTop > tileBottom && playerBottom < tileTop;

                    if (overlapX && overlapY) {
                        return true;
                    }
                }

                col = col + 1;
            }
            row = row + 1;
        }

        return false;
    }

    private boolean isTouchingRedDoor() {
        float playerLeft = playerX;
        float playerRight = playerX + playerWidth;
        float playerBottom = playerY;
        float playerTop = playerY + playerHeight;

        int rows = currentLevel.getRows();
        int cols = currentLevel.getCols();
        int tileSize = currentLevel.getTileSize();

        int row = 0;
        while (row < rows) {
            int col = 0;
            while (col < cols) {

                if (currentLevel.isDoorTile(col, row)) {
                    float x = col * tileSize;
                    float y = row * tileSize;

                    float tileLeft = x;
                    float tileRight = x + tileSize;
                    float tileBottom = y;
                    float tileTop = y + tileSize;

                    boolean overlapX = playerRight > tileLeft && playerLeft < tileRight;
                    boolean overlapY = playerTop > tileBottom && playerBottom < tileTop;

                    if (overlapX && overlapY) {
                        return true;
                    }
                }

                col = col + 1;
            }
            row = row + 1;
        }

        return false;
    }

    private void handleJumpInput() {
        boolean jumpPressedThisFrame = Gdx.input.isKeyJustPressed(Input.Keys.SPACE);

        if (jumpPressedThisFrame) {
            if (jumpsUsed < maxJumps) {
                velocityY = jumpVelocity;
                jumpsUsed = jumpsUsed + 1;
                isOnGround = false;
            }
        }
    }

    private void moveHorizontal(float deltaX) {
        float newX = playerX + deltaX;
        playerX = newX;
        resolveHorizontalCollisions(deltaX);
    }

    private void moveVertical(float deltaY) {
        float newY = playerY + deltaY;
        playerY = newY;

        isOnGround = false;
        resolveVerticalCollisions(deltaY);
    }

    private void resolveHorizontalCollisions(float deltaX) {
        float playerLeft = playerX;
        float playerRight = playerX + playerWidth;
        float playerBottom = playerY;
        float playerTop = playerY + playerHeight;

        int minTileX = (int) (playerLeft / TILE_SIZE);
        int maxTileX = (int) (playerRight / TILE_SIZE);
        int minTileY = (int) (playerBottom / TILE_SIZE);
        int maxTileY = (int) (playerTop / TILE_SIZE);

        int tileY = minTileY;
        while (tileY <= maxTileY) {
            int tileX = minTileX;
            while (tileX <= maxTileX) {
                if (!isSolidTile(tileX, tileY)) {
                    tileX = tileX + 1;
                    continue;
                }

                float tileWorldX = tileX * TILE_SIZE;
                float tileWorldY = tileY * TILE_SIZE;

                float tileLeft = tileWorldX;
                float tileRight = tileWorldX + TILE_SIZE;
                float tileBottom = tileWorldY;
                float tileTop = tileWorldY + TILE_SIZE;

                boolean overlapX = playerRight > tileLeft && playerLeft < tileRight;
                boolean overlapY = playerTop > tileBottom && playerBottom < tileTop;

                if (overlapX && overlapY) {
                    if (deltaX > 0f) {
                        playerX = tileLeft - playerWidth;
                    } else if (deltaX < 0f) {
                        playerX = tileRight;
                    }

                    playerLeft = playerX;
                    playerRight = playerX + playerWidth;
                }

                tileX = tileX + 1;
            }
            tileY = tileY + 1;
        }
    }

    private void resolveVerticalCollisions(float deltaY) {
        float playerLeft = playerX;
        float playerRight = playerX + playerWidth;
        float playerBottom = playerY;
        float playerTop = playerY + playerHeight;

        int minTileX = (int) (playerLeft / TILE_SIZE);
        int maxTileX = (int) (playerRight / TILE_SIZE);
        int minTileY = (int) (playerBottom / TILE_SIZE);
        int maxTileY = (int) (playerTop / TILE_SIZE);

        int tileY = minTileY;
        while (tileY <= maxTileY) {
            int tileX = minTileX;
            while (tileX <= maxTileX) {
                if (!isSolidTile(tileX, tileY)) {
                    tileX = tileX + 1;
                    continue;
                }

                float tileWorldX = tileX * TILE_SIZE;
                float tileWorldY = tileY * TILE_SIZE;

                float tileLeft = tileWorldX;
                float tileRight = tileWorldX + TILE_SIZE;
                float tileBottom = tileWorldY;
                float tileTop = tileWorldY + TILE_SIZE;

                boolean overlapX = playerRight > tileLeft && playerLeft < tileRight;
                boolean overlapY = playerTop > tileBottom && playerBottom < tileTop;

                if (overlapX && overlapY) {
                    if (deltaY > 0f) {
                        // moving up, hit head
                        playerY = tileBottom - playerHeight;
                        velocityY = 0f;
                    } else if (deltaY < 0f) {
                        // moving down, landed
                        playerY = tileTop;
                        velocityY = 0f;
                        isOnGround = true;
                        jumpsUsed = 0;
                    }

                    playerBottom = playerY;
                    playerTop = playerY + playerHeight;
                }

                tileX = tileX + 1;
            }
            tileY = tileY + 1;
        }
    }

    private boolean isSolidTile(int tileX, int tileY) {
        return currentLevel.isSolidTile(tileX, tileY);
    }

    // ----------------------- CAMERA & DRAWING -----------------------

    private void updateCamera() {
        float camX = playerX + playerWidth / 2f;
        float camY = playerY + playerHeight / 2f;

        float halfW = camera.viewportWidth / 2f;
        float halfH = camera.viewportHeight / 2f;

        if (camX < halfW) {
            camX = halfW;
        }
        if (camX > worldWidthPixels - halfW) {
            camX = worldWidthPixels - halfW;
        }

        if (camY < halfH) {
            camY = halfH;
        }
        if (camY > worldHeightPixels - halfH) {
            camY = worldHeightPixels - halfH;
        }

        camera.position.set(camX, camY, 0f);
    }

    private void drawTiles() {
        int rows = currentLevel.getRows();
        int cols = currentLevel.getCols();
        int tileSize = currentLevel.getTileSize();

        int row = 0;
        while (row < rows) {
            int col = 0;
            while (col < cols) {
                int tile = currentLevel.getTile(row, col);

                if (tile == 1) {
                    float x = col * tileSize;
                    float y = row * tileSize;

                    shapeRenderer.setColor(0.2f, 0.8f, 0.2f, 1f);
                    shapeRenderer.rect(x, y, tileSize, tileSize);
                } else if (tile == 2) {
                    float x = col * tileSize;
                    float y = row * tileSize;

                    shapeRenderer.setColor(1f, 0f, 0f, 1f);
                    shapeRenderer.rect(x, y, tileSize, tileSize);
                } else if (tile == 3) {
                    float x = col * tileSize;
                    float y = row * tileSize;

                    shapeRenderer.setColor(0f, 0.5f, 0.5f, 1f);
                    shapeRenderer.rect(x, y, tileSize, tileSize);
                }

                col = col + 1;
            }
            row = row + 1;
        }
    }


    private void drawPlayer() {
        shapeRenderer.setColor(1f, 1f, 1f, 1f);
        shapeRenderer.rect(playerX, playerY, playerWidth, playerHeight);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
