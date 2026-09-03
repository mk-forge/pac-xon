package com.mkforge.pacxon;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import java.util.List;

public record PowerUp(ImageView view, String type) {

    public void applyEffect(GameEntity pacman, List<Ghost> ghosts, Game game, Timeline peachTimeline, Timeline strawberryTimeline) {
        switch (type) {
            case "peach.png" -> {
                game.logGameEvent("Snězen power up: peach.");
                game.logGameEvent("Nyní se všichni duchové hýbají pomaleji.");
                applyPeachEffect(ghosts, peachTimeline);
            } case "strawberry.png" -> {
                game.logGameEvent("Snězen power up: strawberry.");
                game.logGameEvent("Nyní jsou všichni duchové pozastaveni.");
                applyStrawberryEffect(ghosts, strawberryTimeline);
            } case "cherry.png" -> {
                game.logGameEvent("Snězen power up: cherry.");
                game.logGameEvent("Nyní se hýbeš rychleji.");
                pacman.setPacmanSpeed(2, true);
                applyCherryEffect(pacman);
            } case "ball.png" -> {
                game.logGameEvent("Snězen power up: ball.");
                game.logGameEvent("Nyní můžeš jíst duchy.");
            }
        }
    }

    private void applyPeachEffect(List<Ghost> ghosts, Timeline peachTimeline) {
        if (peachTimeline != null) peachTimeline.stop();

        for (Ghost ghost : ghosts) {
            if (!ghost.isEaten())
                ghost.setSpeed(0.5, 0.5, false);
        }

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            for (Ghost ghost : ghosts) {
                if (!ghost.isEaten())
                    ghost.setSpeed(1.0, 1.0, false);
            }
        }));
        timeline.setCycleCount(1);
        timeline.play();
    }

    private void applyStrawberryEffect(List<Ghost> ghosts, Timeline strawberryTimeline) {
        if (strawberryTimeline != null) strawberryTimeline.stop();

        for (Ghost ghost : ghosts) {
            if (!ghost.isEaten())
                ghost.stopGhost();
        }

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            for (Ghost ghost : ghosts) {
                if (!ghost.isEaten())
                    ghost.setSpeed(1.0, 1.0, false);
            }
        }));
        timeline.setCycleCount(1);
        timeline.play();
    }

    private void applyCherryEffect(GameEntity pacman) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> pacman.setPacmanSpeed(1, false)));
        timeline.setCycleCount(1);
        timeline.play();
    }
}