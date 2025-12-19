package com.anthony.platformer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;


import java.util.ArrayList;
import java.util.List;

public class PlatformerGame extends ApplicationAdapter {

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

    // Controller mapping (you may need to change these after you test)
    private static final int AXIS_LEFT_X = 0;

    // Common mappings (often, but not always):
    private static final int BUTTON_A = 0;   // jump
    private static final int BUTTON_X = 2;   // attack

    private boolean wasJumpDownLastFrame = false;
    private boolean wasAttackDownLastFrame = false;

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

    // ---------------- HITBOXES ----------------

    private final Rectangle playerHurtbox = new Rectangle();
    private final Rectangle playerSwordHitbox = new Rectangle();

    // Optional: basic "invincibility" so you don't take damage every frame
    private float playerHurtCooldownSeconds = 0f;
    private static final float PLAYER_HURT_COOLDOWN = 0.50f;

    private int playerHp = 5;

    // Sword tuning
    private static final float SWORD_WIDTH = 14f;
    private static final float SWORD_HEIGHT = 10f;
    private static final float SWORD_FORWARD_OFFSET = 10f;  // how far in front of player
    private static final float SWORD_VERTICAL_OFFSET = 4f;  // height relative to playerY

    // ---------------- PLAYER DAMAGE COOLDOWNS ----------------

    private float playerGlobalHurtLockSeconds = 0f;
    private static final float PLAYER_GLOBAL_HURT_LOCK = 0.15f;

    // Body contact damage pacing
    private float playerBodyHurtCooldownSeconds = 0f;
    private static final float PLAYER_BODY_HURT_COOLDOWN = 0.60f;

    // Enemy sword damage pacing
    private float playerSwordHurtCooldownSeconds = 0f;
    private static final float PLAYER_SWORD_HURT_COOLDOWN = 0.40f;


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

    private Controller controller;

