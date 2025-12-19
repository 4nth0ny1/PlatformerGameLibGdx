package com.anthony.platformer;

import com.badlogic.gdx.math.Rectangle;

public class Enemy {
    public float x;
    public float y;

    public float width;
    public float height;

    public boolean facingRight;
    public boolean isMoving;

    public boolean wasHitThisAttack = false;

    // --- Combat / collision boxes ---
    public final Rectangle hurtbox = new Rectangle();
    public final Rectangle swordHitbox = new Rectangle();

    // --- Combat state ---
    public boolean isAttacking = false;
    public float attackTimeSeconds = 0f;
    public float attackDurationSeconds = 0.48f;

    public float attackCooldownSeconds = 0f;
    public float attackCooldownDurationSeconds = 1.00f;

    // --- Hit reaction ---
    public float stunSeconds = 0f;
    public float knockbackVelX = 0f;

    // --- Animation time (the ONLY time value enemies should use for animations) ---
    public float animTimeSeconds = 0f;

    // --- Death state ---
    public boolean isDead = false;
    public boolean deathStarted = false;
    public boolean readyToRemove = false;

    // ---------------- ENEMY MOVEMENT AI ----------------
    public float patrolLeftX = 0f;
    public float patrolRightX = 0f;

    public int moveDir = 1; // 1 = right, -1 = left

    public float patrolSpeed = 60f;
    public float chaseSpeed = 90f;

    public float aggroRangePixels = 140f; // start chasing when player is this close
    public float disengageRangePixels = 180f; // stop chasing when farther than this

    public boolean isChasing = false;
    // --- Platform lock (the ground Y this enemy belongs to) ---
    public float homeGroundY = 0f;


    // ---------------- ENEMY VERTICAL PHYSICS ----------------
    public float velocityY = 0f;
    public boolean isOnGround = false;


    public int hp = 3;

    public Enemy(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.facingRight = true;
        this.isMoving = true;

        updateHurtbox();
        clearSwordHitbox();
    }

    public void updateHurtbox() {
        hurtbox.set(x, y, width, height);
    }

    public void clearSwordHitbox() {
        swordHitbox.set(0f, 0f, 0f, 0f);
    }

    public void startDeath() {
        isDead = true;
        deathStarted = false;
        readyToRemove = false;

        isAttacking = false;
        attackTimeSeconds = 0f;
        clearSwordHitbox();

        stunSeconds = 0f;
        knockbackVelX = 0f;

        animTimeSeconds = 0f;
    }
}
