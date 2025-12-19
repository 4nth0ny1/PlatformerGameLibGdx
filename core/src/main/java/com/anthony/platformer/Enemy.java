package com.anthony.platformer;

import com.badlogic.gdx.math.Rectangle;

public class Enemy {
    public float x;
    public float y;

    public float width;
    public float height;

    public boolean facingRight;
    public boolean isMoving;

    public float stateTimeSeconds;
    public boolean wasHitThisAttack = false;

    // --- Combat / collision boxes ---
    // Hurtbox: where the enemy can be hit
    public final Rectangle hurtbox = new Rectangle();

    // Hitbox: where the enemy's sword would hit (only when attacking)
    public final Rectangle swordHitbox = new Rectangle();

    // Simple combat state (we'll use in later steps)
    public boolean isAttacking = false;
    public float attackTimeSeconds = 0f;
    public float attackDurationSeconds = 0.20f;

    public float attackCooldownSeconds = 0f;
    public float attackCooldownDurationSeconds = 1.00f;

    public int hp = 3;

    public Enemy(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.facingRight = true;
        this.isMoving = true;
        this.stateTimeSeconds = 0f;

        updateHurtbox();
        clearSwordHitbox();
    }

    public void updateHurtbox() {
        hurtbox.set(x, y, width, height);
    }

    public void clearSwordHitbox() {
        swordHitbox.set(0f, 0f, 0f, 0f);
    }
}
