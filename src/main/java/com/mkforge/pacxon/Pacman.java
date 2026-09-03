package com.mkforge.pacxon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class Pacman extends GameEntity implements Collisionable {
	private final ImageView pacmanView;
	private final Image possibleBlock;
	private final Image blockImage;
	private final List<ImageView> possibleBlocks;
	private final List<ImageView> filledBlocks;
	private final List<ImageView> activeTrail;
	private final GameInfo score;
	private boolean gameOver = false;
	private long lastMoveTime = 0;
	private long moveDelay;
	private final Game game;
	private KeyCode currentDirection = null;

	public Pacman(GameInfo score, Game game) {
		super();
		this.score = score;
		this.game = game;
		this.possibleBlocks = new ArrayList<>();
		this.filledBlocks = new ArrayList<>();
		this.activeTrail = new ArrayList<>();
		this.moveDelay = 70000000;

		java.net.URL pacmanUrl = getClass().getResource("images/pacman.png");
		if (pacmanUrl != null) {
			Image pacman = new Image(pacmanUrl.toExternalForm());
			pacmanView = new ImageView(pacman);
		} else {
			pacmanView = new ImageView();
		}

		java.net.URL possibleBlockUrl = getClass().getResource("images/possible_block.png");
		if (possibleBlockUrl != null)
			possibleBlock = new Image(possibleBlockUrl.toExternalForm());
		else
			possibleBlock = null;

		java.net.URL blockUrl = getClass().getResource("images/block.png");
		if (blockUrl != null)
			blockImage = new Image(blockUrl.toExternalForm());
		else
			blockImage = null;
	}

	public void initializePacman() {
		pacmanView.setX(5);
		pacmanView.setY(35);
		pacmanView.setFitHeight(14);
		pacmanView.setFitWidth(14);
	}

	public ImageView getShape() {
		return pacmanView;
	}

	@Override
	public void setPacmanSpeed(double pacmanSpeed, boolean powerUpActive) {
		this.speed = pacmanSpeed;
		this.powerUpActive = powerUpActive;

		if (pacmanSpeed == 2)
			this.moveDelay = 50000000;
		else
			this.moveDelay = 70000000;
	}

	public void moveRight() {
		currentDirection = KeyCode.RIGHT;
	}

	public void moveLeft() {
		currentDirection = KeyCode.LEFT;
	}

	public void moveUp() {
		currentDirection = KeyCode.UP;
	}

	public void moveDown() {
		currentDirection = KeyCode.DOWN;
	}

	public void stopMovement() {
		currentDirection = null;
	}

	public void updateMovement() {
		if (currentDirection == null) return;
		if (gameOver) return;

		long now = System.nanoTime();
		if (now - lastMoveTime < moveDelay) return;

		switch (currentDirection) {
			case RIGHT -> {
				pacmanView.setX(pacmanView.getX() + 15);
				pacmanView.setRotate(0);
				SoundManager.playMove();
			} case LEFT -> {
				pacmanView.setX(pacmanView.getX() - 15);
				pacmanView.setRotate(-180);
				SoundManager.playMove();
			} case UP -> {
				pacmanView.setY(pacmanView.getY() - 15);
				pacmanView.setRotate(-90);
				SoundManager.playMove();
			} case DOWN -> {
				pacmanView.setY(pacmanView.getY() + 15);
				pacmanView.setRotate(90);
				SoundManager.playMove();
			}
		}
		lastMoveTime = now;
	}

	@Override
	public void checkBoundary() {
		if (pacmanView.getX() > Game.WIDTH - 25)
			pacmanView.setX(pacmanView.getX() - 15 * speed);
		else if (pacmanView.getX() < 5)
			pacmanView.setX(pacmanView.getX() + 15 * speed);
		else if (pacmanView.getY() > Game.HEIGHT - 70)
			pacmanView.setY(pacmanView.getY() - 15 * speed);
		else if (pacmanView.getY() < 35)
			pacmanView.setY(pacmanView.getY() + 15 * speed);
	}

	public void resetPosition() {
		pacmanView.setX(5);
		pacmanView.setY(35);
	}

	public void drawPossibleBlock(Group gameGroup, List<Ghost> ghosts) {
		double nextXPos = pacmanView.getX();
		double nextYPos = pacmanView.getY();

		if (pacmanView.getRotate() == 0)
			nextXPos -= 15;
		else if (pacmanView.getRotate() == -180)
			nextXPos += 15;
		else if (pacmanView.getRotate() == -90)
			nextYPos += 15;
		else if (pacmanView.getRotate() == 90)
			nextYPos -= 15;

		boolean isBlockPresent = false;
		for (ImageView existingBlock : possibleBlocks) {
			if (existingBlock.getX() == nextXPos && existingBlock.getY() == nextYPos) {
				isBlockPresent = true;
				break;
			}
		}

		if (!isBlockPresent && nextXPos > 5 && nextXPos < Game.WIDTH - 35 && nextYPos > 35 && nextYPos < Game.HEIGHT - 70) {
			ImageView newPossibleBlockIV = new ImageView(possibleBlock);
			newPossibleBlockIV.setFitHeight(15);
			newPossibleBlockIV.setFitWidth(15);
			newPossibleBlockIV.setX(nextXPos);
			newPossibleBlockIV.setY(nextYPos);

			gameGroup.getChildren().addAll(newPossibleBlockIV);
			possibleBlocks.add(newPossibleBlockIV);
			activeTrail.add(newPossibleBlockIV);
			pacmanView.toFront();

			Map map = game.getMap();
			if (map != null) {
				for (PowerUp powerUp : map.getActivePowerUps())
					powerUp.view().toFront();
			}
		} else if (!activeTrail.isEmpty() && isOnSafeGround()) {
			closeTrailAndFillArea(gameGroup, ghosts);
		}

		pacmanView.toFront();

		Map map = game.getMap();
		if (map != null) {
			for (PowerUp powerUp : map.getActivePowerUps())
				powerUp.view().toFront();
		}
	}

	public List<ImageView> getCreatedPossibleBlocks() {
		return possibleBlocks;
	}

	public List<ImageView> getActiveTrail() {
		return activeTrail;
	}

	public boolean isOnSafeGround() {
		double x = pacmanView.getX();
		double y = pacmanView.getY();

		if (x <= 5 || x >= Game.WIDTH - 35 || y <= 35 || y >= Game.HEIGHT - 70)
			return true;

		for (ImageView block : possibleBlocks) {
			if (block.getX() == x && block.getY() == y && !activeTrail.contains(block))
				return true;
		}

		return false;
	}

	private void closeTrailAndFillArea(Group gameGroup, List<Ghost> ghosts) {
		List<ImageView> validTrail = filterValidTrail();
		activeTrail.clear();
		activeTrail.addAll(validTrail);

		Set<String> blockedCells = buildBlockedCells();
		processComponents(blockedCells, ghosts, gameGroup);

		convertTrailToBlocks();
		activeTrail.clear();

		Map map = game.getMap();
		if (map != null) {
			for (PowerUp powerUp : map.getActivePowerUps())
				powerUp.view().toFront();
		}
		pacmanView.toFront();
	}

	private List<ImageView> filterValidTrail() {
		List<ImageView> validTrail = new ArrayList<>();
		for (ImageView block : activeTrail) {
			if (possibleBlocks.contains(block))
				validTrail.add(block);
		}
		return validTrail;
	}

	private Set<String> buildBlockedCells() {
		Set<String> blockedCells = new HashSet<>();
		for (ImageView block : possibleBlocks)
			blockedCells.add(cellKey(block.getX(), block.getY()));

		return blockedCells;
	}

	private void processComponents(Set<String> blockedCells, List<Ghost> ghosts, Group gameGroup) {
		Set<String> globallyVisited = new HashSet<>();

		for (double x = 20; x < Game.WIDTH - 35; x += 15) {
			for (double y = 50; y < Game.HEIGHT - 70; y += 15) {
				String currentKey = cellKey(x, y);
				if (blockedCells.contains(currentKey) || globallyVisited.contains(currentKey))
					continue;

				List<Point2D> region = new ArrayList<>();
				Queue<Point2D> cellQueue = new LinkedList<>();
				cellQueue.add(new Point2D(x, y));
				globallyVisited.add(currentKey);

				while (!cellQueue.isEmpty()) {
					Point2D currentCell = cellQueue.poll();
					region.add(currentCell);

					Point2D[] neighbors = {
							new Point2D(currentCell.getX() + 15, currentCell.getY()),
							new Point2D(currentCell.getX() - 15, currentCell.getY()),
							new Point2D(currentCell.getX(), currentCell.getY() + 15),
							new Point2D(currentCell.getX(), currentCell.getY() - 15)
					};

					for (Point2D neighbor : neighbors) {
						double neighborX = neighbor.getX();
						double neighborY = neighbor.getY();
						if (neighborX >= 20 && neighborX < Game.WIDTH - 35 && neighborY >= 50 && neighborY < Game.HEIGHT - 70) {
							String neighborKey = cellKey(neighborX, neighborY);
							if (!blockedCells.contains(neighborKey) && !globallyVisited.contains(neighborKey)) {
								globallyVisited.add(neighborKey);
								cellQueue.add(new Point2D(neighborX, neighborY));
							}
						}
					}
				}

				if (!containsGhost(region, ghosts))
					fillComponent(region, gameGroup);
			}
		}
	}

	private boolean containsGhost(List<Point2D> region, List<Ghost> ghosts) {
		for (Ghost ghost : ghosts) {
			if (ghost.isEaten()) continue;

			double ghostX = ghost.getShape().getX();
			double ghostY = ghost.getShape().getY();
			for (Point2D cell : region) {
				if (Math.abs(cell.getX() - ghostX) < 15 && Math.abs(cell.getY() - ghostY) < 15)
					return true;
			}
		}
		return false;
	}

	private void fillComponent(List<Point2D> region, Group gameGroup) {
		for (Point2D cell : region) {
			ImageView fillBlockView = new ImageView(blockImage);
			fillBlockView.setFitHeight(15);
			fillBlockView.setFitWidth(15);
			fillBlockView.setX(cell.getX());
			fillBlockView.setY(cell.getY());

			gameGroup.getChildren().addAll(fillBlockView);
			possibleBlocks.add(fillBlockView);
			filledBlocks.add(fillBlockView);

			int scoreGain = Integer.parseInt(score.getShape().getText()) + 50;
			score.setScore(Integer.toString(scoreGain));
		}
	}

	private void convertTrailToBlocks() {
		for (ImageView block : activeTrail) {
			if (blockImage != null && possibleBlocks.contains(block)) {
				block.setImage(blockImage);
				filledBlocks.add(block);
				int scoreValue = Integer.parseInt(score.getShape().getText()) + 50;
				score.setScore(Integer.toString(scoreValue));
			}
		}
	}

	private static String cellKey(double x, double y) {
		return (int)x + ":" + (int)y;
	}

	@Override
	public void checkCollisionWithPossibleBlock(Pacman pacman, GameInfo lives, Map map, Group gameGroup) {
		List<ImageView> blocks = new ArrayList<>(activeTrail);

		for (ImageView block : blocks) {
			if (block.getX() == pacmanView.getX() && block.getY() == pacmanView.getY()) {
				resetPosition();
				int changeInLives = Integer.parseInt(lives.getShape().getText()) - 1;
				lives.setValue(String.valueOf(changeInLives));
				game.logGameEvent("Srážka s rozestavěnými bloky, počet zbývajících životů: " + changeInLives + ".");

				gameGroup.getChildren().removeAll(activeTrail);
				possibleBlocks.removeAll(activeTrail);
				activeTrail.clear();

				if (changeInLives <= 0) {
					Stage stage = (Stage)gameGroup.getScene().getWindow();
					GameOverHandler.handleGameOver(gameGroup, stage, game, pacman, map, lives);
				}
			}
		}
	}

	public double getProgressPercentage(int totalBlocks) {
		int totalDrawnBlocks = filledBlocks.size();
		return Math.min((double)totalDrawnBlocks / totalBlocks * 100, 100);
	}

	@Override
	public void checkCollisionWithPacman(Pacman pacman, Ghost ghost, Map map, Group gameGroup) {
	}

	public void setGameOver(boolean gameOver) {
		this.gameOver = gameOver;
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public List<ImageView> getFilledBlocks() {
		return filledBlocks;
	}

	public int getScore() {
		return Integer.parseInt(score.getShape().getText());
	}

	public void setScore(String score) {
		this.score.setScore(score);
	}
}