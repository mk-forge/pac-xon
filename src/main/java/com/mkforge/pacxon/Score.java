package com.mkforge.pacxon;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Score extends GameInfo {

    public Score(Text titleText, Text valueText) {
        super(titleText, valueText);
    }

    @Override
    public void initialize(Group gameGroup) {
        Font gameFont = loadGameFont();
        configureText(titleText, 130, "Score: ", Color.YELLOW, gameFont);
        configureText(valueText, titleText.getX() + titleText.getLayoutBounds().getWidth(), "0", Color.WHITE, gameFont);
        addToGroup(gameGroup);
    }

    public void setScore(String score) {
        valueText.setText(score);
    }
}