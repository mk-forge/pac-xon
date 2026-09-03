package com.mkforge.pacxon;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.scene.Group;
import javafx.stage.Stage;

public class Ghost extends GameEntity implements Collisionable {
    private ImageView ghostImageView;
    private final Pacman pacman;
    private final GameInfo lives;
    private boolean powerUpActive = false;
    private boolean isStopped = false;
    private final Game game;
    private Image originalImage;
    private int orangeDirection = 0;
    private static final Point2D[] ORANGE_DIRECTIONS = { new Point2D(1, 0), new Point2D(0, 1), new Point2D(-1, 0), new Point2D(0, -1) };
    private double startX, startY;
    private boolean isEaten = false;
    private double orangeSpeedMultiplier = 1.0;

    public Ghost(Pacman pacman, GameInfo lives, Game game) {
        super();
        this.pacman = pacman;
        this.lives = lives;
        this.game = game;
        this.speedX = 1;
        this.speedY = 1;
    }

    public void initializePinkGhost(int x, int y) {
        initializeGhost("images/pink_ghost.png", x, y);
    }

    public void initializeRedGhost(int x, int y) {
        initializeGhost("images/red_ghost.png", x, y);
    }

    public void initializeOrangeGhost(int x, int y) {
        initializeGhost("images/orange_ghost.png", x, y);
    }

    public void initializeBlueGhost(int x, int y) {
        initializeGhost("images/blue_ghost.png", x, y);
    }

    private void initializeGhost(String imagePath, int x, int y) {
        this.startX = x;
        this.startY = y;
        java.net.URL imageUrl = getClass().getResource(imagePath);

        if (imageUrl != null) {
            Image ghostImage = new Image(imageUrl.toExternalForm());
            originalImage = ghostImage;
            ghostImageView = new ImageView(ghostImage);
            ghostImageView.setFitHeight(15);
            ghostImageView.setFitWidth(15);

            while (isPositionTaken(x, y)) {
                Random random = new Random();
                x = 20 + random.nextInt(Game.WIDTH - 70);
                y = 50 + random.nextInt(Game.HEIGHT - 120);
            }

            ghostImageView.setX(x);
            ghostImageView.setY(y);
            this.startX = x;
            this.startY = y;
        } else {
            ghostImageView = new ImageView();
        }
    }

    private boolean isPositionTaken(double x, double y) {
        for (ImageView block : pacman.getCreatedPossibleBlocks()) {
            if (Math.abs(block.getX() - x) < 15 && Math.abs(block.getY() - y) < 15)
                return true;
        }
        return false;
    }

    public ImageView getShape() {
        return ghostImageView;
    }

    @Override
    public void setSpeed(double speedX, double speedY, boolean powerUpActive) {
        super.setSpeed(speedX, speedY, powerUpActive);
        this.orangeSpeedMultiplier = speedX;

        if (speedX != 0 || speedY != 0)
            this.isStopped = false;
    }

    private Point2D applyBoundaryConstraints(Point2D pos) {
        double x = pos.getX();
        double y = pos.getY();
        double width = ghostImageView.getFitWidth();
        double height = ghostImageView.getFitHeight();

        if (y + height > Game.HEIGHT - 70 && speedY > 0) {
            y = Game.HEIGHT - height - 70;
            speedY = -Math.abs(speedY);
        }

        if (x + width > Game.WIDTH - 35 && speedX > 0) {
            x = Game.WIDTH - width - 35;
            speedX = -Math.abs(speedX);
        }

        if (y < 50 && speedY < 0) {
            y = 50;
            speedY = Math.abs(speedY);
        }

        if (x < 20 && speedX < 0) {
            x = 20;
            speedX = Math.abs(speedX);
        }

        return new Point2D(x, y);
    }

