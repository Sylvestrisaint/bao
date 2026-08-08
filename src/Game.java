import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.Objects;

public class Game {
    private PaneOrganizer paneOrganizer;
    private Button pause;
    private Timeline timeline;
    private int timeInMinutes;
    private int timeInSeconds;
    private Label timeLeft;
    private Label turnLabel;
    private int currentPlayer;
    private boolean isGameActive = false;

    public Game(PaneOrganizer paneOrganizer) {
        this.paneOrganizer = paneOrganizer;
        this.setupGame();
    }

    private void setupGame() {
        BorderPane gamePane = new BorderPane();
        this.setupBoard(gamePane);
    }

    private void setupBoard(BorderPane gamePane) {
        // Header
        BorderPane topBar = new BorderPane();
        topBar.setPadding(new Insets(20.0));

        Label title = new Label("BAO");
        title.setFont(loadFont(40));
        topBar.setLeft(title);

        // Player turns
        this.turnLabel = new Label("CLICK START TO BEGIN!");
        turnLabel.setFont(loadFont(24));
        BorderPane.setAlignment(turnLabel, Pos.CENTER);
        topBar.setCenter(turnLabel);

        // Timer
        this.timeLeft = new Label("10:00");
        this.timeLeft.setFont(loadFont(24));
        topBar.setRight(this.timeLeft);

        gamePane.setTop(topBar);

        // Board
        Board board = new Board(this, gamePane);

        // Spacer
        Pane rightSpacer = new Pane();
        rightSpacer.setPrefWidth(94);
        gamePane.setRight(rightSpacer);

        // Start/Restart button
        HBox bottomBar = new HBox();
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setPadding(new Insets(18.0));
        Button start = new Button("START");
        start.setOnAction(actionEvent -> {
            this.isGameActive = true;
            this.currentPlayer = 1;
            this.startTimer();
            this.showRestart(start, bottomBar);
            this.turnLabel.setText("PLAYER " + this.currentPlayer + "'s TURN");
            board.flashPits();
        } );
        start.setCursor(Cursor.HAND);
        this.styleButton(start);
        bottomBar.getChildren().add(start);
        gamePane.setBottom(bottomBar);

        // Controls
        VBox controls = new VBox();
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(30.0));
        controls.setSpacing(12.0);

        Button back = new Button();
        back.getStyleClass().add("controls-button");
        back.setCursor(Cursor.HAND);
        back.setGraphic(this.loadImageView("/resources/back.png"));
        back.setOnAction(actionEvent -> {
            this.turnLabel.setVisible(true);
            topBar.setRight(this.timeLeft);
            bottomBar.setVisible(true);
            gamePane.setCenter(board.getBoard());
        });

        Button settings = new Button();
        settings.getStyleClass().add("controls-button");
        settings.setCursor(Cursor.HAND);
        settings.setOnAction(actionEvent -> {
            displayControlsPage(topBar, bottomBar, back);
            gamePane.setCenter(showSettingsPage());
        });
        settings.setGraphic(this.loadImageView("/resources/settings.png"));

        Button info = new Button();
        info.getStyleClass().add("controls-button");
        info.setCursor(Cursor.HAND);
        info.setOnAction(actionEvent -> {
            displayControlsPage(topBar, bottomBar, back);
            gamePane.setCenter(showInfoPage());
        });
        info.setGraphic(this.loadImageView("/resources/info.png"));

        Button exit = new Button();
        exit.getStyleClass().add("controls-button");
        exit.setId("exit");
        exit.setOnAction(actionEvent -> System.exit(0));
        exit.setCursor(Cursor.HAND);
        exit.setGraphic(this.loadImageView("/resources/exit.png"));

        controls.getChildren().addAll(settings, info, exit);
        gamePane.setLeft(controls);

