package com.mkforge.pacxon;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.InputStream;

public class GameOverHandler {

    public static void handleGameOver(Group gameGroup, Stage stage, Game game, Pacman pacman, Map map, GameInfo lives) {
        SoundManager.playLose();
        game.logGameEvent("Konec hry, prohrál jsi!");
        game.logGameEvent("Nahrané skóre: " + pacman.getScore() + ".");
        game.logGameEvent("Zbývající životy: " + lives.getShape().getText() + ".");
        InputStream gameFontStream = GameOverHandler.class.getResourceAsStream("fonts/bangers.ttf");
        Font gameFont = Font.loadFont(gameFontStream, 50);

        Rectangle overlay = new Rectangle(Game.WIDTH, Game.HEIGHT);
        overlay.setFill(Color.rgb(0, 0, 0, 0.5));
        overlay.setMouseTransparent(false);
        overlay.setOnMouseClicked(e -> {});

        Text gameOver = new Text();
        gameOver.setFill(Color.RED);
        gameOver.setText("YOU LOSE!");
        gameOver.setStroke(Color.BLACK);
        gameOver.setStrokeWidth(0.5);
        gameOver.setFont(gameFont);
        gameOver.setX((double)Game.WIDTH / 2 - gameOver.getLayoutBounds().getWidth() / 2);
        gameOver.setY((double)Game.HEIGHT / 2);
        gameOver.setVisible(true);

        pacman.setGameOver(true);
        game.setGameOver(true);
        gameGroup.getChildren().addAll(overlay, gameOver);
        Game.loop.stop();
        map.stopPowerUpRefresh();
        MenuButtonHelper.showMenuButton(gameGroup, stage, Game.loop);
    }
}