    private Point2D resolveBlockCollision(Point2D pos, ImageView block) {
        double x = pos.getX();
        double y = pos.getY();

        double overlapX = Math.max(0, Math.min(ghostImageView.getBoundsInParent().getMaxX(), block.getBoundsInParent().getMaxX()) - Math.max(ghostImageView.getBoundsInParent().getMinX(), block.getBoundsInParent().getMinX()));
        double overlapY = Math.max(0, Math.min(ghostImageView.getBoundsInParent().getMaxY(), block.getBoundsInParent().getMaxY()) - Math.max(ghostImageView.getBoundsInParent().getMinY(), block.getBoundsInParent().getMinY()));

        double ghostCenterX = ghostImageView.getBoundsInParent().getMinX() + ghostImageView.getBoundsInParent().getWidth() / 2;
        double blockCenterX = block.getBoundsInParent().getMinX() + block.getBoundsInParent().getWidth() / 2;
        double ghostCenterY = ghostImageView.getBoundsInParent().getMinY() + ghostImageView.getBoundsInParent().getHeight() / 2;
        double blockCenterY = block.getBoundsInParent().getMinY() + block.getBoundsInParent().getHeight() / 2;

        if (overlapX > overlapY) {
            if (ghostCenterY < blockCenterY) {
                y -= overlapY;
                speedY = -Math.abs(speedY);
            } else {
                y += overlapY;
                speedY = Math.abs(speedY);
            }
        } else {
            if (ghostCenterX < blockCenterX) {
                x -= overlapX;
                speedX = -Math.abs(speedX);
            } else {
                x += overlapX;
                speedX = Math.abs(speedX);
            }
        }

        return new Point2D(x, y);
    }

    private void moveGhostCommon(boolean destroyBlocks) {
        if (isStopped) return;

        double newX = ghostImageView.getX() + 2 * speedX;
        double newY = ghostImageView.getY() + 2 * speedY;
        Point2D pos = new Point2D(newX, newY);

        pos = applyBoundaryConstraints(pos);
        List<ImageView> blocksToRemove = new ArrayList<>();
        boolean hasDestroyed = false;
        List<ImageView> activeTrail = pacman.getActiveTrail();

        for (ImageView possibleBlock : pacman.getCreatedPossibleBlocks()) {
            if (activeTrail.contains(possibleBlock)) continue;

            if (ghostImageView.getBoundsInParent().intersects(possibleBlock.getBoundsInParent())) {
                if (destroyBlocks) {
                    hasDestroyed = true;
                    blocksToRemove.add(possibleBlock);
                }
                pos = resolveBlockCollision(pos, possibleBlock);
            }
        }

        if (hasDestroyed) SoundManager.playDestroy();

        for (ImageView block : blocksToRemove) {
            pacman.getCreatedPossibleBlocks().remove(block);
            pacman.getFilledBlocks().remove(block);
            if (block.getParent() != null)
                ((Group)block.getParent()).getChildren().remove(block);
        }

        ghostImageView.setX(pos.getX());
        ghostImageView.setY(pos.getY());
    }

    public void moveGhost() {
        moveGhostCommon(false);
    }

    public void moveRedGhost() {
        moveGhostCommon(true);
    }

    public void moveOrangeGhost() {
        if (isStopped) return;

        double speedMagnitude = 1.5 * orangeSpeedMultiplier;
        int left = (orangeDirection + 3) % 4;
        int right = (orangeDirection + 1) % 4;
        int back = (orangeDirection + 2) % 4;
        int nextDirection = orangeDirection;

        if (canMoveOrange(left, speedMagnitude)) {
            nextDirection = left;
        } else if (!canMoveOrange(orangeDirection, speedMagnitude)) {
            if (canMoveOrange(right, speedMagnitude))
                nextDirection = right;
            else if (canMoveOrange(back, speedMagnitude))
                nextDirection = back;
        }

        orangeDirection = nextDirection;
        Point2D direction = ORANGE_DIRECTIONS[orangeDirection];
        speedX = direction.getX() * speedMagnitude;
        speedY = direction.getY() * speedMagnitude;
        ghostImageView.setX(ghostImageView.getX() + 2 * speedX);
        ghostImageView.setY(ghostImageView.getY() + 2 * speedY);
    }

    private boolean canMoveOrange(int direction, double speedMagnitude) {
        Point2D dir = ORANGE_DIRECTIONS[direction];
        double nextX = ghostImageView.getX() + 2 * dir.getX() * speedMagnitude;
        double nextY = ghostImageView.getY() + 2 * dir.getY() * speedMagnitude;

        if (nextX < 15 || nextX + ghostImageView.getFitWidth() > Game.WIDTH - 30 || nextY < 45 || nextY + ghostImageView.getFitHeight() > Game.HEIGHT - 65)
            return false;

        List<ImageView> activeTrail = pacman.getActiveTrail();
        for (ImageView possibleBlock : pacman.getCreatedPossibleBlocks()) {
            if (activeTrail.contains(possibleBlock)) continue;
            boolean overlaps = nextX + ghostImageView.getFitWidth() > possibleBlock.getX() && nextX < possibleBlock.getX() + possibleBlock.getFitWidth() && nextY + ghostImageView.getFitHeight() > possibleBlock.getY() && nextY < possibleBlock.getY() + possibleBlock.getFitHeight();
            if (overlaps) return false;
        }

        return true;
    }

