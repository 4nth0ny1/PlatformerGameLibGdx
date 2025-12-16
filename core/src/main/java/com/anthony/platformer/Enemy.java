package com.anthony.platformer;

public class Enemy {
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
