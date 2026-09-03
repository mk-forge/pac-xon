package com.mkforge.pacxon;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class Map {

    private final Image block;
    private final Game game;
    private final List<PowerUp> activePowerUps = new ArrayList<>();
    private final String[] powerUpTypes = {"ball.png", "strawberry.png", "cherry.png", "peach.png"};
    private final Random random = new Random();
    private Group gameGroup;
    private Timeline refreshTimer;
    private List<Ghost> ghosts = new ArrayList<>();
    private final Image blueGhostImage;
    private Timeline peachTimeline;
    private Timeline strawberryTimeline;
    private Timeline ballTimeline;
    private final List<Image> originalGhostImages = new ArrayList<>();

    public Map(Game game) {
        this.game = game;
        this.block = loadImage("images/block.png");
        this.blueGhostImage = loadImage("images/dark_blue_ghost.png");
    }

    private Image loadImage(String path) {
        java.net.URL url = getClass().getResource(path);
        return url != null ? new Image(url.toExternalForm()) : new Image("");
    }

    public void setGhosts(List<Ghost> ghosts) {
        this.ghosts = ghosts;
        originalGhostImages.clear();

        for (Ghost currentGhost : ghosts) {
            originalGhostImages.add(currentGhost.getOriginalImage());
        }
    }

    public void initializeMap(Group gameGroup) {
        this.gameGroup = gameGroup;
        drawBorders();
    }

    private void drawBorders() {
        int blockSize = 15;
        int topY = 35;
        int bottomY = Game.HEIGHT - 70;
        int leftX = 5;
        int rightX = Game.WIDTH - 35;

        for (int x = leftX; x <= rightX; x += blockSize) {
            addBlock(x, topY);
            addBlock(x, bottomY);
        }

        for (int y = topY + blockSize; y < bottomY; y += blockSize) {
            addBlock(leftX, y);
            addBlock(rightX, y);
        }
    }

    private void addBlock(double x, double y) {
        ImageView blockView = new ImageView(block);
        blockView.setX(x);
        blockView.setY(y);
        blockView.setFitHeight(15);
        blockView.setFitWidth(15);
        gameGroup.getChildren().add(blockView);
    }

    public void initializeGrid(Group gameGroup) {
        Canvas grid = new Canvas(Game.WIDTH - 35, Game.HEIGHT - 70);
        GraphicsContext gc = grid.getGraphicsContext2D();
        gc.setLineWidth(1.0);
        gc.setStroke(Color.rgb(40, 40, 40));

        for (int x = 20; x < Game.WIDTH - 25; x += 15) {
            gc.moveTo(x + 0.5, 50);
            gc.lineTo(x + 0.5, Game.HEIGHT - 70);
            gc.stroke();
        }

        for (int y = 50; y < Game.HEIGHT - 25; y += 15) {
            gc.moveTo(20, y + 0.5);
            gc.lineTo(Game.WIDTH - 25, y + 0.5);
            gc.stroke();
        }

        gameGroup.getChildren().add(grid);
        spawnPowerUps(3);
        game.logGameEvent("Spawnuto 3 power-upů.");
        scheduleNextRefresh();
    }

    private void scheduleNextRefresh() {
        int randomDelay = 10 + random.nextInt(21);
        game.logGameEvent("Další obnova power-upů za " + randomDelay + " sekund.");
        if (refreshTimer != null) {
            refreshTimer.stop();
        }

        refreshTimer = new Timeline(new KeyFrame(Duration.seconds(randomDelay), event -> {
            refreshPowerUps();
            scheduleNextRefresh();
        }));
        refreshTimer.setCycleCount(1);
        refreshTimer.play();
    }

    public void spawnPowerUps(int count) {
        for (int i = 0; i < count; i++) {
            String type = powerUpTypes[random.nextInt(powerUpTypes.length)];
            ImageView view = createPowerUpView(type);

            if (view != null) {
                double x = 20 + random.nextInt((Game.WIDTH - 35 - 20) / 15) * 15;
                double y = 50 + random.nextInt((Game.HEIGHT - 70 - 50) / 15) * 15;
                view.setX(x);
                view.setY(y);
                view.setFitHeight(15);
                view.setFitWidth(15);
                gameGroup.getChildren().add(view);
                view.toFront();
                activePowerUps.add(new PowerUp(view, type));
            }
        }
    }

    private ImageView createPowerUpView(String type) {
        java.net.URL powerUpUrl = getClass().getResource("images/" + type);
        if (powerUpUrl != null) {
            return new ImageView(new Image(powerUpUrl.toExternalForm()));
        }
        return null;
    }

    public void refreshPowerUps() {
        for (PowerUp powerUp : activePowerUps) {
            gameGroup.getChildren().remove(powerUp.view());
        }

        activePowerUps.clear();
        spawnPowerUps(3);
        game.logGameEvent("Power-upy byly obnoveny.");
    }

    public void getPowerUp(GameEntity pacman, Group gameGroup) {
        List<PowerUp> toRemove = new ArrayList<>();

        for (PowerUp powerUp : activePowerUps) {
            if (pacman.getShape().getBoundsInParent().intersects(powerUp.view().getBoundsInParent())) {
                gameGroup.getChildren().remove(powerUp.view());
                toRemove.add(powerUp);
                SoundManager.playPowerUp();

                if ("ball.png".equals(powerUp.type())) {
                    if (ballTimeline == null)
                        applyBallEffect(gameGroup);
                } else {
                    powerUp.applyEffect(pacman, ghosts, game, peachTimeline, strawberryTimeline);
                }
            }
        }

        activePowerUps.removeAll(toRemove);
    }

    private void applyBallEffect(Group gameGroup) {
        if (ballTimeline != null) {
            ballTimeline.stop();
            ballTimeline = null;
        }

        for (int i = 0; i < ghosts.size(); i++) {
            Ghost currentGhost = ghosts.get(i);
            if (currentGhost.isEaten()) {
                Image originalImage = originalGhostImages.get(i);
                if (originalImage != null) currentGhost.getShape().setImage(originalImage);
                currentGhost.resetState();

                if (i != 0)
                    currentGhost.respawnToFreePosition(gameGroup);
            } else {
                currentGhost.canBeEaten(false);
            }
        }

        for (int i = 0; i < ghosts.size(); i++) {
            Ghost currentGhost = ghosts.get(i);
            originalGhostImages.set(i, currentGhost.getOriginalImage());
            currentGhost.getShape().setImage(blueGhostImage);
            currentGhost.canBeEaten(true);
        }

        for (Ghost currentGhost : ghosts) {
            if (game.getPacman().getShape().getBoundsInParent().intersects(currentGhost.getShape().getBoundsInParent())) {
                gameGroup.getChildren().remove(currentGhost.getShape());
                currentGhost.setEaten(true);
            }
        }

        ballTimeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            for (int i = 0; i < ghosts.size(); i++) {
                Ghost currentGhost = ghosts.get(i);
                Image originalImage = originalGhostImages.get(i);
                if (originalImage != null)
                    currentGhost.getShape().setImage(originalImage);

                if (currentGhost.isEaten()) {
                    currentGhost.resetState();

                    if (i != 0)
                        currentGhost.respawnToFreePosition(gameGroup);
                } else {
                    currentGhost.canBeEaten(false);
                }
            }
            ballTimeline = null;
        }));
        ballTimeline.setCycleCount(1);
        ballTimeline.play();
    }

    public void stopPowerUpRefresh() {
        if (refreshTimer != null) {
            refreshTimer.stop();
            refreshTimer = null;
        }

        if (peachTimeline != null) {
            peachTimeline.stop();
            peachTimeline = null;
        }

        if (strawberryTimeline != null) {
            strawberryTimeline.stop();
            strawberryTimeline = null;
        }

        if (ballTimeline != null) {
            ballTimeline.stop();
            ballTimeline = null;
        }
    }

    public List<PowerUp> getActivePowerUps() {
        return activePowerUps;
    }
}