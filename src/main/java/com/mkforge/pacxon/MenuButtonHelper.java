package com.mkforge.pacxon;

import java.io.IOException;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.layout.StackPane;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MenuButtonHelper {

    private static boolean isButtonAdded = false;
    private static final Logger LOGGER = Logger.getLogger(MenuButtonHelper.class.getName());

    public static void showMenuButton(Group gameGroup, Stage stage, AnimationTimer loop) {
        if (isButtonAdded) return;

        InputStream gameFontStream = GameOverHandler.class.getResourceAsStream("fonts/bangers.ttf");
        Font gameFont = Font.loadFont(gameFontStream, 30);

        Text menuText = new Text("Menu");
        menuText.setFont(gameFont);
        menuText.setFill(Color.WHITE);
        menuText.setStroke(Color.BLACK);
        menuText.setStrokeWidth(0.5);
        double textWidth = menuText.getLayoutBounds().getWidth();
        double buttonWidth = textWidth + 40;

        StackPane menuPane = new StackPane();
        menuPane.setPrefSize(buttonWidth, 50);
        menuPane.setLayoutX((double)Game.WIDTH / 2 - buttonWidth / 2);
        menuPane.setLayoutY((double)Game.HEIGHT / 2 + 30);
        menuPane.setStyle("-fx-background-color: yellow; -fx-background-radius: 2cm;");
        menuPane.getChildren().add(menuText);
        menuPane.setOnMouseClicked(e -> {
            if (loop != null) loop.stop();

            try {
                FXMLLoader loader = new FXMLLoader(MenuButtonHelper.class.getResource("GameView.fxml"));
                Parent root = loader.load();
                GameController controller = loader.getController();
                InputStream logoFontStream = GameOverHandler.class.getResourceAsStream("fonts/bangers.ttf");
                Font logoFont = Font.loadFont(logoFontStream, 50);

                controller.pacxonLbl.setFont(logoFont);
                controller.newGameBtn.setFont(gameFont);
                controller.exitGameBtn.setFont(gameFont);
                controller.menuRoot.getStyleClass().add("menu-root");

                Scene scene = new Scene(root);
                java.net.URL cssUrl = MenuButtonHelper.class.getResource("styles/styles.css");
                if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
                stage.setScene(scene);
                isButtonAdded = false;
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Chyba při návratu do menu", ex);
            }
        });

        gameGroup.getChildren().add(menuPane);
        isButtonAdded = true;
    }
}