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

import java.util.Objects;

public class Game {
    private PaneOrganizer paneOrganizer;
    private Label turnLabel;
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
        Label timer = new Label("9:59");
        timer.setFont(loadFont(24));
        topBar.setRight(timer);

        gamePane.setTop(topBar);

        // Controls
        VBox controls = new VBox();
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(30.0));
        controls.setSpacing(12.0);

        Button settings = new Button();
        settings.getStyleClass().add("controls-button");
        settings.setGraphic(this.loadImageView("/resources/settings.png"));

        Button info = new Button();
        info.getStyleClass().add("controls-button");
        info.setGraphic(this.loadImageView("/resources/info.png"));

        Button exit = new Button();
        exit.getStyleClass().add("controls-button");
        exit.setGraphic(this.loadImageView("/resources/exit.png"));

        controls.getChildren().addAll(settings, info, exit);
        gamePane.setLeft(controls);

        // Board
        new Board(this, gamePane);

        // Spacer
        Pane rightSpacer = new Pane();
        rightSpacer.setPrefWidth(94);
        gamePane.setRight(rightSpacer);

        // Start/Restart button
        HBox bottomPane = new HBox();
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.setPadding(new Insets(18.0));
        Button start = new Button("START");
        start.setOnAction(actionEvent -> {
            this.isGameActive = true;
            this.showRestart(start, bottomPane);
            this.switchPlayer("PLAYER 1'S TURN");
        } );
        start.setCursor(Cursor.HAND);
        this.styleButton(start);
        bottomPane.getChildren().add(start);
        gamePane.setBottom(bottomPane);

        this.paneOrganizer.showScreen(gamePane);
    }

    private void showRestart(Button start, HBox bottomPane) {
        Button restart = new Button("RESTART");
        restart.setOnAction(actionEvent -> {
            bottomPane.getChildren().remove(restart);
            bottomPane.getChildren().add(start);
        });
        restart.setCursor(Cursor.HAND);
        this.styleButton(restart);
        bottomPane.getChildren().remove(start);
        bottomPane.getChildren().add(restart);
    }

    public boolean gameIsActive() {
        return this.isGameActive;
    }

    public void switchPlayer(String newTurn) {
        this.turnLabel.setText(newTurn);
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