    public void stopGhost() {
        this.speedX = 0;
        this.speedY = 0;
        this.isStopped = true;
    }

    @Override
    public void checkCollisionWithPacman(Pacman pacman, Ghost ghost, Map map, Group gameGroup) {
        if (!powerUpActive) {
            boolean hitsPacman = !this.pacman.isOnSafeGround() && pacman.getShape().getBoundsInParent().intersects(ghostImageView.getBoundsInParent());
            boolean hitsActiveTrail = touchesActiveTrail();

            if (hitsPacman || hitsActiveTrail) {
                pacman.resetPosition();
                int changeInLives = Integer.parseInt(lives.getShape().getText()) - 1;
                lives.setValue(String.valueOf(changeInLives));
                game.logGameEvent("Srážka s duchem, počet zbývajících životů: " + changeInLives + ".");

                List<ImageView> activeTrail = this.pacman.getActiveTrail();
                gameGroup.getChildren().removeAll(activeTrail);
                pacman.getCreatedPossibleBlocks().removeAll(activeTrail);
                activeTrail.clear();

                if (changeInLives <= 0) {
                    Stage stage = (Stage) gameGroup.getScene().getWindow();
                    GameOverHandler.handleGameOver(gameGroup, stage, game, pacman, map, lives);
                }
            }
        } else {
            if (pacman.getShape().getBoundsInParent().intersects(ghost.getShape().getBoundsInParent())) {
                gameGroup.getChildren().remove(ghost.getShape());
                ghost.setEaten(true);
                int currentScore = pacman.getScore() + 200;
                pacman.setScore(String.valueOf(currentScore));
                game.logGameEvent("Snězen duch, přidáno 200 bodů. Skóre: " + currentScore + ".");
            }
        }
    }

    private boolean touchesActiveTrail() {
        for (ImageView block : pacman.getActiveTrail()) {
            if (ghostImageView.getBoundsInParent().intersects(block.getBoundsInParent()))
                return true;
        }
        return false;
    }

    public void respawnToFreePosition(Group gameGroup) {
        int x = (int) startX;
        int y = (int) startY;
        Random random = new Random();

        while (isPositionTaken(x, y)) {
            x = 20 + random.nextInt((Game.WIDTH - 35 - 20) / 15) * 15;
            y = 50 + random.nextInt((Game.HEIGHT - 70 - 50) / 15) * 15;
        }

        ghostImageView.setX(x);
        ghostImageView.setY(y);
        startX = x;
        startY = y;

        if (originalImage != null) {
            ghostImageView.setImage(originalImage);
        } else {
            java.net.URL fallbackUrl = getClass().getResource("images/orange_ghost.png");
            if (fallbackUrl != null) {
                ghostImageView.setImage(new Image(fallbackUrl.toExternalForm()));
            }
        }

        if (!gameGroup.getChildren().contains(ghostImageView)) {
            gameGroup.getChildren().add(ghostImageView);
        }

        for (int dir = 0; dir < 4; dir++) {
            if (canMoveOrange(dir, 0.5 * orangeSpeedMultiplier)) {
                orangeDirection = dir;
                break;
            }
        }
    }

    public void resetState() {
        this.isStopped = false;
        this.powerUpActive = false;
        this.isEaten = false;
        this.speedX = 1.0;
        this.speedY = 1.0;
        this.orangeSpeedMultiplier = 1.0;
    }

    public void canBeEaten(boolean state) {
        this.powerUpActive = state;
    }

    @Override
    public void checkBoundary() {
    }

    @Override
    public void checkCollisionWithPossibleBlock(Pacman pacman, GameInfo lives, Map map, Group gameGroup) {
    }

    public Image getOriginalImage() {
        return originalImage;
    }

    public boolean isEaten() {
        return isEaten;
    }

    public void setEaten(boolean eaten) {
        this.isEaten = eaten;
    }
}