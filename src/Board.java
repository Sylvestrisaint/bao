import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class Board {
    private GridPane pitGrid;
    private Pit[][] pits = new Pit[4][8];

    public Board(BorderPane gamePane) {
        this.setupBoardWithPits(gamePane);
    }

    public void sowFrom(int startRow, int startCol) {
        Pit start = this.pits[startRow][startCol];
        int beadsToSow = start.getBeadCount();
        start.flashRemoved(0);

        int row = startRow, col = startRow;
        for (int i = 0; i < beadsToSow; i++) {
            col++;
            if (col >= 8) {
                col = 0;
                row = (row + 1) % 4;
            }
            Pit next = this.pits[row][col];
            next.flashAdded(next.getBeadCount() + 1);
        }
    }

    private void setupBoardWithPits(BorderPane gamePane) {
        Image image = new Image(this.getClass().getResourceAsStream("/resources/board.png"));
        ImageView boardImageView = new ImageView(image);
        boardImageView.setFitWidth(Constants.BOARD_WIDTH);
        boardImageView.setFitHeight(Constants.BOARD_HEIGHT);
        boardImageView.setPreserveRatio(true);

        this.pitGrid = new GridPane();
        this.pitGrid.setPrefSize(Constants.BOARD_WIDTH, Constants.BOARD_HEIGHT);
        this.pitGrid.setMaxSize(Constants.BOARD_WIDTH, Constants.BOARD_HEIGHT);

        for(int row = 0; row < 4; row++) {
            for (int col = 0; col < 8; col++) {
                Pit pit = new Pit(row, col, 2);
                pit.getView().setPrefSize(Constants.BOARD_WIDTH / 8.0, Constants.BOARD_HEIGHT / 4.0);
                this.pitGrid.add(pit.getView(), col, row);

                this.pits[row][col] = pit;
            }
        }

        StackPane boardStack = new StackPane();
        boardStack.getChildren().addAll(boardImageView, this.pitGrid);

        Font customFont = Font.loadFont(this.getClass().
                getResourceAsStream("fonts/noot-regular.ttf"), 24);
        if (customFont == null) { customFont = Font.getDefault();}

        Label leftCount = new Label("32");
        leftCount.setFont(customFont);
        leftCount.getStyleClass().add("label-gunn");
        Label rightCount = new Label("32");
        rightCount.setFont(customFont);
        rightCount.getStyleClass().add("label-gunn");

        HBox boardRow = new HBox(20.0);
        boardRow.setAlignment(Pos.CENTER);
        boardRow.getChildren().addAll(leftCount, boardStack, rightCount);

        BorderPane.setAlignment(boardRow, Pos.CENTER);
        gamePane.setCenter(boardRow);
    }

    public class Pit {
        private static final int MAX_BEADS = 11;
        private static final Duration FLASH_DURATION = Duration.millis(400);

        private final int row, col;
        private int beadCount;
        private final StackPane view;
        private final ImageView beadImageView;
        private final Label overflowLabel;
        private final Tooltip tooltip;
        private final Circle highlightOverlay;

        public Pit(int row, int col, int beadCount) {
            this.row = row;
            this.col = col;
            this.view = new StackPane();

            this.beadImageView = new ImageView();
            this.beadImageView.setFitWidth(40);
            this.beadImageView.setFitHeight(40);
            this.beadImageView.setPreserveRatio(true);

            this.overflowLabel = new Label();
            this.overflowLabel.setFont(Game.loadFont(14));
            this.overflowLabel.setTextFill(Color.BLACK);
            this.overflowLabel.setVisible(false);

            this.highlightOverlay = new Circle(28);
            this.highlightOverlay.setOpacity(0);
            this.highlightOverlay.setMouseTransparent(true);

            this.view.getChildren().addAll(this.beadImageView, this.overflowLabel, this.highlightOverlay);

            this.tooltip = new Tooltip();
            Tooltip.install(this.view, this.tooltip);

            this.setBeadCount(beadCount);
        }

        public StackPane getView() { return view; }
        public int getBeadCount() { return this.beadCount; }

        public void setBeadCount(int count) {
            this.beadCount = count;
            this.refreshVisual();
        }

        public void flashRemoved(int newCount) {
            this.setBeadCount(newCount);
            this.flash(Color.rgb(220, 40, 40, 0.45));
        }

        public void flashAdded(int newCount) {
            this.setBeadCount(newCount);
            this.flash(Color.rgb(40, 180, 80, 0.45));
        }

        private void flash(Color color) {
            this.highlightOverlay.setFill(color);
            this.highlightOverlay.setOpacity(1.0);

            FadeTransition fade = new FadeTransition(FLASH_DURATION, this.highlightOverlay);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.play();
        }

        private void refreshVisual() {
            int index = Math.min(this.beadCount, MAX_BEADS);
            this.beadImageView.setImage(BEADS[index]);

            if (this.beadCount > MAX_BEADS) {
                this.overflowLabel.setText(String.valueOf(this.beadCount));
                this.overflowLabel.setVisible(true);
            } else {
                this.overflowLabel.setVisible(false);
            }

            this.tooltip.setText(this.beadCount + (this.beadCount == 1 ? " bead" : " beads"));
        }

        private static final Image[] BEADS = new Image[MAX_BEADS + 1];
        static {
            for (int i = 1; i <= MAX_BEADS ; i++) {
                BEADS[i] = new Image(Pit.class.getResourceAsStream("/resources/beads_" + i + ".png"));
            }
        }
    }

}
