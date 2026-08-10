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
    private Board board;
    private BorderPane gamePane;
    private HBox bottomBar;
    private Button pause;
    private Button restart;
    private Timeline timeline;
    private int timeInMinutes;
    private int timeInSeconds;
    private Label timeLeft;
    private Label turnLabel;
    private int currentPlayer;
    private boolean isGameActive = false;

    /**
     * Instantiates a new standard human vs human game.
     * @param paneOrganizer The screen manager used to switch between different pages.
     */
    public Game(PaneOrganizer paneOrganizer) {
        this.paneOrganizer = paneOrganizer;
        this.gamePane = new BorderPane();
        this.setupGameView();
    }

    /**
     * Sets up the initial game state i.e board, timer, and control buttons.
     */
    private void setupGameView() {
        // Board
        this.board = new Board(this, this.gamePane);

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
        this.timeLeft = new Label(this.timeInMinutes + ":00");
        this.timeLeft.setFont(loadFont(24));
        topBar.setRight(this.timeLeft);

        this.gamePane.setTop(topBar);

        // Spacer
        Pane rightSpacer = new Pane();
        rightSpacer.setPrefWidth(94);
        this.gamePane.setRight(rightSpacer);

        // Start/Restart button
        this.bottomBar = new HBox();
        this.bottomBar.setAlignment(Pos.CENTER);
        this.bottomBar.setPadding(new Insets(18.0));
        Button start = new Button("START");
        start.setOnAction(actionEvent -> {
            this.isGameActive = true;
            this.currentPlayer = 1;
            this.startTimer();
            this.showPauseAndRestart();
            this.turnLabel.setText("PLAYER " + this.currentPlayer + "'s TURN");
            board.flashPits();
        } );
        start.setCursor(Cursor.HAND);
        this.styleButton(start);
        this.bottomBar.getChildren().add(start);
        this.gamePane.setBottom(this.bottomBar);

        // Controls
        VBox controls = new VBox();
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(30.0));
        controls.setSpacing(12.0);

        Button back = new Button();
        back.getStyleClass().add("controls-button");
        back.setCursor(Cursor.HAND);
        back.setGraphic(this.loadIcon("/resources/back.png"));
        back.setOnAction(actionEvent -> {
            this.turnLabel.setVisible(true);
            topBar.setRight(this.timeLeft);
            this.bottomBar.setVisible(true);
            this.gamePane.setCenter(board.getBoard());
        });

        Button settings = new Button();
        settings.getStyleClass().add("controls-button");
        settings.setCursor(Cursor.HAND);
        settings.setOnAction(actionEvent -> {
            prepControlsPage(topBar, back);
            this.gamePane.setCenter(loadPage(
                    "/resources/settings-page.png",
                    Constants.PAGE_WIDTH,
                    Constants.PAGE_HEIGHT
            ));
        });
        settings.setGraphic(this.loadIcon("/resources/settings.png"));

        Button info = new Button();
        info.getStyleClass().add("controls-button");
        info.setCursor(Cursor.HAND);
        info.setOnAction(actionEvent -> {
            prepControlsPage(topBar, back);
            this.gamePane.setCenter(loadPage(
                    "/resources/info-page.png",
                    Constants.PAGE_WIDTH,
                    Constants.PAGE_HEIGHT
            ));
        });
        info.setGraphic(this.loadIcon("/resources/info.png"));

        Button exit = new Button();
        exit.getStyleClass().add("controls-button");
        exit.setId("exit");
        exit.setOnAction(actionEvent -> System.exit(0));
        exit.setCursor(Cursor.HAND);
        exit.setGraphic(this.loadIcon("/resources/exit.png"));

        controls.getChildren().addAll(settings, info, exit);
        this.gamePane.setLeft(controls);

        this.paneOrganizer.showScreen(this.gamePane);
    }

    /**
     * Pauses the current game and hides the top and bottom bar
     * @param topBar - pane consisting of the header, player turn label, and timer
     * @param back - back navigation button
     */
    private void prepControlsPage(BorderPane topBar, Button back) {
        if (this.timeline != null && this.timeline.getStatus() == Animation.Status.RUNNING) this.pauseGame();
        this.turnLabel.setVisible(false);
        this.bottomBar.setVisible(false);
        topBar.setRight(back);
    }

    /**
     * Loads the image from path and adds it to a layout pane
     * @param path - image path
     * @return layout pane containing the loaded image
     */
    private HBox loadPage(String path, int width, int height) {
        HBox pane = new HBox();
        Image image = new Image(this.getClass().getResourceAsStream(path));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        pane.setAlignment(Pos.CENTER);
        pane.getChildren().add(imageView);
        return pane;
    }

    /**
     * Starts the timer at the beginning of a new game
     */
    private void startTimer() {
        KeyFrame timer = new KeyFrame(Duration.seconds(1), actionEvent -> {
            if (this.timeInMinutes == 0 && this.timeInSeconds == 0) {
                this.endGame();
                return;
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

        this.timeInMinutes = 10;
        this.timeInSeconds = 0;
        this.timeline = new Timeline(timer);
        this.timeline.setCycleCount(Timeline.INDEFINITE);
        this.timeline.play();
    }

    /**
     * Ends game and displays winner
     */
    private void endGame() {
        this.timeline.stop();
        this.turnLabel.setVisible(false);
        this.bottomBar.getChildren().remove(this.pause);
        this.restart.setText("PLAY AGAIN");

        int countOne = this.board.getPlayerOneBeadCount();
        int countTwo = this.board.getPlayerTwoBeadCount();
        HBox promptPane;
        if (countOne > countTwo) {
            promptPane = loadPage(
                    "/resources/winner1.png",
                    Constants.PROMPT_WIDTH,
                    Constants.PROMPT_HEIGHT);
        }
        else if (countTwo > countOne) {
            promptPane = loadPage(
                    "/resources/winner2.png",
                    Constants.PROMPT_WIDTH,
                    Constants.PROMPT_HEIGHT);
        }
        else {
            promptPane = loadPage(
                    "/resources/tie.png",
                    Constants.PROMPT_WIDTH,
                    Constants.PROMPT_HEIGHT);
        }

        StackPane stack = new StackPane();
        Label outcome = new Label(countOne + " : " + countTwo);
        outcome.setFont(loadFont(24));
        stack.getChildren().addAll(promptPane, outcome);
        this.gamePane.setCenter(stack);
    }

    /**
     * Display the pause and restart buttons when a new game begins
     */
    private void showPauseAndRestart() {
        this.restart = new Button("RESTART");
        this.restart.setOnAction(actionEvent -> {
            this.timeline.stop();
            this.setupGameView();
        });
        this.restart.setCursor(Cursor.HAND);
        this.styleButton(this.restart);
        this.bottomBar.getChildren().clear();

        this.pause = new Button("PAUSE");
        this.pause.setCursor(Cursor.HAND);
        this.styleButton(pause);
        this.pause.setOnAction(actionEvent -> {
            this.pauseGame();
        });
        this.bottomBar.getChildren().addAll(this.pause, restart);
    }

    /**
     * Pauses an ongoing game and plays a paused game
     */
    private void pauseGame() {
        if (this.gameIsActive()) {
            this.pause.setText("RESUME");
            this.timeline.pause();
            this.turnLabel.setText("GAME PAUSED");
        } else {
            this.pause.setText("PAUSE");
            this.turnLabel.setText("PLAYER " + this.currentPlayer + "'S TURN");
            this.timeline.play();
        }
        this.isGameActive = !this.isGameActive;
    }

    /**
     * Checks whether the current game is active i.e has begun and is not paused
     * @return whether the game is active or not
     */
    public boolean gameIsActive() {
        return this.isGameActive;
    }

    /**
     * Switches turns between players and updates the appropriate labels
     */
    public void switchPlayer() {
        this.currentPlayer = (this.currentPlayer == 1) ? 2 : 1;
        this.turnLabel.setText("PLAYER " + this.currentPlayer +"'S TURN");
    }

    /**
     * Loads resources used as icons in the controls section
     * @param path - image path
     * @return ImageView to be used as graphic on the buttons
     */
    private ImageView loadIcon(String path) {
        Image icon = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream(path)));
        ImageView iconImageView = new ImageView(icon);
        iconImageView.setFitWidth(30);
        iconImageView.setFitHeight(30);
        return iconImageView;
    }

    /**
     * Loads imported font
     * @param size - font size
     * @return sized font
     */
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
