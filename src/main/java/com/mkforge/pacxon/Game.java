package com.mkforge.pacxon;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import java.util.Random;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Game extends Application {

    public static Scene scene;
    public static final int WIDTH = 640;
    public static final int HEIGHT = 480;
    public static AnimationTimer loop;
    private Pacman pacman;
    public static final int BLOCKS = 936;
    private final Set<String> loggedEvents = new HashSet<>();
    private final Random random = new Random();
    public static Font logoFont;
    public static Font gameFont;
    private boolean isGameOver = false;
    private static final Logger LOGGER = Logger.getLogger(Game.class.getName());
    private Map map;
    private Group gameGroup;

    @Override
    public void start(Stage primaryStage) {
        try {
            InputStream logoFontStream = getClass().getResourceAsStream("fonts/bangers.ttf");
            logoFont = Font.loadFont(logoFontStream, 50);
            InputStream gameFontStream = getClass().getResourceAsStream("fonts/bangers.ttf");
            gameFont = Font.loadFont(gameFontStream, 30);
            SoundManager.initialize();
            SoundManager.playBackground();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("GameView.fxml"));
            Parent root = loader.load();
            GameController gameController = loader.getController();
            scene = new Scene(root);

            java.net.URL cssUrl = getClass().getResource("styles/styles.css");
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

            gameController.menuRoot.getStyleClass().add("menu-root");
            gameController.pacxonLbl.setFont(logoFont);
            gameController.newGameBtn.setFont(gameFont);
            gameController.exitGameBtn.setFont(gameFont);

            primaryStage.setScene(scene);
            primaryStage.setTitle("PAC-XON");
            primaryStage.setResizable(false);
            primaryStage.setWidth(640);
            primaryStage.setHeight(480);

            try {
                InputStream iconStream = getClass().getResourceAsStream("images/icon.png");
                if (iconStream != null) {
                    Image icon = new Image(iconStream);
                    if (!icon.isError()) primaryStage.getIcons().add(icon);
                }
            } catch (Exception ignored) {}

            primaryStage.show();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Chyba při načítání scény", e);
        }
    }

    public void setGameOver(boolean state) {
        this.isGameOver = state;
    }

    public void startGame(Stage stage) {
        try {
            Files.deleteIfExists(Path.of("gamelog.txt"));
        } catch (IOException ignored) {}

        if (pacman != null) {
            pacman.setGameOver(false);
            pacman.stopMovement();
        }
        isGameOver = false;

        int target = GameController.getTargetPercentage();
        String difficulty = switch (target) {
            case 50 -> "Easy";
            case 90 -> "Hard";
            default -> "Medium";
        };
        logGameEvent("Obtížnost: " + difficulty + " (cíl " + target + "%).");
        logGameEvent("Hra začíná.");

        gameGroup = new Group();
        Scene gameScene = new Scene(gameGroup, scene.getWidth(), scene.getHeight());
        gameScene.setFill(Color.BLACK);

        Text scoreText = new Text();
        Text scoreValue = new Text();
        GameInfo score = new Score(scoreText, scoreValue);
        score.initialize(gameGroup);

        Text livesText = new Text();
        Text livesValue = new Text();
        GameInfo lives = new Lives(livesText, livesValue);
        lives.initialize(gameGroup);

        Text progressText = new Text();
        Text progressValue = new Text();
        Text progressPercent = new Text();
        GameInfo progress = new Progress(progressText, progressValue, progressPercent);
        progress.initialize(gameGroup);

        pacman = new Pacman(score, this);
        pacman.initializePacman();

        map = new Map(this);
        map.initializeMap(gameGroup);
        map.initializeGrid(gameGroup);
        List<Ghost> ghosts = createGhosts(pacman, lives);

        gameScene.setOnKeyPressed(this::movePacmanInGrid);
        gameScene.setOnKeyReleased(event -> {
            KeyCode key = event.getCode();
            if (key == KeyCode.RIGHT || key == KeyCode.LEFT || key == KeyCode.UP || key == KeyCode.DOWN || key == KeyCode.W || key == KeyCode.A || key == KeyCode.S || key == KeyCode.D)
                pacman.stopMovement();
        });

        gameGroup.getChildren().addAll(pacman.getShape());
        pacman.getShape().toFront();

        StackPane restartPane = createIconButton("images/restart.png", Game.WIDTH - 85, () -> {
            loop.stop();
            map.stopPowerUpRefresh();
            gameGroup.getChildren().clear();
            startGame(stage);
        });
        if (restartPane != null) gameGroup.getChildren().add(restartPane);

        StackPane exitPane = createIconButton("images/exit.png", Game.WIDTH - 50, () -> {
            loop.stop();
            map.stopPowerUpRefresh();
            returnToMainMenu(stage);
        });
        if (exitPane != null) gameGroup.getChildren().add(exitPane);

        stage.setScene(gameScene);

        loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                for (Ghost ghost : ghosts) {
                    if (ghost.isEaten()) continue;

                    if (ghost == ghosts.get(0))
                        ghosts.get(0).moveOrangeGhost();
                    else if (ghost == ghosts.get(2))
                        ghosts.get(2).moveRedGhost();
                    else
                        ghost.moveGhost();

                    map.getPowerUp(pacman, gameGroup);
                    ghost.checkCollisionWithPacman(pacman, ghost, map, gameGroup);
                }

                pacman.updateMovement();
                pacman.drawPossibleBlock(gameGroup, ghosts);
                pacman.checkCollisionWithPossibleBlock(pacman, lives, map, gameGroup);
                pacman.checkBoundary();

                double percentage = pacman.getProgressPercentage(BLOCKS);
                progress.getShape().setText(String.format("%.0f", percentage));

                if (percentage >= GameController.getTargetPercentage()) {
                    SoundManager.playWin();
                    logGameEvent("Konec hry, vyhrál jsi s " + (int)percentage + "%!");
                    logGameEvent("Nahrané skóre: " + pacman.getScore() + ".");
                    logGameEvent("Zbývající životy: " + lives.getShape().getText() + ".");

                    Rectangle overlay = new Rectangle(Game.WIDTH, Game.HEIGHT);
                    overlay.setFill(Color.rgb(0, 0, 0, 0.5));
                    overlay.setMouseTransparent(false);
                    overlay.setOnMouseClicked(e -> {});

                    Text win = new Text();
                    win.setFill(Color.web("#00FF00"));
                    win.setText("YOU WIN!");
                    win.setStroke(Color.BLACK);
                    win.setStrokeWidth(0.5);
                    win.setFont(logoFont);
                    win.setX((double)Game.WIDTH / 2 - win.getLayoutBounds().getWidth() / 2);
                    win.setY((double)Game.HEIGHT / 2);
                    win.setVisible(true);

                    gameGroup.getChildren().addAll(overlay, win);
                    isGameOver = true;
                    loop.stop();
                    map.stopPowerUpRefresh();
                    MenuButtonHelper.showMenuButton(gameGroup, stage, loop);
                }
            }
        };

        loop.start();
    }

    private List<Ghost> createGhosts(Pacman pacman, GameInfo lives) {
        List<Ghost> ghosts = new ArrayList<>();
        int xOrangeGhost = 300;
        int yOrangeGhost = 46;
        int xPinkGhost = 10 + random.nextInt(Game.WIDTH - 70);
        int yPinkGhost = 30 + random.nextInt(Game.HEIGHT - 120);
        int xRedGhost = 40 + random.nextInt(Game.WIDTH - 70);
        int yRedGhost = 20 + random.nextInt(Game.HEIGHT - 120);
        int xBlueGhost = 30 + random.nextInt(Game.WIDTH - 70);
        int yBlueGhost = 50 + random.nextInt(Game.HEIGHT - 120);

        Ghost orangeGhost = new Ghost(pacman, lives, this);
        orangeGhost.initializeOrangeGhost(xOrangeGhost, yOrangeGhost);
        gameGroup.getChildren().add(orangeGhost.getShape());
        ghosts.add(orangeGhost);

        Ghost pinkGhost = new Ghost(pacman, lives, this);
        pinkGhost.initializePinkGhost(xPinkGhost, yPinkGhost);
        gameGroup.getChildren().add(pinkGhost.getShape());
        ghosts.add(pinkGhost);

        Ghost redGhost = new Ghost(pacman, lives, this);
        redGhost.initializeRedGhost(xRedGhost, yRedGhost);
        gameGroup.getChildren().add(redGhost.getShape());
        ghosts.add(redGhost);

        Ghost blueGhost = new Ghost(pacman, lives, this);
        blueGhost.initializeBlueGhost(xBlueGhost, yBlueGhost);
        gameGroup.getChildren().add(blueGhost.getShape());
        ghosts.add(blueGhost);
        map.setGhosts(ghosts);

        return ghosts;
    }

    private StackPane createIconButton(String imagePath, double x, Runnable action) {
        InputStream stream = getClass().getResourceAsStream(imagePath);
        if (stream == null) return null;
        Image image = new Image(stream);
        ImageView icon = new ImageView(image);
        icon.setFitWidth(24);
        icon.setFitHeight(24);

        StackPane pane = new StackPane(icon);
        pane.setLayoutX(x);
        pane.setPrefSize(34, 34);
        pane.setStyle("-fx-cursor: hand; -fx-background-color: transparent;");
        pane.setOnMouseClicked(e -> action.run());

        return pane;
    }

    public void movePacmanInGrid(KeyEvent event) {
        if (isGameOver || pacman.isGameOver()) return;

        KeyCode key = event.getCode();
        if (key == KeyCode.RIGHT || key == KeyCode.D)
            pacman.moveRight();
        else if (key == KeyCode.LEFT || key == KeyCode.A)
            pacman.moveLeft();
        else if (key == KeyCode.UP || key == KeyCode.W)
            pacman.moveUp();
        else if (key == KeyCode.DOWN || key == KeyCode.S)
            pacman.moveDown();
    }

    public void logGameEvent(String logMessage) {
        if (!loggedEvents.contains(logMessage)) {
            try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("gamelog.txt", true)))) {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();

                if (loggedEvents.isEmpty())
                    writer.print(dtf.format(now) + " - " + logMessage);
                else
                    writer.print(System.lineSeparator() + dtf.format(now) + " - " + logMessage);

                loggedEvents.add(logMessage);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Nepodařilo se zapsat do gamelog.txt", e);
            }
        }
    }

    private void returnToMainMenu(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("GameView.fxml"));
            Parent root = loader.load();
            GameController controller = loader.getController();
            controller.pacxonLbl.setFont(logoFont);
            controller.newGameBtn.setFont(gameFont);
            controller.exitGameBtn.setFont(gameFont);

            Scene scene = new Scene(root);
            java.net.URL cssUrl = getClass().getResource("styles/styles.css");
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Chyba při návratu do menu", ex);
        }
    }

    public Pacman getPacman() {
        return pacman;
    }

    public Map getMap() {
        return map;
    }
}