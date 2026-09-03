package com.mkforge.pacxon;

import java.io.InputStream;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public abstract class GameInfo {
    protected Text titleText;
    protected Text valueText;

    public GameInfo(Text titleText, Text valueText) {
        this.titleText = titleText;
        this.valueText = valueText;
    }

    public abstract void initialize(Group gameGroup);

    public Text getShape() {
        return valueText;
    }

    public void setValue(String value) {
        valueText.setText(value);
    }

    protected void configureText(Text text, double x, String content, Color color, Font font) {
        text.setX(x);
        text.setY(30);
        text.setFill(color);
        text.setText(content);
        text.setFont(font);
    }

    protected void addToGroup(Group group) {
        group.getChildren().addAll(titleText, valueText);
    }

    protected Font loadGameFont() {
        InputStream gameFontStream = getClass().getResourceAsStream("fonts/bangers.ttf");
        return Font.loadFont(gameFontStream, 30);
    }

    public void setScore(String score) {
        setValue(score);
    }
}