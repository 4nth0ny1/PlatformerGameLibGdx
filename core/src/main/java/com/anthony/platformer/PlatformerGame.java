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
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class PlatformerGame extends ApplicationAdapter {

    private static class Enemy {
        public float x;
        public float y;

        public float width;
        public float height;

        public boolean facingRight;
        public boolean isMoving;

        public float stateTimeSeconds;

        public Enemy(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;

            this.facingRight = true;
            this.isMoving = true;

            this.stateTimeSeconds = 0f;
        }
    }

    private SpriteBatch batch;

    private Texture playerSheetTexture;
    private Texture skeletonSheetTexture;

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
    private Texture roundedGrassTexture;
    private Texture roundedGrassFlipTexture;

    private float spriteFootOffset = 42f; // pixels inside the 48x48 frame (tweak)

    private Animation<TextureRegion> walkRightAnimation;
    private Animation<TextureRegion> walkLeftAnimation;
    private Animation<TextureRegion> attackRightAnimation;
    private Animation<TextureRegion> attackLeftAnimation;

    private Animation<TextureRegion> enemyWalkRightAnimation;
    private Animation<TextureRegion> enemyWalkLeftAnimation;

    private float attackTimeSeconds = 0f;
    private float attackDurationSeconds = 0f;

    private float stateTimeSeconds;

    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;

    // ---------------- TILE & WORLD SETTINGS ----------------

    private static final int TILE_SIZE = 16;

    // Put this value in your Levels.LEVEL_X arrays wherever you want an enemy to spawn.
    // Example: tile value 20 means "spawn enemy here"
    private static final int TILE_ENEMY_SPAWN = 20;

    private Level currentLevel;

    private int worldWidthPixels;
    private int worldHeightPixels;

    private int currentLevelNumber = 1;
    private boolean wasTouchingDoorLastFrame = false;
    private boolean wasTouchingAquaDoorLastFrame = false;

    // ---------------- PLAYER SETTINGS ----------------

    private float playerWidth = 16f;
    private float playerHeight = 16f;

    private float enemyWidth = 16f;
    private float enemyHeight = 16f;

    private float playerX;
    private float playerY;

    private float moveSpeed = 150f;
    private float gravity = -800f;
    private float velocityY = 0f;
    private float jumpVelocity = 300f;

    private boolean facingRight = true;
    private boolean isMoving = false;
    private boolean isAttacking = false;

    private static final int FRAME_WIDTH = 48;
    private static final int FRAME_HEIGHT = 48;
    private static final int WALK_ROW = 1;
    private static final int WALK_FRAMES = 6;

    private static final int ENEMY_WALK_ROW = 1;
    private static final int ENEMY_FRAMES = 6;

    private static final int ATTACK_ROW = 7;
    private static final int ATTACK_FRAMES = 4;

    private float drawWidth;
    private float drawHeight;

    // Double jump
    private int maxJumps = 2;
    private int jumpsUsed = 0;
    private boolean isOnGround = false;

    // ---------------- PLAINS TILE REGIONS ----------------

    private TextureRegion[][] plainsGrid;

    private TextureRegion groundRegion;
    private TextureRegion redDoorRegion;
    private TextureRegion aquaDoorRegion;

    // ---------------- ENEMIES ----------------

    private List<Enemy> enemies = new ArrayList<Enemy>();

    @Override
    public void create() {
        batch = new SpriteBatch();

        playerSheetTexture = new Texture("player.png");
        skeletonSheetTexture = new Texture("skeleton.png");

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
        roundedGrassTexture = new Texture("grass-rounded-up.png");
        roundedGrassFlipTexture = new Texture("grass-rounded-up-flip.png");

        TextureRegion[][] playerGrid = TextureRegion.split(playerSheetTexture, FRAME_WIDTH, FRAME_HEIGHT);
        TextureRegion[][] skeletonGrid = TextureRegion.split(skeletonSheetTexture, FRAME_WIDTH, FRAME_HEIGHT);

        // Plains sheet split (48x48 chunks)
        plainsGrid = TextureRegion.split(plainsSheetTexture, FRAME_WIDTH, FRAME_HEIGHT);

        // Build WALK RIGHT frames from the chosen row
        TextureRegion[] walkRightFrames = new TextureRegion[WALK_FRAMES];
        for (int col = 0; col < WALK_FRAMES; col++) {
            walkRightFrames[col] = playerGrid[WALK_ROW][col];
        }

        // Enemy walk right frames
        TextureRegion[] enemyWalkRightFrames = new TextureRegion[ENEMY_FRAMES];
        for (int k = 0; k < ENEMY_FRAMES; k++) {
            enemyWalkRightFrames[k] = skeletonGrid[ENEMY_WALK_ROW][k];
        }

        // Build WALK LEFT frames by flipping copies of the right frames
        TextureRegion[] walkLeftFrames = new TextureRegion[WALK_FRAMES];
        for (int i = 0; i < WALK_FRAMES; i++) {
            TextureRegion copy = new TextureRegion(walkRightFrames[i]);
            copy.flip(true, false);
            walkLeftFrames[i] = copy;
        }

        // Enemy walk left frames
        TextureRegion[] enemyWalkLeftFrames = new TextureRegion[ENEMY_FRAMES];
        for (int i = 0; i < ENEMY_FRAMES; i++) {
            TextureRegion copy = new TextureRegion(enemyWalkRightFrames[i]);
            copy.flip(true, false);
            enemyWalkLeftFrames[i] = copy;
        }

        // Build Attack Right animation
        TextureRegion[] attackRightFrames = new TextureRegion[ATTACK_FRAMES];
        for (int j = 0; j < ATTACK_FRAMES; j++) {
            attackRightFrames[j] = playerGrid[ATTACK_ROW][j];
        }

        float attackFrameDurationSeconds = 0.08f;
        attackRightAnimation = new Animation<TextureRegion>(attackFrameDurationSeconds, attackRightFrames);
        attackRightAnimation.setPlayMode(Animation.PlayMode.NORMAL);

        // Attack left frames
        TextureRegion[] attackLeftFrames = new TextureRegion[ATTACK_FRAMES];
        for (int i = 0; i < ATTACK_FRAMES; i++) {
            TextureRegion copy = new TextureRegion(attackRightFrames[i]);
            copy.flip(true, false);
            attackLeftFrames[i] = copy;
        }

        attackLeftAnimation = new Animation<TextureRegion>(attackFrameDurationSeconds, attackLeftFrames);
        attackLeftAnimation.setPlayMode(Animation.PlayMode.NORMAL);

        // Total time of the attack animation (so we know when to stop attacking)
        attackDurationSeconds = ATTACK_FRAMES * attackFrameDurationSeconds;

        // Plains regions (optional)
        groundRegion = plainsGrid[2][0];
        redDoorRegion = plainsGrid[2][1];
        aquaDoorRegion = plainsGrid[1][1];

        // Player draw settings
        playerWidth = 16f;
        playerHeight = 20f;
        drawWidth = 72f;
        drawHeight = 72f;

        float frameDurationSeconds = 0.10f;
        walkRightAnimation = new Animation<TextureRegion>(frameDurationSeconds, walkRightFrames);
        walkLeftAnimation = new Animation<TextureRegion>(frameDurationSeconds, walkLeftFrames);

        enemyWalkRightAnimation = new Animation<TextureRegion>(frameDurationSeconds, enemyWalkRightFrames);
        enemyWalkLeftAnimation = new Animation<TextureRegion>(frameDurationSeconds, enemyWalkLeftFrames);

        walkRightAnimation.setPlayMode(Animation.PlayMode.LOOP);
        walkLeftAnimation.setPlayMode(Animation.PlayMode.LOOP);

        enemyWalkRightAnimation.setPlayMode(Animation.PlayMode.LOOP);
        enemyWalkLeftAnimation.setPlayMode(Animation.PlayMode.LOOP);

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
        stateTimeSeconds = stateTimeSeconds + deltaTime;

        updatePlayer(deltaTime);
        updateEnemies(deltaTime);
        updateCamera();

        float red = 0.05f;
        float green = 0.05f;
        float blue = 0.18f;
        float alpha = 1.0f;

        Gdx.gl.glClearColor(red, green, blue, alpha);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.zoom = MathUtils.clamp(camera.zoom, 0.5f, 3.0f);
        camera.update();

        // --- DRAW WORLD (TEXTURES) ---
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawTilesWithTextures();
        drawPlayerWithBatch();
        drawEnemiesWithBatch();
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

    private void drawEnemiesWithBatch() {
        int i = 0;
        while (i < enemies.size()) {
            Enemy e = enemies.get(i);

            TextureRegion currentFrame = getCurrentEnemyFrame(e);

            float drawX = e.x - (drawWidth - e.width) / 2f;
            float drawY = e.y - (drawHeight - e.height) + spriteFootOffset;

            batch.draw(currentFrame, drawX, drawY, drawWidth, drawHeight);

            i = i + 1;
        }
    }

    private TextureRegion getCurrentEnemyFrame(Enemy e) {
        if (e.isMoving) {
            if (e.facingRight) {
                return enemyWalkRightAnimation.getKeyFrame(e.stateTimeSeconds, true);
            } else {
                return enemyWalkLeftAnimation.getKeyFrame(e.stateTimeSeconds, true);
            }
        }

        if (e.facingRight) {
            return enemyWalkRightAnimation.getKeyFrames()[0];
        } else {
            return enemyWalkLeftAnimation.getKeyFrames()[0];
        }
    }

    private TextureRegion getCurrentPlayerFrame() {
        // 1) Attack has highest priority
        if (isAttacking) {
            if (facingRight) {
                return attackRightAnimation.getKeyFrame(attackTimeSeconds, false);
            } else {
                return attackLeftAnimation.getKeyFrame(attackTimeSeconds, false);
            }
        }

        // 2) Movement animations
        if (isMoving) {
            if (facingRight) {
                return walkRightAnimation.getKeyFrame(stateTimeSeconds, true);
            } else {
                return walkLeftAnimation.getKeyFrame(stateTimeSeconds, true);
            }
        }

        // 3) Idle
        if (facingRight) {
            return walkRightAnimation.getKeyFrames()[0];
        } else {
            return walkLeftAnimation.getKeyFrames()[0];
        }
    }

    // ----------------------- LEVEL BUILDING -----------------------

    private Level createLevel1() {
        return new Level(Levels.LEVEL_1, TILE_SIZE, 5, 2);
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

        buildEnemiesFromLevel();
    }

    private void buildEnemiesFromLevel() {
        enemies.clear();

        int rows = currentLevel.getRows();
        int cols = currentLevel.getCols();
        int tileSize = currentLevel.getTileSize();

        int row = 0;
        while (row < rows) {
            int col = 0;
            while (col < cols) {
                int tile = currentLevel.getTile(row, col);

                if (tile == TILE_ENEMY_SPAWN) {
                    float spawnX = col * tileSize;
                    float spawnY = row * tileSize;

                    Enemy e = new Enemy(spawnX, spawnY, enemyWidth, enemyHeight);
                    enemies.add(e);
                }

                col = col + 1;
            }
            row = row + 1;
        }
    }

    // ----------------------- UPDATE LOGIC -----------------------

    private void updateEnemies(float deltaTime) {
        int i = 0;
        while (i < enemies.size()) {
            Enemy e = enemies.get(i);
            e.stateTimeSeconds = e.stateTimeSeconds + deltaTime;

            // Placeholder: no AI yet.
            // If you want them to patrol, we can add movement + collisions next.

            i = i + 1;
        }
    }

    private void updatePlayer(float deltaTime) {
        // Horizontal movement
        float deltaX = 0f;

        isMoving = false;

        boolean leftPressed = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean rightPressed = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        if (Gdx.input.isKeyPressed(Input.Keys.Z)) {
            camera.zoom += 0.02f;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.X)) {
            camera.zoom -= 0.02f;
        }

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
        handleAttackInput(deltaTime);

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

    private void handleAttackInput(float deltaTime) {
        boolean attackPressedThisFrame = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);

        if (attackPressedThisFrame) {
            isAttacking = true;
            attackTimeSeconds = 0f;
        }

        if (isAttacking) {
            attackTimeSeconds = attackTimeSeconds + deltaTime;

            if (attackTimeSeconds >= attackDurationSeconds) {
                isAttacking = false;
                attackTimeSeconds = 0f;
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
        // Enemy spawn tiles should not be solid. Your Level.isSolidTile probably checks tile==1 only,
        // but this guard makes it future-proof.
        int tileValue = currentLevel.getTile(tileY, tileX);
        if (tileValue == TILE_ENEMY_SPAWN) {
            return false;
        }

        return currentLevel.isSolidTile(tileX, tileY);
    }

    // ----------------------- CAMERA & DRAWING -----------------------

    private void updateCamera() {
        float camX = playerX + playerWidth / 2f;
        float camY = playerY + playerHeight / 2f;

        float halfW = camera.viewportWidth * camera.zoom / 2f;
        float halfH = camera.viewportHeight * camera.zoom / 2f;

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
                } else if (tile == 12) {
                    batch.draw(roundedGrassTexture, x, y, tileSize, tileSize);
                } else if (tile == 13) {
                    batch.draw(roundedGrassFlipTexture, x, y, tileSize, tileSize);
                }

                // We intentionally do nothing for TILE_ENEMY_SPAWN here.
                // It only exists to place enemies in the level array.

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
        if (skeletonSheetTexture != null) {
            skeletonSheetTexture.dispose();
        }
        if (plainsSheetTexture != null) {
            plainsSheetTexture.dispose();
        }

        if (grassTexture != null) {
            grassTexture.dispose();
        }
        if (dirtTexture != null) {
            dirtTexture.dispose();
        }
        if (rightTopGrassTexture != null) {
            rightTopGrassTexture.dispose();
        }
        if (leftTopGrassTexture != null) {
            leftTopGrassTexture.dispose();
        }
        if (leftDirtTexture != null) {
            leftDirtTexture.dispose();
        }
        if (rightDirtTexture != null) {
            rightDirtTexture.dispose();
        }
        if (bottomDirtTexture != null) {
            bottomDirtTexture.dispose();
        }
        if (leftBottomDirtTexture != null) {
            leftBottomDirtTexture.dispose();
        }
        if (rightBottomDirtTexture != null) {
            rightBottomDirtTexture.dispose();
        }
        if (roundedGrassTexture != null) {
            roundedGrassTexture.dispose();
        }
        if (roundedGrassFlipTexture != null) {
            roundedGrassFlipTexture.dispose();
        }
    }
}
