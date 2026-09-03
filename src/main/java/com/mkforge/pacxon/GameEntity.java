package com.mkforge.pacxon;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.ImageView;

public class GameEntity {

    private boolean gameOver = false;
    protected boolean powerUpActive = false;
    protected double speedX = 1;
    protected double speedY = 1;
    protected double speed = 1;

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public ImageView getShape() {
        return null;
    }

    public List<ImageView> getCreatedPossibleBlocks() {
        return new ArrayList<>();
    }

    public void setSpeed(double speedX, double speedY, boolean powerUpActive) {
        if (!powerUpActive) {
            this.speedX = speedX;
            this.speedY = speedY;
        }
    }

    public void setPacmanSpeed(double pacmanSpeed, boolean powerUpActive) {
        this.speed = pacmanSpeed;
        this.powerUpActive = powerUpActive;
    }
}