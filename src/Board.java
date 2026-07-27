import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

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
}
