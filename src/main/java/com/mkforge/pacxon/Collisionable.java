package com.mkforge.pacxon;

import javafx.scene.Group;

public interface Collisionable {
	void checkCollisionWithPacman(Pacman pacman, Ghost ghost, Map map, Group gameGroup);
	void checkCollisionWithPossibleBlock(Pacman pacman, GameInfo lives, Map map, Group gameGroup);
	void checkBoundary();
}