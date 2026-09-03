package com.mkforge.pacxon;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Lives extends GameInfo {

    public Lives(Text titleText, Text valueText) {
        super(titleText, valueText);
    }

    @Override
    public void initialize(Group gameGroup) {
        Font gameFont = loadGameFont();
        configureText(titleText, 10, "Lives: ", Color.YELLOW, gameFont);
        configureText(valueText, titleText.getX() + titleText.getLayoutBounds().getWidth(), "5", Color.WHITE, gameFont);
        addToGroup(gameGroup);
    }
}