package com.mkforge.pacxon;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Progress extends GameInfo {

    private final Text percentText;

    public Progress(Text titleText, Text valueText, Text percentText) {
        super(titleText, valueText);
        this.percentText = percentText;
    }

    @Override
    public void initialize(Group gameGroup) {
        Font gameFont = loadGameFont();
        int target = GameController.getTargetPercentage();
        configureText(titleText, 320, "Progress: ", Color.YELLOW, gameFont);
        configureText(valueText, titleText.getX() + titleText.getLayoutBounds().getWidth(), "0", Color.WHITE, gameFont);

        valueText.textProperty().addListener((obs, oldVal, newVal) -> {
            double newX = valueText.getX() + valueText.getLayoutBounds().getWidth() + 5;
            percentText.setX(newX);
        });

        double percentX = valueText.getX() + valueText.getLayoutBounds().getWidth() + 5;
        configureText(percentText, percentX, "/" + target + "%", Color.YELLOW, gameFont);
        addToGroup(gameGroup);
        gameGroup.getChildren().add(percentText);
    }
}