    @Override
    public void create() {

        controller = null;
        if (Controllers.getControllers().size > 0) {
            controller = Controllers.getControllers().first();
            System.out.println("Using controller: " + controller.getName());
        }

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
        updatePlayerHurtbox();
        updatePlayerSwordHitbox();
        updateEnemyHitboxes();
        handlePlayerSwordHits();
        handleEnemySwordHitsPlayer();
        handleEnemyBodyHitsPlayer();
        updateCamera();

        if (isAttacking) {
            System.out.println("Sword box: x=" + playerSwordHitbox.x
                + " y=" + playerSwordHitbox.y
                + " w=" + playerSwordHitbox.width
                + " h=" + playerSwordHitbox.height);
        }

//        if (enemies.size() > 0) {
//            Enemy e = enemies.get(0);
//            System.out.println("Enemy0 hurtbox: x=" + e.hurtbox.x
//                + " y=" + e.hurtbox.y
//                + " w=" + e.hurtbox.width
//                + " h=" + e.hurtbox.height);
//        }


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

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        drawHitboxesDebug();
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

            // -------- Attack cooldown tick --------
            if (e.attackCooldownSeconds > 0f) {
                e.attackCooldownSeconds = e.attackCooldownSeconds - deltaTime;
                if (e.attackCooldownSeconds < 0f) {
                    e.attackCooldownSeconds = 0f;
                }
            }

            // -------- Attack active tick --------
            if (e.isAttacking) {
                e.attackTimeSeconds = e.attackTimeSeconds + deltaTime;

                if (e.attackTimeSeconds >= e.attackDurationSeconds) {
                    e.isAttacking = false;
                    e.attackTimeSeconds = 0f;

                    // after a swing, go on cooldown
                    e.attackCooldownSeconds = e.attackCooldownDurationSeconds;
                }
            }

            // -------- Start attack if close enough --------
            if (!e.isAttacking && e.attackCooldownSeconds == 0f) {
                float enemyCenterX = e.x + e.width / 2f;
                float playerCenterX = playerX + playerWidth / 2f;

                float distanceX = enemyCenterX - playerCenterX;
                float absDistanceX = Math.abs(distanceX);

                float attackRange = 40f;

                if (absDistanceX <= attackRange) {
                    e.isAttacking = true;
                    e.attackTimeSeconds = 0f;

                    // Face the player
                    e.facingRight = playerCenterX > enemyCenterX;

                    System.out.println("ENEMY ATTACK START");
                }
            }

            i = i + 1;
        }
    }


    private void updatePlayer(float deltaTime) {
        // Horizontal movement
        float deltaX = 0f;

        isMoving = false;

        float axisX = 0f;

        if (controller != null) {
            axisX = controller.getAxis(AXIS_LEFT_X); // left stick X is often 0
        }

        // deadzone so it doesn't drift
        float deadzone = 0.20f;
        if (Math.abs(axisX) < deadzone) {
            axisX = 0f;
        }

        if (playerHurtCooldownSeconds > 0f) {
            playerHurtCooldownSeconds = playerHurtCooldownSeconds - deltaTime;
            if (playerHurtCooldownSeconds < 0f) {
                playerHurtCooldownSeconds = 0f;
            }
        }

        if (playerGlobalHurtLockSeconds > 0f) {
            playerGlobalHurtLockSeconds = playerGlobalHurtLockSeconds - deltaTime;
            if (playerGlobalHurtLockSeconds < 0f) {
                playerGlobalHurtLockSeconds = 0f;
            }
        }

        if (playerBodyHurtCooldownSeconds > 0f) {
            playerBodyHurtCooldownSeconds = playerBodyHurtCooldownSeconds - deltaTime;
            if (playerBodyHurtCooldownSeconds < 0f) {
                playerBodyHurtCooldownSeconds = 0f;
            }
        }

        if (playerSwordHurtCooldownSeconds > 0f) {
            playerSwordHurtCooldownSeconds = playerSwordHurtCooldownSeconds - deltaTime;
            if (playerSwordHurtCooldownSeconds < 0f) {
                playerSwordHurtCooldownSeconds = 0f;
            }
        }



        boolean leftPressed = axisX < 0f
            || Gdx.input.isKeyPressed(Input.Keys.A)
            || Gdx.input.isKeyPressed(Input.Keys.LEFT);

        boolean rightPressed = axisX > 0f
            || Gdx.input.isKeyPressed(Input.Keys.D)
            || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

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

        // ---------------- CONTROLLER + KB/MOUSE INPUT MERGE ----------------

        boolean jumpDown = false;
        boolean attackDown = false;

        if (controller != null) {
            jumpDown = controller.getButton(BUTTON_A);
            attackDown = controller.getButton(BUTTON_X);
        }

        // Keep keyboard/mouse too
        // Jump: Space (down)
        jumpDown = jumpDown || Gdx.input.isKeyPressed(Input.Keys.SPACE);

        // Attack: left mouse (down)
        attackDown = attackDown || Gdx.input.isButtonPressed(Input.Buttons.LEFT);

        // Convert "down" to "pressed this frame" (edge detect)
        boolean jumpPressedThisFrame = jumpDown && !wasJumpDownLastFrame;
        boolean attackPressedThisFrame = attackDown && !wasAttackDownLastFrame;

        wasJumpDownLastFrame = jumpDown;
        wasAttackDownLastFrame = attackDown;

        // Jump / double jump + attack
        handleJumpInput(jumpPressedThisFrame);
        handleAttackInput(deltaTime, attackPressedThisFrame);

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

    private void handleEnemyBodyHitsPlayer() {
        if (playerGlobalHurtLockSeconds > 0f) {
            return;
        }
        if (playerBodyHurtCooldownSeconds > 0f) {
            return;
        }

        int i = 0;
        while (i < enemies.size()) {
            Enemy e = enemies.get(i);

            if (playerHurtbox.overlaps(e.hurtbox)) {
                playerHp = playerHp - 1;

                playerGlobalHurtLockSeconds = PLAYER_GLOBAL_HURT_LOCK;
                playerBodyHurtCooldownSeconds = PLAYER_BODY_HURT_COOLDOWN;

                System.out.println("PLAYER HIT by ENEMY BODY! HP = " + playerHp);

                if (playerHp <= 0) {
                    System.out.println("PLAYER DEAD");
                }

                return;
            }

            i = i + 1;
        }
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

    // CHANGED: takes jumpPressedThisFrame from updatePlayer()
    private void handleJumpInput(boolean jumpPressedThisFrame) {
        if (jumpPressedThisFrame) {
            if (jumpsUsed < maxJumps) {
                velocityY = jumpVelocity;
                jumpsUsed = jumpsUsed + 1;
                isOnGround = false;
            }
        }
    }

    private void handleAttackInput(float deltaTime, boolean attackPressedThisFrame) {
        // Start attack only on edge press
        if (attackPressedThisFrame && !isAttacking) {
            System.out.println("ATTACK START");
            isAttacking = true;
            attackTimeSeconds = 0f;

            int i = 0;
            while (i < enemies.size()) {
                enemies.get(i).wasHitThisAttack = false;
                i = i + 1;
            }
        }


        // If attacking, advance timer and end attack when done
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

    private void updatePlayerHurtbox() {
        playerHurtbox.set(playerX, playerY, playerWidth, playerHeight);
    }

    private void updatePlayerSwordHitbox() {
        if (!isAttacking) {
            playerSwordHitbox.set(0f, 0f, 0f, 0f);
            return;
        }

        float swordX;
        if (facingRight) {
            swordX = playerX + playerWidth + SWORD_FORWARD_OFFSET;
        } else {
            swordX = playerX - SWORD_FORWARD_OFFSET - SWORD_WIDTH;
        }

        float swordY = playerY + SWORD_VERTICAL_OFFSET;

        playerSwordHitbox.set(swordX, swordY, SWORD_WIDTH, SWORD_HEIGHT);
    }

    private void updateEnemyHitboxes() {
        int i = 0;
        while (i < enemies.size()) {
            Enemy e = enemies.get(i);

            // Enemy hurtbox follows its position
            e.updateHurtbox();

            // Enemy sword hitbox: ON only during attacks
            if (e.isAttacking) {
                float swordW = 14f;
                float swordH = 10f;

                float forward = 6f;
                float vertical = 4f;

                float swordX;
                if (e.facingRight) {
                    swordX = e.x + e.width + forward;
                } else {
                    swordX = e.x - forward - swordW;
                }

                float swordY = e.y + vertical;

                e.swordHitbox.set(swordX, swordY, swordW, swordH);
            } else {
                e.clearSwordHitbox();
            }

            i = i + 1;
        }
    }


    private void handlePlayerSwordHits() {
        if (!isAttacking) {
            return;
        }

        System.out.println("Checking sword hits... enemies=" + enemies.size());

        int i = 0;
        while (i < enemies.size()) {
            Enemy e = enemies.get(i);

            if (!e.wasHitThisAttack && playerSwordHitbox.overlaps(e.hurtbox)) {
                e.hp = e.hp - 1;
                e.wasHitThisAttack = true;

                // Optional: tiny hit feedback (later we can add knockback)
                System.out.println("Enemy hit! HP = " + e.hp);

                if (e.hp <= 0) {
                    enemies.remove(i);
                    continue; // do NOT increment i
                }
            }

            i = i + 1;
        }
    }


    private void drawHitboxesDebug() {
        // Player hurtbox (green)
        shapeRenderer.setColor(0f, 1f, 0f, 1f);
        shapeRenderer.rect(playerHurtbox.x, playerHurtbox.y, playerHurtbox.width, playerHurtbox.height);

        // Player sword (yellow)
        shapeRenderer.setColor(1f, 1f, 0f, 1f);
        shapeRenderer.rect(playerSwordHitbox.x, playerSwordHitbox.y, playerSwordHitbox.width, playerSwordHitbox.height);

        // Enemy hurtboxes (red)
        shapeRenderer.setColor(1f, 0f, 0f, 1f);
        int i = 0;
        while (i < enemies.size()) {
            Enemy e = enemies.get(i);
            shapeRenderer.rect(e.hurtbox.x, e.hurtbox.y, e.hurtbox.width, e.hurtbox.height);
            i = i + 1;
        }
    }

    private void handleEnemySwordHitsPlayer() {
        if (playerGlobalHurtLockSeconds > 0f) {
            return;
        }
        if (playerSwordHurtCooldownSeconds > 0f) {
            return;
        }

        int i = 0;
        while (i < enemies.size()) {
            Enemy e = enemies.get(i);

            if (e.swordHitbox.width > 0f && e.swordHitbox.height > 0f) {
                if (e.swordHitbox.overlaps(playerHurtbox)) {
                    playerHp = playerHp - 1;

                    playerGlobalHurtLockSeconds = PLAYER_GLOBAL_HURT_LOCK;
                    playerSwordHurtCooldownSeconds = PLAYER_SWORD_HURT_COOLDOWN;

                    System.out.println("PLAYER HIT by ENEMY SWORD! HP = " + playerHp);

                    if (playerHp <= 0) {
                        System.out.println("PLAYER DEAD");
                    }

                    return;
                }
            }

            i = i + 1;
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
