import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Objects;

public class Game {
    private PaneOrganizer paneOrganizer;
    public Game(PaneOrganizer paneOrganizer) {
        this.paneOrganizer = paneOrganizer;
        this.setupGame();
    }

    private void setupGame() {
        BorderPane gamePane = new BorderPane();
        this.setupBoard(gamePane);
    }

    private void setupBoard(BorderPane gamePane) {
        BorderPane topBar = new BorderPane();
        topBar.setPadding(new Insets(20.0));

        Label title = new Label("BAO");
        title.setFont(loadFont(40));
        topBar.setLeft(title);

        Label turnLabel = new Label("PLAYER 1'S TURN");
        turnLabel.setFont(loadFont(24));
        BorderPane.setAlignment(turnLabel, Pos.CENTER);
        topBar.setCenter(turnLabel);

        Label timer = new Label("9:59");
        timer.setFont(loadFont(24));
        topBar.setRight(timer);

        gamePane.setTop(topBar);

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

        Image image = new Image(this.getClass().getResourceAsStream("/resources/board.png"));
        ImageView boardImageView = new ImageView(image);
        boardImageView.setFitWidth(Constants.BOARD_WIDTH);
        boardImageView.setFitHeight(Constants.BOARD_HEIGHT);

        Label leftCount = new Label("32");
        leftCount.setFont(loadFont(24));
        leftCount.getStyleClass().add("label-gunn");
        Label rightCount = new Label("32");
        rightCount.setFont(loadFont(24));
        rightCount.getStyleClass().add("label-gunn");

        HBox boardRow = new HBox(20.0);
        boardRow.setAlignment(Pos.CENTER);
        boardRow.getChildren().addAll(leftCount, boardImageView, rightCount);

        BorderPane.setAlignment(boardRow, Pos.CENTER);
        gamePane.setCenter(boardRow);

        Pane rightSpacer = new Pane();
        rightSpacer.setPrefWidth(94);
        gamePane.setRight(rightSpacer);

        HBox bottomPane = new HBox();
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.setPadding(new Insets(18.0));
        Button start = new Button("START");
        this.styleButton(start);
        bottomPane.getChildren().add(start);
        gamePane.setBottom(bottomPane);

        this.paneOrganizer.showScreen(gamePane);
    }

    private ImageView loadImageView(String path) {
        Image icon = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream(path)));
        ImageView iconImageView = new ImageView(icon);
        iconImageView.setFitWidth(30);
        iconImageView.setFitHeight(30);
        return iconImageView;
    }

    private Font loadFont(int size) {
        Font customFont = Font.loadFont(this.getClass().
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
