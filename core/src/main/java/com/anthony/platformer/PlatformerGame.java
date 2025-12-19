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

    // ---------------- ANIMATIONS (PLAYER) ----------------
    private Animation<TextureRegion> walkRightAnimation;
    private Animation<TextureRegion> walkLeftAnimation;
    private Animation<TextureRegion> attackRightAnimation;
    private Animation<TextureRegion> attackLeftAnimation;

    // Optional (Step 6): player death animation
    private Animation<TextureRegion> deathRightAnimation;
    private Animation<TextureRegion> deathLeftAnimation;

    // ---------------- ANIMATIONS (ENEMY) ----------------
    private Animation<TextureRegion> enemyWalkRightAnimation;
    private Animation<TextureRegion> enemyWalkLeftAnimation;

    // Step 6: enemy attack + death animations
    private Animation<TextureRegion> enemyAttackRightAnimation;
    private Animation<TextureRegion> enemyAttackLeftAnimation;
    private Animation<TextureRegion> enemyDeathRightAnimation;
    private Animation<TextureRegion> enemyDeathLeftAnimation;

    // ---------------- PLAYER ATTACK TIMING ----------------
    private float attackTimeSeconds = 0f;
    private float attackDurationSeconds = 0f;

    // ---------------- GLOBAL (PLAYER) ANIMATION TIME ----------------
    private float playerAnimTimeSeconds = 0f;
    private boolean playerDeathStarted = false;

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
    private static final float SWORD_FORWARD_OFFSET = 10f;
    private static final float SWORD_VERTICAL_OFFSET = 4f;

    // ---------------- PLAYER DAMAGE COOLDOWNS ----------------
    private float playerGlobalHurtLockSeconds = 0f;
    private static final float PLAYER_GLOBAL_HURT_LOCK = 0.15f;

    private float playerBodyHurtCooldownSeconds = 0f;
    private static final float PLAYER_BODY_HURT_COOLDOWN = 0.60f;

    private float playerSwordHurtCooldownSeconds = 0f;
    private static final float PLAYER_SWORD_HURT_COOLDOWN = 0.40f;

    // ---------------- HIT REACTION (PLAYER) ----------------
    private float playerStunSeconds = 0f;
    private float playerKnockbackVelX = 0f;

    private static final float PLAYER_STUN_DURATION = 0.12f;
    private static final float PLAYER_KNOCKBACK_SPEED = 260f;
    private static final float PLAYER_KNOCKBACK_FRICTION = 1600f;

    // ---------------- HIT REACTION (ENEMY) ----------------
    private static final float ENEMY_STUN_DURATION = 0.10f;
    private static final float ENEMY_KNOCKBACK_SPEED = 220f;
    private static final float ENEMY_KNOCKBACK_FRICTION = 1400f;

    // ---------------- TILE & WORLD SETTINGS ----------------
    private static final int TILE_SIZE = 16;

    // Put this value in your Levels.LEVEL_X arrays wherever you want an enemy to spawn.
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

    // Enemy gravity can match player gravity
    private static final float ENEMY_GRAVITY = -800f;
    private static final float ENEMY_TERMINAL_VEL = -900f;


    private boolean facingRight = true;
    private boolean isMoving = false;
    private boolean isAttacking = false;

    // ---------------- SPRITE SHEET CONSTANTS ----------------
    private static final int FRAME_WIDTH = 48;
    private static final int FRAME_HEIGHT = 48;

    // Player
    private static final int WALK_ROW = 1;
    private static final int WALK_FRAMES = 6;

    private static final int ATTACK_ROW = 7;
    private static final int ATTACK_FRAMES = 4;

    // IMPORTANT: You may need to change these rows/frames to match your sheet.
    // Enemy
    private static final int ENEMY_WALK_ROW = 1;
    private static final int ENEMY_WALK_FRAMES = 6;

    private static final int ENEMY_ATTACK_ROW = 7;
    private static final int ENEMY_ATTACK_FRAMES = 6;

    private static final int ENEMY_DEATH_ROW = 12;
    private static final int ENEMY_DEATH_FRAMES = 5;

    // Player death (optional)
    private static final int PLAYER_DEATH_ROW = 9;
    private static final int PLAYER_DEATH_FRAMES = 3;

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

        plainsGrid = TextureRegion.split(plainsSheetTexture, FRAME_WIDTH, FRAME_HEIGHT);

        // ---------------- PLAYER WALK ----------------
        TextureRegion[] walkRightFrames = new TextureRegion[WALK_FRAMES];
        for (int col = 0; col < WALK_FRAMES; col++) {
            walkRightFrames[col] = playerGrid[WALK_ROW][col];
        }

        TextureRegion[] walkLeftFrames = new TextureRegion[WALK_FRAMES];
        for (int i = 0; i < WALK_FRAMES; i++) {
            TextureRegion copy = new TextureRegion(walkRightFrames[i]);
            copy.flip(true, false);
            walkLeftFrames[i] = copy;
        }

        float walkFrameDurationSeconds = 0.10f;
        walkRightAnimation = new Animation<TextureRegion>(walkFrameDurationSeconds, walkRightFrames);
        walkLeftAnimation = new Animation<TextureRegion>(walkFrameDurationSeconds, walkLeftFrames);
        walkRightAnimation.setPlayMode(Animation.PlayMode.LOOP);
        walkLeftAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // ---------------- PLAYER ATTACK ----------------
        TextureRegion[] attackRightFrames = new TextureRegion[ATTACK_FRAMES];
        for (int j = 0; j < ATTACK_FRAMES; j++) {
            attackRightFrames[j] = playerGrid[ATTACK_ROW][j];
        }

        TextureRegion[] attackLeftFrames = new TextureRegion[ATTACK_FRAMES];
        for (int i = 0; i < ATTACK_FRAMES; i++) {
            TextureRegion copy = new TextureRegion(attackRightFrames[i]);
            copy.flip(true, false);
            attackLeftFrames[i] = copy;
        }

        float attackFrameDurationSeconds = 0.08f;
        attackRightAnimation = new Animation<TextureRegion>(attackFrameDurationSeconds, attackRightFrames);
        attackLeftAnimation = new Animation<TextureRegion>(attackFrameDurationSeconds, attackLeftFrames);
        attackRightAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        attackLeftAnimation.setPlayMode(Animation.PlayMode.NORMAL);

        // Total time of the attack animation (so we know when to stop attacking)
        attackDurationSeconds = ATTACK_FRAMES * attackFrameDurationSeconds;

        // ---------------- PLAYER DEATH (OPTIONAL) ----------------
        // If your player sheet doesn't have death row, you can set PLAYER_DEATH_FRAMES to 0 and this will never be used.
        if (PLAYER_DEATH_FRAMES > 0) {
            int playerDeathFrames = Math.min(PLAYER_DEATH_FRAMES, playerGrid[PLAYER_DEATH_ROW].length);

            TextureRegion[] deathRightFrames = new TextureRegion[playerDeathFrames];
            for (int i = 0; i < playerDeathFrames; i++) {
                deathRightFrames[i] = playerGrid[PLAYER_DEATH_ROW][i];
            }

            TextureRegion[] deathLeftFrames = new TextureRegion[PLAYER_DEATH_FRAMES];
            for (int i = 0; i < PLAYER_DEATH_FRAMES; i++) {
                TextureRegion copy = new TextureRegion(deathRightFrames[i]);
                copy.flip(true, false);
                deathLeftFrames[i] = copy;
            }

            float deathFrameDurationSeconds = 0.10f;
            deathRightAnimation = new Animation<TextureRegion>(deathFrameDurationSeconds, deathRightFrames);
            deathLeftAnimation = new Animation<TextureRegion>(deathFrameDurationSeconds, deathLeftFrames);
            deathRightAnimation.setPlayMode(Animation.PlayMode.NORMAL);
            deathLeftAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        }

        // ---------------- ENEMY WALK ----------------
        TextureRegion[] enemyWalkRightFrames = new TextureRegion[ENEMY_WALK_FRAMES];
        for (int k = 0; k < ENEMY_WALK_FRAMES; k++) {
            enemyWalkRightFrames[k] = skeletonGrid[ENEMY_WALK_ROW][k];
        }

        TextureRegion[] enemyWalkLeftFrames = new TextureRegion[ENEMY_WALK_FRAMES];
        for (int i = 0; i < ENEMY_WALK_FRAMES; i++) {
            TextureRegion copy = new TextureRegion(enemyWalkRightFrames[i]);
            copy.flip(true, false);
            enemyWalkLeftFrames[i] = copy;
        }

        enemyWalkRightAnimation = new Animation<TextureRegion>(walkFrameDurationSeconds, enemyWalkRightFrames);
        enemyWalkLeftAnimation = new Animation<TextureRegion>(walkFrameDurationSeconds, enemyWalkLeftFrames);
        enemyWalkRightAnimation.setPlayMode(Animation.PlayMode.LOOP);
        enemyWalkLeftAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // ---------------- ENEMY ATTACK ----------------
        TextureRegion[] enemyAttackRightFrames = new TextureRegion[ENEMY_ATTACK_FRAMES];
        for (int i = 0; i < ENEMY_ATTACK_FRAMES; i++) {
            enemyAttackRightFrames[i] = skeletonGrid[ENEMY_ATTACK_ROW][i];
        }

        TextureRegion[] enemyAttackLeftFrames = new TextureRegion[ENEMY_ATTACK_FRAMES];
        for (int i = 0; i < ENEMY_ATTACK_FRAMES; i++) {
            TextureRegion copy = new TextureRegion(enemyAttackRightFrames[i]);
            copy.flip(true, false);
            enemyAttackLeftFrames[i] = copy;
        }

        enemyAttackRightAnimation = new Animation<TextureRegion>(attackFrameDurationSeconds, enemyAttackRightFrames);
        enemyAttackLeftAnimation = new Animation<TextureRegion>(attackFrameDurationSeconds, enemyAttackLeftFrames);
        enemyAttackRightAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        enemyAttackLeftAnimation.setPlayMode(Animation.PlayMode.NORMAL);

        // ---------------- ENEMY DEATH ----------------
        int enemyDeathFrames = Math.min(ENEMY_DEATH_FRAMES, skeletonGrid[ENEMY_DEATH_ROW].length);

        TextureRegion[] enemyDeathRightFrames = new TextureRegion[enemyDeathFrames];
        for (int i = 0; i < enemyDeathFrames; i++) {
            enemyDeathRightFrames[i] = skeletonGrid[ENEMY_DEATH_ROW][i];
        }

        TextureRegion[] enemyDeathLeftFrames = new TextureRegion[ENEMY_DEATH_FRAMES];
        for (int i = 0; i < ENEMY_DEATH_FRAMES; i++) {
            TextureRegion copy = new TextureRegion(enemyDeathRightFrames[i]);
            copy.flip(true, false);
            enemyDeathLeftFrames[i] = copy;
        }

        float enemyDeathFrameDurationSeconds = 0.10f;
        enemyDeathRightAnimation = new Animation<TextureRegion>(enemyDeathFrameDurationSeconds, enemyDeathRightFrames);
        enemyDeathLeftAnimation = new Animation<TextureRegion>(enemyDeathFrameDurationSeconds, enemyDeathLeftFrames);
        enemyDeathRightAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        enemyDeathLeftAnimation.setPlayMode(Animation.PlayMode.NORMAL);

        // Plains regions (optional)
        groundRegion = plainsGrid[2][0];
        redDoorRegion = plainsGrid[2][1];
        aquaDoorRegion = plainsGrid[1][1];

        // Player draw settings
        playerWidth = 16f;
        playerHeight = 20f;
        drawWidth = 72f;
        drawHeight = 72f;

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

        // Animation time
        playerAnimTimeSeconds = playerAnimTimeSeconds + deltaTime;

        updatePlayer(deltaTime);
        updateEnemies(deltaTime);

        updatePlayerHurtbox();
        updatePlayerSwordHitbox();
        updateEnemyHitboxes();

        handlePlayerSwordHits();
        handleEnemySwordHitsPlayer();
        handleEnemyBodyHitsPlayer();

        updateCamera();

        float red = 0.05f;
        float green = 0.05f;
        float blue = 0.18f;
        float alpha = 1.0f;

        Gdx.gl.glClearColor(red, green, blue, alpha);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.zoom = MathUtils.clamp(camera.zoom, 0.5f, 3.0f);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawTilesWithTextures();
        drawPlayerWithBatch();
        drawEnemiesWithBatch();
        batch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawDoorShapes();
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        drawHitboxesDebug();
        shapeRenderer.end();

        removeEnemiesThatFinishedDeath();
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
        // Death locks everything
        if (e.isDead) {
            if (!e.deathStarted) {
                e.deathStarted = true;
                e.animTimeSeconds = 0f;
            }

            if (e.facingRight) {
                if (enemyDeathRightAnimation.isAnimationFinished(e.animTimeSeconds)) {
                    e.readyToRemove = true;
                }
                return enemyDeathRightAnimation.getKeyFrame(e.animTimeSeconds, false);
            } else {
                if (enemyDeathLeftAnimation.isAnimationFinished(e.animTimeSeconds)) {
                    e.readyToRemove = true;
                }
                return enemyDeathLeftAnimation.getKeyFrame(e.animTimeSeconds, false);
            }
        }

        // Attack
        if (e.isAttacking) {
            if (e.facingRight) {
                return enemyAttackRightAnimation.getKeyFrame(e.attackTimeSeconds, false);
            } else {
                return enemyAttackLeftAnimation.getKeyFrame(e.attackTimeSeconds, false);
            }
        }

        // "Hit" state (stun) - if you don't have a hit animation, just show idle frame
        if (e.stunSeconds > 0f) {
            if (e.facingRight) {
                return enemyWalkRightAnimation.getKeyFrames()[0];
            } else {
                return enemyWalkLeftAnimation.getKeyFrames()[0];
            }
        }

        // Walk / idle
        if (e.isMoving) {
            if (e.facingRight) {
                return enemyWalkRightAnimation.getKeyFrame(e.animTimeSeconds, true);
            } else {
                return enemyWalkLeftAnimation.getKeyFrame(e.animTimeSeconds, true);
            }
        }

        if (e.facingRight) {
            return enemyWalkRightAnimation.getKeyFrames()[0];
        } else {
            return enemyWalkLeftAnimation.getKeyFrames()[0];
        }
    }

    private TextureRegion getCurrentPlayerFrame() {
        boolean dead = playerHp <= 0;

        // Death locks
        if (dead && deathRightAnimation != null && deathLeftAnimation != null) {
            if (!playerDeathStarted) {
                playerDeathStarted = true;
                playerAnimTimeSeconds = 0f;
            }

            if (facingRight) {
                return deathRightAnimation.getKeyFrame(playerAnimTimeSeconds, false);
            } else {
                return deathLeftAnimation.getKeyFrame(playerAnimTimeSeconds, false);
            }
        }

        // Attack has priority
        if (isAttacking) {
            if (facingRight) {
                return attackRightAnimation.getKeyFrame(attackTimeSeconds, false);
            } else {
                return attackLeftAnimation.getKeyFrame(attackTimeSeconds, false);
            }
        }

        // Hit state (stun) - if you don't have a hit animation, show idle
        if (playerStunSeconds > 0f) {
            if (facingRight) {
                return walkRightAnimation.getKeyFrames()[0];
            } else {
                return walkLeftAnimation.getKeyFrames()[0];
            }
        }

        // Walk
        if (isMoving) {
            if (facingRight) {
                return walkRightAnimation.getKeyFrame(playerAnimTimeSeconds, true);
            } else {
                return walkLeftAnimation.getKeyFrame(playerAnimTimeSeconds, true);
            }
        }

        // Idle
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

                    float groundTopY = findGroundYBelow(spawnX, spawnY, enemyWidth);

                    // place enemy standing on the ground tile
                    float fixedY = groundTopY;

                    Enemy e = new Enemy(spawnX, fixedY, enemyWidth, enemyHeight);

                    // Patrol bounds: 6 tiles left/right from spawn (tune this)
                    float patrolRadiusPixels = 6f * TILE_SIZE;
                    e.patrolLeftX = Math.max(0f, spawnX - patrolRadiusPixels);
                    e.patrolRightX = Math.min(worldWidthPixels - e.width, spawnX + patrolRadiusPixels);

                    e.moveDir = 1;

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

            // Always advance animation time (even while dead, for death playback)
            e.animTimeSeconds = e.animTimeSeconds + deltaTime;

            // If dead: do nothing else (death animation plays via animTimeSeconds)
            if (e.isDead) {
                i = i + 1;
                continue;
            }

            // ---------------- ENEMY GRAVITY ----------------
            e.velocityY = e.velocityY + ENEMY_GRAVITY * deltaTime;

            if (e.velocityY < ENEMY_TERMINAL_VEL) {
                e.velocityY = ENEMY_TERMINAL_VEL;
            }

            float deltaY = e.velocityY * deltaTime;
            if (deltaY != 0f) aaa

dddddddddddddddddddd            if (e.y < 0f) {
                e.y = 0f;
                e.velocityY = 0f;
                e.isOnGround = true;
            }


            // Stun timer
            if (e.stunSeconds > 0f) {
                e.stunSeconds = e.stunSeconds - deltaTime;
                if (e.stunSeconds < 0f) {
                    e.stunSeconds = 0f;
                }
            }

            // Knockback
            if (e.knockbackVelX != 0f) {
                float dx = e.knockbackVelX * deltaTime;
                e.x = e.x + dx;

                if (e.x < 0f) {
                    e.x = 0f;
                    e.knockbackVelX = 0f;
                }
                if (e.x + e.width > worldWidthPixels) {
                    e.x = worldWidthPixels - e.width;
                    e.knockbackVelX = 0f;
                }

                if (e.knockbackVelX > 0f) {
                    e.knockbackVelX = e.knockbackVelX - ENEMY_KNOCKBACK_FRICTION * deltaTime;
                    if (e.knockbackVelX < 0f) {
                        e.knockbackVelX = 0f;
                    }
                } else {
                    e.knockbackVelX = e.knockbackVelX + ENEMY_KNOCKBACK_FRICTION * deltaTime;
                    if (e.knockbackVelX > 0f) {
                        e.knockbackVelX = 0f;
                    }
                }
            }

            boolean enemyStunned = e.stunSeconds > 0f;

            // Cooldown tick
            if (e.attackCooldownSeconds > 0f) {
                e.attackCooldownSeconds = e.attackCooldownSeconds - deltaTime;
                if (e.attackCooldownSeconds < 0f) {
                    e.attackCooldownSeconds = 0f;
                }
            }

            // Attack tick
            if (e.isAttacking) {
                e.attackTimeSeconds = e.attackTimeSeconds + deltaTime;

                if (e.attackTimeSeconds >= e.attackDurationSeconds) {
                    e.isAttacking = false;
                    e.attackTimeSeconds = 0f;
                    e.attackCooldownSeconds = e.attackCooldownDurationSeconds;
                }
            }

            // Start attack if close, not stunned
            if (!enemyStunned && !e.isAttacking && e.attackCooldownSeconds == 0f) {
                float enemyCenterX = e.x + e.width / 2f;
                float playerCenterX = playerX + playerWidth / 2f;

                float absDistanceX = Math.abs(enemyCenterX - playerCenterX);
                float attackRange = 40f;

                if (absDistanceX <= attackRange) {
                    e.isAttacking = true;
                    e.attackTimeSeconds = 0f;
                    e.facingRight = playerCenterX > enemyCenterX;
                }
            }
            updateEnemyMovementAI(e, deltaTime);


            i = i + 1;
        }
    }

    private void removeEnemiesThatFinishedDeath() {
        int i = 0;
        while (i < enemies.size()) {
            Enemy e = enemies.get(i);

            if (e.isDead && e.readyToRemove) {
                enemies.remove(i);
                continue;
            }

            i = i + 1;
        }
    }

    private void updatePlayer(float deltaTime) {
        float deltaX = 0f;
        isMoving = false;

        float axisX = 0f;
        if (controller != null) {
            axisX = controller.getAxis(AXIS_LEFT_X);
        }

        float deadzone = 0.20f;
        if (Math.abs(axisX) < deadzone) {
            axisX = 0f;
        }

        // cooldown timers
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

        // stun timer
        if (playerStunSeconds > 0f) {
            playerStunSeconds = playerStunSeconds - deltaTime;
            if (playerStunSeconds < 0f) {
                playerStunSeconds = 0f;
            }
        }

        // Apply knockback
        if (playerKnockbackVelX != 0f) {
            float dx = playerKnockbackVelX * deltaTime;
            moveHorizontal(dx);

            if (playerKnockbackVelX > 0f) {
                playerKnockbackVelX = playerKnockbackVelX - PLAYER_KNOCKBACK_FRICTION * deltaTime;
                if (playerKnockbackVelX < 0f) {
                    playerKnockbackVelX = 0f;
                }
            } else {
                playerKnockbackVelX = playerKnockbackVelX + PLAYER_KNOCKBACK_FRICTION * deltaTime;
                if (playerKnockbackVelX > 0f) {
                    playerKnockbackVelX = 0f;
                }
            }
        }

        boolean dead = playerHp <= 0;
        boolean stunned = playerStunSeconds > 0f;

        // Camera zoom keys still allowed
        if (Gdx.input.isKeyPressed(Input.Keys.Z)) {
            camera.zoom += 0.02f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.X)) {
            camera.zoom -= 0.02f;
        }

        // If dead: no input, but still fall with gravity
        if (!dead && !stunned) {
            boolean leftPressed = axisX < 0f
                || Gdx.input.isKeyPressed(Input.Keys.A)
                || Gdx.input.isKeyPressed(Input.Keys.LEFT);

            boolean rightPressed = axisX > 0f
                || Gdx.input.isKeyPressed(Input.Keys.D)
                || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

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

            // Input merge
            boolean jumpDown = false;
            boolean attackDown = false;

            if (controller != null) {
                jumpDown = controller.getButton(BUTTON_A);
                attackDown = controller.getButton(BUTTON_X);
            }

            jumpDown = jumpDown || Gdx.input.isKeyPressed(Input.Keys.SPACE);
            attackDown = attackDown || Gdx.input.isButtonPressed(Input.Buttons.LEFT);

            boolean jumpPressedThisFrame = jumpDown && !wasJumpDownLastFrame;
            boolean attackPressedThisFrame = attackDown && !wasAttackDownLastFrame;

            wasJumpDownLastFrame = jumpDown;
            wasAttackDownLastFrame = attackDown;

            handleJumpInput(jumpPressedThisFrame);
            handleAttackInput(deltaTime, attackPressedThisFrame);
        } else {
            // If dead or stunned, stop starting new attacks
            wasJumpDownLastFrame = false;
            wasAttackDownLastFrame = false;
        }

        // Gravity always applies
        velocityY = velocityY + gravity * deltaTime;

        float deltaY = velocityY * deltaTime;
        if (deltaY != 0f) {
            moveVertical(deltaY);
        }

        if (playerX < 0f) {
            playerX = 0f;
        }
        if (playerX + playerWidth > worldWidthPixels) {
            playerX = worldWidthPixels - playerWidth;
        }

        if (playerY < 0f) {
            playerY = 0f;
            velocityY = 0f;
            isOnGround = true;
            jumpsUsed = 0;
        }

        boolean touchingDoorNow = isTouchingRedDoor();
        boolean touchingAquaDoorNow = isTouchingAquaDoor();

        if (touchingDoorNow && !wasTouchingDoorLastFrame) {
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

            if (e.isDead) {
                i = i + 1;
                continue;
            }

            if (playerHurtbox.overlaps(e.hurtbox)) {
                playerHp = playerHp - 1;

                playerStunSeconds = PLAYER_STUN_DURATION;

                float enemyCenterX = e.x + e.width / 2f;
                float playerCenterX = playerX + playerWidth / 2f;

                if (playerCenterX < enemyCenterX) {
                    playerKnockbackVelX = -PLAYER_KNOCKBACK_SPEED;
                } else {
                    playerKnockbackVelX = PLAYER_KNOCKBACK_SPEED;
                }

                playerGlobalHurtLockSeconds = PLAYER_GLOBAL_HURT_LOCK;
                playerBodyHurtCooldownSeconds = PLAYER_BODY_HURT_COOLDOWN;

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
        if (attackPressedThisFrame && !isAttacking) {
            isAttacking = true;
            attackTimeSeconds = 0f;

            int i = 0;
            while (i < enemies.size()) {
                enemies.get(i).wasHitThisAttack = false;
                i = i + 1;
            }
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

    private void moveEnemyVertical(Enemy e, float deltaY) {
        e.y = e.y + deltaY;
        e.isOnGround = false;
        resolveEnemyVerticalCollisions(e, deltaY);
    }

    private void resolveEnemyVerticalCollisions(Enemy e, float deltaY) {
        float left = e.x;
        float right = e.x + e.width;
        float bottom = e.y;
        float top = e.y + e.height;

        int minTileX = (int) (left / TILE_SIZE);
        int maxTileX = (int) (right / TILE_SIZE);
        int minTileY = (int) (bottom / TILE_SIZE);
        int maxTileY = (int) (top / TILE_SIZE);

        int ty = minTileY;
        while (ty <= maxTileY) {
            int tx = minTileX;
            while (tx <= maxTileX) {

                if (!isSolidTileForEnemy(tx, ty)) {
                    tx = tx + 1;
                    continue;
                }

                float tileLeft = tx * TILE_SIZE;
                float tileRight = tileLeft + TILE_SIZE;
                float tileBottom = ty * TILE_SIZE;
                float tileTop = tileBottom + TILE_SIZE;

                boolean overlapX = right > tileLeft && left < tileRight;
                boolean overlapY = top > tileBottom && bottom < tileTop;

                if (overlapX && overlapY) {
                    if (deltaY > 0f) {
                        // moving up -> hit ceiling
                        e.y = tileBottom - e.height;
                        e.velocityY = 0f;
                        return;
                    }

                    if (deltaY < 0f) {
                        // moving down -> land on ground
                        e.y = tileTop;
                        e.velocityY = 0f;
                        e.isOnGround = true;
                        return;
                    }
                }

                tx = tx + 1;
            }
            ty = ty + 1;
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
                        playerY = tileBottom - playerHeight;
                        velocityY = 0f;
                        return;
                    }

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
        int tileValue = currentLevel.getTile(tileY, tileX);
        if (tileValue == TILE_ENEMY_SPAWN) {
            return false;
        }

        return currentLevel.isSolidTile(tileX, tileY);
    }

    private boolean isSolidTileForEnemy(int tileX, int tileY) {
        // Treat outside world as solid so enemies turn around
        if (tileX < 0 || tileY < 0) {
            return true;
        }

        int maxTileX = currentLevel.getCols() - 1;
        int maxTileY = currentLevel.getRows() - 1;

        if (tileX > maxTileX || tileY > maxTileY) {
            return true;
        }

        int tileValue = currentLevel.getTile(tileY, tileX);

        // Enemy spawn marker should not block movement
        if (tileValue == TILE_ENEMY_SPAWN) {
            return false;
        }

        return currentLevel.isSolidTile(tileX, tileY);
    }

    private float findGroundYBelow(float startX, float startY, float entityWidth) {
        // Start checking from the tile row at startY and go downward until we hit a solid tile.
        int startColLeft = (int) (startX / TILE_SIZE);
        int startColRight = (int) ((startX + entityWidth - 1f) / TILE_SIZE);

        int startRow = (int) (startY / TILE_SIZE) - 1;

        int row = startRow;
        while (row >= 0) {
            boolean foundSolid = false;

            int col = startColLeft;
            while (col <= startColRight) {
                if (isSolidTileForEnemy(col, row)) {
                    foundSolid = true;
                    break;
                }
                col = col + 1;
            }

            if (foundSolid) {
                // Ground tile top in world coords
                float tileTopY = (row + 1) * TILE_SIZE;
                return tileTopY;
            }

            row = row - 1;
        }

        // If nothing solid below, just return original
        return startY;
    }


    private boolean enemyWouldCollideAtX(Enemy e, float newX) {
        float left = newX;
        float right = newX + e.width;

        float bottom = e.y;
        float top = e.y + e.height;

        int minTileX = (int) (left / TILE_SIZE);
        int maxTileX = (int) (right / TILE_SIZE);

        int minTileY = (int) (bottom / TILE_SIZE);
        int maxTileY = (int) (top / TILE_SIZE);

        int ty = minTileY;
        while (ty <= maxTileY) {
            int tx = minTileX;
            while (tx <= maxTileX) {
                if (isSolidTileForEnemy(tx, ty)) {
                    // basic AABB overlap test with this tile
                    float tileLeft = tx * TILE_SIZE;
                    float tileRight = tileLeft + TILE_SIZE;

                    float tileBottom = ty * TILE_SIZE;
                    float tileTop = tileBottom + TILE_SIZE;

                    boolean overlapX = right > tileLeft && left < tileRight;
                    boolean overlapY = top > tileBottom && bottom < tileTop;

                    if (overlapX && overlapY) {
                        return true;
                    }
                }
                tx = tx + 1;
            }
            ty = ty + 1;
        }

        return false;
    }

    private boolean enemyHasGroundAhead(Enemy e, int dir) {
        // Look one pixel ahead of the enemy's front foot
        float frontX;
        if (dir > 0) {
            frontX = e.x + e.width + 1f;
        } else {
            frontX = e.x - 1f;
        }

        float footY = e.y - 1f;

        int tileX = (int) (frontX / TILE_SIZE);
        int tileY = (int) (footY / TILE_SIZE);

        return isSolidTileForEnemy(tileX, tileY);
    }

    private boolean enemyHasWallAhead(Enemy e, int dir) {
        float frontX;
        if (dir > 0) {
            frontX = e.x + e.width + 1f;
        } else {
            frontX = e.x - 1f;
        }

        int tileX = (int) (frontX / TILE_SIZE);

        // Check along enemy vertical body
        float bodyBottom = e.y + 1f;
        float bodyTop = e.y + e.height - 1f;

        int minTileY = (int) (bodyBottom / TILE_SIZE);
        int maxTileY = (int) (bodyTop / TILE_SIZE);

        int ty = minTileY;
        while (ty <= maxTileY) {
            if (isSolidTileForEnemy(tileX, ty)) {
                return true;
            }
            ty = ty + 1;
        }

        return false;
    }

    private void updateEnemyMovementAI(Enemy e, float deltaTime) {
        // Do not move if dead or stunned
        if (e.isDead) {
            e.isMoving = false;
            return;
        }
        if (e.stunSeconds > 0f) {
            e.isMoving = false;
            return;
        }

        // If currently attacking, do not move (keeps swings clean)
        if (e.isAttacking) {
            e.isMoving = false;
            return;
        }

        float enemyCenterX = e.x + e.width / 2f;
        float playerCenterX = playerX + playerWidth / 2f;

        float distX = playerCenterX - enemyCenterX;
        float absDistX = Math.abs(distX);

        // Chase toggle (hysteresis prevents jitter)
        if (!e.isChasing && absDistX <= e.aggroRangePixels) {
            e.isChasing = true;
        } else if (e.isChasing && absDistX >= e.disengageRangePixels) {
            e.isChasing = false;
        }

        float speed;
        int dir;

        if (e.isChasing) {
            // Chase the player
            dir = (distX >= 0f) ? 1 : -1;
            speed = e.chaseSpeed;
        } else {
            // Patrol
            dir = e.moveDir;
            speed = e.patrolSpeed;

            // Patrol bounds
            if (e.x <= e.patrolLeftX) {
                dir = 1;
            }
            if (e.x >= e.patrolRightX) {
                dir = -1;
            }

            // Turn around if ledge or wall ahead
            if (!enemyHasGroundAhead(e, dir) || enemyHasWallAhead(e, dir)) {
                dir = -dir;
            }
        }

        // Apply movement
        float dx = dir * speed * deltaTime;
        float newX = e.x + dx;

        // Collision: if would collide, turn around and stop this frame
        if (enemyWouldCollideAtX(e, newX)) {
            e.moveDir = -dir;
            e.isMoving = false;
            return;
        }

        // Commit movement and update direction/facing
        e.x = newX;

        // Keep inside world
        if (e.x < 0f) {
            e.x = 0f;
            dir = 1;
        }
        if (e.x + e.width > worldWidthPixels) {
            e.x = worldWidthPixels - e.width;
            dir = -1;
        }

        e.moveDir = dir;
        e.facingRight = dir > 0;
        e.isMoving = true;
    }


    // ----------------------- CAMERA & HITBOX UPDATES -----------------------
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

            e.updateHurtbox();

            // No sword hitbox when dead
            if (e.isDead) {
                e.clearSwordHitbox();
                i = i + 1;
                continue;
            }

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

        int i = 0;
        while (i < enemies.size()) {
            Enemy e = enemies.get(i);

            if (e.isDead) {
                i = i + 1;
                continue;
            }

            if (!e.wasHitThisAttack && playerSwordHitbox.overlaps(e.hurtbox)) {
                e.hp = e.hp - 1;
                e.wasHitThisAttack = true;

                e.stunSeconds = ENEMY_STUN_DURATION;

                float enemyCenterX = e.x + e.width / 2f;
                float playerCenterX = playerX + playerWidth / 2f;

                if (enemyCenterX < playerCenterX) {
                    e.knockbackVelX = -ENEMY_KNOCKBACK_SPEED;
                } else {
                    e.knockbackVelX = ENEMY_KNOCKBACK_SPEED;
                }

                // Cancel swing when hit
                e.isAttacking = false;
                e.attackTimeSeconds = 0f;
                e.clearSwordHitbox();

                if (e.hp <= 0) {
                    e.startDeath();
                }
            }

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

            if (e.isDead) {
                i = i + 1;
                continue;
            }

            if (e.swordHitbox.width > 0f && e.swordHitbox.height > 0f) {
                if (e.swordHitbox.overlaps(playerHurtbox)) {
                    playerHp = playerHp - 1;

                    playerStunSeconds = PLAYER_STUN_DURATION;

                    float enemyCenterX = e.x + e.width / 2f;
                    float playerCenterX = playerX + playerWidth / 2f;

                    if (playerCenterX < enemyCenterX) {
                        playerKnockbackVelX = -PLAYER_KNOCKBACK_SPEED;
                    } else {
                        playerKnockbackVelX = PLAYER_KNOCKBACK_SPEED;
                    }

                    playerGlobalHurtLockSeconds = PLAYER_GLOBAL_HURT_LOCK;
                    playerSwordHurtCooldownSeconds = PLAYER_SWORD_HURT_COOLDOWN;

                    return;
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

            // Enemy sword hitbox (orange-ish)
            shapeRenderer.setColor(1f, 0.5f, 0f, 1f);
            shapeRenderer.rect(e.swordHitbox.x, e.swordHitbox.y, e.swordHitbox.width, e.swordHitbox.height);

            shapeRenderer.setColor(1f, 0f, 0f, 1f);
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
                    shapeRenderer.setColor(1f, 0f, 0f, 1f);
                    shapeRenderer.rect(x, y, tileSize, tileSize);
                }

                if (tile == 3) {
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
        int tileSize = currentLevel.getTileSize();

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