        this.paneOrganizer.showScreen(gamePane);
    }

    private void displayControlsPage(BorderPane topBar, HBox bottomBar, Button back) {
        if (this.timeline != null && this.timeline.getStatus() == Animation.Status.RUNNING) this.pauseGame();
        this.turnLabel.setVisible(false);
        bottomBar.setVisible(false);
        topBar.setRight(back);
    }

    private HBox loadPage(String path, Integer width, Integer height) {
        HBox pane = new HBox();
        Image image = new Image(this.getClass().getResourceAsStream(path));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(Constants.SETTINGS_WIDTH);
        imageView.setFitHeight(Constants.SETTINGS_HEIGHT);
        pane.setAlignment(Pos.CENTER);
        pane.getChildren().add(imageView);
        return pane;
    }

    private HBox showInfoPage() {
        return loadPage("/resources/info-page.png", Constants.INFO_WIDTH, Constants.INFO_HEIGHT);
    }

    private HBox showSettingsPage() {
        return loadPage("/resources/settings-page.png", Constants.SETTINGS_WIDTH, Constants.SETTINGS_HEIGHT);
    }

    private void startTimer() {
        this.timeInMinutes = 9;
        this.timeInSeconds = 60;
        KeyFrame timer = new KeyFrame(Duration.seconds(1), actionEvent -> {
            if (this.timeInMinutes == 0 && this.timeInSeconds == 0) {
                this.endGame();
            }

            if (this.timeInSeconds == 0) {
                this.timeInSeconds = 60;
                this.timeInMinutes -= 1;
            }

            this.timeInSeconds -= 1;
            if (this.timeInSeconds < 10) {
                this.timeLeft.setText(this.timeInMinutes + ":0" + this.timeInSeconds);
            } else {
                this.timeLeft.setText(this.timeInMinutes + ":" + this.timeInSeconds);
            }
        });
        this.timeline = new Timeline(timer);
        this.timeline.setCycleCount(Timeline.INDEFINITE);
        this.timeline.play();
    }

    private void endGame() {}

    private void showRestart(Button start, HBox bottomPane) {
        Button restart = new Button("RESTART");
        restart.setOnAction(actionEvent -> {
            this.timeline.stop();
            this.setupGame();
        });
        restart.setCursor(Cursor.HAND);
        this.styleButton(restart);
        bottomPane.getChildren().clear();

        this.pause = new Button("PAUSE");
        this.pause.setCursor(Cursor.HAND);
        this.styleButton(pause);
        this.pause.setOnAction(actionEvent -> {
            this.pauseGame();
        });
        bottomPane.getChildren().addAll(this.pause, restart);
    }

    private void pauseGame() {
        if (this.gameIsActive()) {
            this.pause.setText("PLAY");
            this.timeline.pause();
            this.turnLabel.setText("GAME PAUSED");
        } else {
            this.pause.setText("PAUSE");
            this.turnLabel.setText("PLAYER " + this.currentPlayer + "'S TURN");
            this.timeline.play();
        }
        this.isGameActive = !this.isGameActive;
    }

    public boolean gameIsActive() {
        return this.isGameActive;
    }

    public void switchPlayer() {
        this.currentPlayer = (this.currentPlayer == 1) ? 2 : 1;
        this.turnLabel.setText("PLAYER " + this.currentPlayer +"'S TURN");
    }

    private ImageView loadImageView(String path) {
        Image icon = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream(path)));
        ImageView iconImageView = new ImageView(icon);
        iconImageView.setFitWidth(30);
        iconImageView.setFitHeight(30);
        return iconImageView;
    }

    public static Font loadFont(int size) {
        Font customFont = Font.loadFont(Game.class.
                getResourceAsStream("fonts/noot-regular.ttf"),size);
        if (customFont == null) { customFont = Font.getDefault();}
        return customFont;
    }

    /**
     * Customizes buttons by loading a retro digital font, setting text colors, and applying CSS styles.
     * @param button The button being customized.
     */
    private void styleButton(Button button) {
        button.getStyleClass().add("full-image-button");
        button.setFont(loadFont(20));
        button.setTextFill(Color.BLACK);
    }

}
