package com.anthony.platformer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class PlatformerGame extends ApplicationAdapter {

    private SpriteBatch batch;
    private Texture playerSheetTexture;
    private Texture plainsSheetTexture;
    private Texture grassTexture;
    private Texture dirtTexture;
    private Texture rightTopGrassTexture;
    private Texture leftTopGrassTexture;
    private Texture leftDirtTexture;
    private Texture rightDirtTexture;
    private Texture bottomDirtTexture;
    private Texture leftBottomDirtTexture;
    private Texture rightBottomDirtTexture;
    private float spriteFootOffset = 42f; // pixels inside the 48x48 frame (tweak)

    private Animation<TextureRegion> walkRightAnimation;
    private Animation<TextureRegion> walkLeftAnimation;

    private float stateTimeSeconds;


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

    private boolean facingRight = true;
    private boolean isMoving = false;

    private static final int FRAME_WIDTH = 48;
    private static final int FRAME_HEIGHT = 48;
    private static final int WALK_ROW = 1;
    private static final int WALK_FRAMES = 6;
    private float drawWidth;
    private float drawHeight;

    // Double jump
    private int maxJumps = 2;
    private int jumpsUsed = 0;
    private boolean isOnGround = false;

    // ---------------- PLAINS TILE REGIONS ----------------

    private TextureRegion[][] plainsGrid;

    // Pick a chunk from plains.png to use for "ground"
    // plains.png in your screenshot is 96x192, so at 48x48 it becomes 4 rows x 2 cols.
    private TextureRegion groundRegion;
    private TextureRegion redDoorRegion;
    private TextureRegion aquaDoorRegion;



    @Override
    public void create() {
        batch = new SpriteBatch();

        playerSheetTexture = new Texture("player.png");
        plainsSheetTexture = new Texture("plains.png");
        grassTexture = new Texture("top-grass.png");
        dirtTexture = new Texture("dirt.png");
        rightTopGrassTexture = new Texture("right-top-grass.png");
        leftTopGrassTexture = new Texture("left-top-grass.png");
        leftDirtTexture = new Texture("left-dirt.png");
        rightDirtTexture = new Texture("right-dirt.png");
        bottomDirtTexture = new Texture("bottom-dirt.png");
        rightBottomDirtTexture = new Texture("right-bottom-dirt.png");
        leftBottomDirtTexture = new Texture("left-bottom-dirt.png");

        TextureRegion[][] grid = TextureRegion.split(playerSheetTexture, FRAME_WIDTH, FRAME_HEIGHT);

        // Player sheet split
        TextureRegion[][] playerGrid = TextureRegion.split(playerSheetTexture, FRAME_WIDTH, FRAME_HEIGHT);

        // Plains sheet split (48x48 chunks)
        plainsGrid = TextureRegion.split(plainsSheetTexture, FRAME_WIDTH, FRAME_HEIGHT);

        // Build WALK RIGHT frames from the chosen row
        TextureRegion[] walkRightFrames = new TextureRegion[WALK_FRAMES];
        for (int col = 0; col < WALK_FRAMES; col++) {
            walkRightFrames[col] = grid[WALK_ROW][col];
        }

        // Build WALK LEFT frames by flipping copies of the right frames
        TextureRegion[] walkLeftFrames = new TextureRegion[WALK_FRAMES];
        for (int i = 0; i < WALK_FRAMES; i++) {
            TextureRegion copy = new TextureRegion(walkRightFrames[i]);
            copy.flip(true, false);
            walkLeftFrames[i] = copy;
        }

        // ---------------- PLAINS REGIONS ----------------
        // Change these indices to whatever chunk you actually want:
        // rows: 0..3 (top to bottom), cols: 0..1 (left to right)
        groundRegion = plainsGrid[2][0];    // example: bottom-left chunk
        redDoorRegion = plainsGrid[2][1];   // example: some other chunk
        aquaDoorRegion = plainsGrid[1][1];  // example: some other chunk

        playerWidth = 16f;
        playerHeight = 20f;
        drawWidth = 72f;
        drawHeight = 72f;

        float frameDurationSeconds = 0.10f; // tweak for faster/slower walk
        walkRightAnimation = new Animation<TextureRegion>(frameDurationSeconds, walkRightFrames);
        walkLeftAnimation = new Animation<TextureRegion>(frameDurationSeconds, walkLeftFrames);

        walkRightAnimation.setPlayMode(Animation.PlayMode.LOOP);
        walkLeftAnimation.setPlayMode(Animation.PlayMode.LOOP);

        playerX = 100f;
        playerY = 100f;

        stateTimeSeconds = 0f;

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
        isOnGround = false;

        float deltaTime = Gdx.graphics.getDeltaTime();
        stateTimeSeconds += deltaTime;

        updatePlayer(deltaTime);
        updateCamera();

        float red = 0.05f;
        float green = 0.05f;
        float blue = 0.18f;
        float alpha = 1.0f;

        Gdx.gl.glClearColor(red, green, blue, alpha);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        camera.update();
//        shapeRenderer.setProjectionMatrix(camera.combined);
//
//        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//        drawTiles();
//        shapeRenderer.end();
//
//        batch.setProjectionMatrix(camera.combined);
//
//        batch.begin();
//        TextureRegion currentFrame = getCurrentPlayerFrame();
//
//        float drawX = playerX - (drawWidth - playerWidth) / 2f;     // center sprite on the box
//        float drawY = playerY - (drawHeight - playerHeight) + spriteFootOffset;        // drop sprite so feet touch ground
//        batch.draw(currentFrame, drawX, drawY, drawWidth, drawHeight);
//        batch.end();

        // --- DRAW WORLD (TEXTURES) ---
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawTilesWithTextures();
        drawPlayerWithBatch();
        batch.end();

        /* ---------- SHAPES (DOORS) ---------- */
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawDoorShapes();
        shapeRenderer.end();
    }


    private void drawPlayerWithBatch() {
        TextureRegion currentFrame = getCurrentPlayerFrame();

        float drawX = playerX - (drawWidth - playerWidth) / 2f;
        float drawY = playerY - (drawHeight - playerHeight) + spriteFootOffset;

        batch.draw(currentFrame, drawX, drawY, drawWidth, drawHeight);
    }

    private void drawPlayerDebugBox() {
        shapeRenderer.rect(playerX, playerY, playerWidth, playerHeight);
    }

    private void updateMovement(float deltaSeconds) {
        isMoving = false;

        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            playerX -= moveSpeed * deltaSeconds;
            facingRight = false;
            isMoving = true;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            playerX += moveSpeed * deltaSeconds;
            facingRight = true;
            isMoving = true;
        }
    }

    private TextureRegion getCurrentPlayerFrame() {
        if (!isMoving) {
            // For now: just show first walk frame as "idle"
            // Later we can plug in real idle frames from the sheet.
            if (facingRight) {
                return walkRightAnimation.getKeyFrames()[0];
            } else {
                return walkLeftAnimation.getKeyFrames()[0];
            }
        }

        if (facingRight) {
            return walkRightAnimation.getKeyFrame(stateTimeSeconds, true);
        } else {
            return walkLeftAnimation.getKeyFrame(stateTimeSeconds, true);
        }
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

        isMoving = false;




        boolean leftPressed = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean rightPressed = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

        if (leftPressed && !rightPressed) {
            facingRight = false;
            isMoving = true;
            deltaX = deltaX - moveSpeed * deltaTime;
        }

        if (rightPressed && !leftPressed) {
            facingRight = true;
            isMoving = true;
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

                    // Hitting head
                    if (deltaY > 0f) {
                        playerY = tileBottom - playerHeight;
                        velocityY = 0f;
                        return;
                    }

                    // Landing (ONLY when falling)
                    if (deltaY < 0f) {
                        playerY = tileTop;
                        velocityY = 0f;
                        isOnGround = true;
                        jumpsUsed = 0;
                        return;
                    }
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

    private void drawDoorShapes() {
        int rows = currentLevel.getRows();
        int cols = currentLevel.getCols();
        int tileSize = currentLevel.getTileSize();

        int row = 0;
        while (row < rows) {
            int col = 0;
            while (col < cols) {
                int tile = currentLevel.getTile(row, col);

                float x = col * tileSize;
                float y = row * tileSize;

                if (tile == 2) {
                    // Red door
                    shapeRenderer.setColor(1f, 0f, 0f, 1f);
                    shapeRenderer.rect(x, y, tileSize, tileSize);
                }

                if (tile == 3) {
                    // Aqua door
                    shapeRenderer.setColor(0f, 0.6f, 0.6f, 1f);
                    shapeRenderer.rect(x, y, tileSize, tileSize);
                }

                col = col + 1;
            }
            row = row + 1;
        }
    }


    private void drawTilesWithTextures() {
        int rows = currentLevel.getRows();
        int cols = currentLevel.getCols();
        int tileSize = currentLevel.getTileSize(); // 16

        int row = 0;
        while (row < rows) {
            int col = 0;
            while (col < cols) {
                int tile = currentLevel.getTile(row, col);

                float x = col * tileSize;
                float y = row * tileSize;

                if (tile == 1) {
                    // Solid tile
                    // batch.draw(groundRegion, x, y, tileSize, tileSize);
                    batch.draw(grassTexture, x, y, tileSize, tileSize);
                } else if (tile == 4) {
                    batch.draw(dirtTexture, x, y, tileSize, tileSize);
                } else if (tile == 5) {
                    batch.draw(rightTopGrassTexture, x, y, tileSize, tileSize);
                } else if (tile == 6) {
                    batch.draw(leftTopGrassTexture, x, y, tileSize, tileSize);
                } else if (tile == 7) {
                    batch.draw(leftDirtTexture, x, y, tileSize, tileSize);
                } else if (tile == 8) {
                    batch.draw(rightDirtTexture, x, y, tileSize, tileSize);
                } else if (tile == 9) {
                    batch.draw(bottomDirtTexture, x, y, tileSize, tileSize);
                } else if (tile == 10) {
                    batch.draw(rightBottomDirtTexture, x, y, tileSize, tileSize);
                } else if (tile == 11) {
                    batch.draw(leftBottomDirtTexture, x, y, tileSize, tileSize);
                }
//                else if (tile == 2) {
//                    // Red door tile
////                    batch.draw(redDoorRegion, x, y, tileSize, tileSize);
//

//                } else if (tile == 3) {
//                    // Aqua door tile
////                    batch.draw(aquaDoorRegion, x, y, tileSize, tileSize);

//                }

                col = col + 1;
            }
            row = row + 1;
        }
    }

    @Override
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (batch != null) {
            batch.dispose();
        }
        if (playerSheetTexture != null) {
            playerSheetTexture.dispose();
        }
        if (plainsSheetTexture != null) {
            plainsSheetTexture.dispose();
        }
    }
}
