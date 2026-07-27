import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class Board {
    private Game gamePlay;
    private GridPane pitGrid;
    private Pit[][] pits = new Pit[4][8];
    private boolean isSowInProgress = false;
    private int currentPlayer = 1;

    public Board(Game gamePlay, BorderPane gamePane) {
        this.gamePlay = gamePlay;
        this.setupBoardWithPits(gamePane);
    }

    public void sowFromAnimated(int startRow, int startCol, Duration stepDelay, Runnable onComplete) {
        int scoredBeads = 0;

        if (startRow == 1 || startRow == 2) {
            Pit oppPitOne;
            Pit oppPitTwo;

            if (this.currentPlayer == 1) {
                oppPitOne = this.pits[startRow + 1][startCol];
                oppPitTwo = this.pits[startRow + 1][startCol];
            } else {
                oppPitOne = this.pits[startRow - 1][startCol];
                oppPitTwo = this.pits[startRow - 1][startCol];
            }

            if (oppPitOne.getBeadCount() != 0) {
                scoredBeads = oppPitOne.getBeadCount();
                oppPitOne.setBeadCount(0);
            } else if (oppPitTwo.getBeadCount() != 0) {
                scoredBeads = oppPitTwo.getBeadCount();
                oppPitTwo.setBeadCount(0);
            }
        }

        Pit start = this.pits[startRow][startCol];
        start.setBeadCount(start.getBeadCount() + scoredBeads);

        int beadsToSow = start.getBeadCount();
        start.flashRemoved(0);

        SequentialTransition sequence = new SequentialTransition();
        int row = startRow, col = startCol;
        Pit[] lastPitHolder = new Pit[1];

        for (int i = 0; i < beadsToSow; i++) {
            col++;
            if (col >= 8) {
                col = 0;
                row = (row + 1) % 4;
            }

            final Pit next = this.pits[row][col];
            PauseTransition step = new PauseTransition(stepDelay);
            step.setOnFinished(e -> {
                next.flashAdded(next.getBeadCount() + 1);
                lastPitHolder[0] = next;
            })
            ;
            sequence.getChildren().add(step);
        }

        sequence.setOnFinished(e -> {
            if (onComplete != null) {
//                onComplete.run();
                if (lastPitHolder[0].getBeadCount() != 1) {
                    sowFromAnimated(lastPitHolder[0].getRow(), lastPitHolder[0].getCol(), Duration.millis(150), ()-> {
                        this.isSowInProgress = false;
                        this.switchTurn();
                    });
                } else {
                    onComplete.run();
                }

            }
        });
        sequence.play();
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
                pit.setOnPitClicked(this::handlePitClicked);

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

    private void handlePitClicked(Pit pit) {
        if (!gamePlay.gameIsActive() || this.isSowInProgress) {
            return;
        }

        if (!this.isValidMove(pit)) {
            pit.flashInvalid();
            return;
        }

        this.isSowInProgress = true;

        this.sowFromAnimated(pit.getRow(), pit.getCol(), Duration.millis(150), () -> {
            this.isSowInProgress = false;
            this.switchTurn();
//            this.checkCaptureRules(pit.getRow(), pit.getCol());
        });
    }

//    private void checkCaptureRules(int lastRow, int lastCol) {
//        Pit pit = new Pit(lastRow, lastCol);
//        if (beadCount == 1) {
//            this.switchTurn();
//        } else {
//            this.sowFromAnimated(lastRow, lastCol, Duration.millis(150), () -> {
//                this.isSowInProgress = false;
//                this.checkCaptureRules();
//            });
//        }
//    }

    private boolean isValidMove(Pit pit) {
        if (pit.getBeadCount() == 0) {
            return false;
        }

        boolean isPlayerOnesRow = pit.getRow() < 2;
        boolean isPlayerTwosRow = pit.getRow() >= 2;

        if (this.currentPlayer == 1 && !isPlayerOnesRow) return false;
        if (this.currentPlayer == 2 && !isPlayerTwosRow) return false;

        return true;
    }

    private void switchTurn() {
        this.currentPlayer = (this.currentPlayer == 1) ? 2 : 1;
        gamePlay.switchPlayer("PLAYER " + this.currentPlayer + " 'S TURN");
    }
}
