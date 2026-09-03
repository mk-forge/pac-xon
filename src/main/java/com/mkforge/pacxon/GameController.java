package com.mkforge.pacxon;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameController {

	@FXML public Label pacxonLbl;
	@FXML public Button newGameBtn;
	@FXML public Button exitGameBtn;
	@FXML public AnchorPane menuRoot;
	@FXML public Label difficultyLbl;
	@FXML public HBox difficultyBar;
	@FXML public ToggleButton easyDiffBtn;
	@FXML public ToggleButton mediumDiffBtn;
	@FXML public ToggleButton hardDiffBtn;
	@FXML public ToggleButton musicToggleBtn;
	@FXML public ToggleButton soundToggleBtn;

	private static final Logger LOGGER = Logger.getLogger(GameController.class.getName());
	private static boolean musicEnabled = true;
	private static boolean soundEffectsEnabled = true;
	private static int targetPercentage = 70;

	public void initialize() {
		newGameBtn.setOnAction(event -> startGame());
		exitGameBtn.setOnAction(event -> endGame());

		ToggleGroup diffGroup = new ToggleGroup();
		easyDiffBtn.setToggleGroup(diffGroup);
		mediumDiffBtn.setToggleGroup(diffGroup);
		hardDiffBtn.setToggleGroup(diffGroup);

		mediumDiffBtn.setSelected(true);
		targetPercentage = 70;

		diffGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == easyDiffBtn)
				targetPercentage = 50;
			else if (newValue == mediumDiffBtn)
				targetPercentage = 70;
			else if (newValue == hardDiffBtn)
				targetPercentage = 90;
		});

		musicToggleBtn.setOnAction(event -> toggleMusic());
		soundToggleBtn.setOnAction(event -> toggleSoundEffects());

		musicToggleBtn.setSelected(musicEnabled);
		soundToggleBtn.setSelected(soundEffectsEnabled);

		updateMusicButton();
		updateSoundButton();
	}

	private void toggleMusic() {
		musicEnabled = musicToggleBtn.isSelected();
		updateMusicButton();

		if (!musicEnabled)
			SoundManager.stopBackground();
		else
			SoundManager.playBackground();
	}

	private void toggleSoundEffects() {
		soundEffectsEnabled = soundToggleBtn.isSelected();
		updateSoundButton();
	}

	private void updateMusicButton() {
		String iconPath = musicEnabled ? "images/music.png" : "images/music-off.png";
		ImageView icon = loadIcon(iconPath);
		if (icon != null) musicToggleBtn.setGraphic(icon);
	}

	private void updateSoundButton() {
		String iconPath = soundEffectsEnabled ? "images/volume.png" : "images/volume-off.png";
		ImageView icon = loadIcon(iconPath);
		if (icon != null) soundToggleBtn.setGraphic(icon);
	}

	private ImageView loadIcon(String path) {
		try {
			InputStream inputStream = getClass().getResourceAsStream(path);
			if (inputStream == null) {
				LOGGER.log(Level.WARNING, "Ikona nenalezena: " + path);
				return null;
			}

			Image image = new Image(inputStream);
			ImageView icon = new ImageView(image);
			icon.setFitWidth(24);
			icon.setFitHeight(24);
			return icon;
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Chyba při načítání ikony: " + path, e);
			return null;
		}
	}

	public static boolean isMusicEnabled() {
		return musicEnabled;
	}

	public static boolean areSoundEffectsDisabled() {
		return !soundEffectsEnabled;
	}

	public static int getTargetPercentage() {
		return targetPercentage;
	}

	public void startGame() {
		Stage stage = (Stage) newGameBtn.getScene().getWindow();
		Game game = new Game();
		game.startGame(stage);
	}

	public void endGame() {
		System.exit(0);
	}
}