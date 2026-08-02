import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Board {
    private Game gamePlay;
    private Pit[][] pits = new Pit[4][8];
    private final List<int[]> playerOnePath;
    private final List<int[]> playerTwoPath;
    private boolean isSowInProgress = false;
    private boolean isPitClicked = false;
    private Label playerOneCountLabel;
    private Label playerTwoCountLabel;
    private int playerOneBeads;
    private int playerTwoBeads;
    private int currentPlayer = 1;

    public Board(Game gamePlay, BorderPane gamePane) {
        this.gamePlay = gamePlay;
        this.playerOneBeads = 32;
        this.playerTwoBeads = 32;
        this.setupBoardWithPits(gamePane);
        this.playerOnePath = this.buildPath(0, 1);
        this.playerTwoPath = this.buildPath(2, 3);
    }

    private void setupBoardWithPits(BorderPane gamePane) {
        Image image = new Image(this.getClass().getResourceAsStream("/resources/board.png"));
        ImageView boardImageView = new ImageView(image);
        boardImageView.setFitWidth(Constants.BOARD_WIDTH);
        boardImageView.setFitHeight(Constants.BOARD_HEIGHT);
        boardImageView.setPreserveRatio(true);

        GridPane pitGrid = new GridPane();
        pitGrid.setPrefSize(Constants.BOARD_WIDTH, Constants.BOARD_HEIGHT);
        pitGrid.setMaxSize(Constants.BOARD_WIDTH, Constants.BOARD_HEIGHT);

        for(int row = 0; row < 4; row++) {
            for (int col = 0; col < 8; col++) {
                Pit pit = new Pit(row, col, 2);
                pit.getView().setPrefSize(Constants.BOARD_WIDTH / 8.0, Constants.BOARD_HEIGHT / 4.0);
                pit.setOnPitClicked(this::handlePitClicked);

                pitGrid.add(pit.getView(), col, row);

                this.pits[row][col] = pit;
            }
        }

        StackPane boardStack = new StackPane();
        boardStack.getChildren().addAll(boardImageView, pitGrid);

        Font customFont = Font.loadFont(this.getClass().
                getResourceAsStream("fonts/noot-regular.ttf"), 20);
        if (customFont == null) { customFont = Font.getDefault();}

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        HBox labelOneBox = new HBox();
        labelOneBox.setMaxSize(Constants.BOARD_WIDTH, 50.0);
        this.playerOneCountLabel = new Label(String.valueOf(this.playerOneBeads));
        this.playerOneCountLabel.setFont(customFont);
        this.playerOneCountLabel.getStyleClass().add("label-one");
        Label playerOne = new Label("PLAYER 1");
        playerOne.setTextFill(Color.web(Constants.PLAYER_ONE_COLOR));
        playerOne.setFont(customFont);
        labelOneBox.getChildren().addAll(this.playerOneCountLabel, spacer1, playerOne);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        HBox labelTwoBox = new HBox();
        labelTwoBox.setMaxSize(Constants.BOARD_WIDTH, 50.0);
        this.playerTwoCountLabel = new Label(String.valueOf(this.playerTwoBeads));
        this.playerTwoCountLabel.setFont(customFont);
        this.playerTwoCountLabel.getStyleClass().add("label-two");
        Label playerTwo = new Label("PLAYER 2");
        playerTwo.setTextFill(Color.web(Constants.PLAYER_TWO_COLOR));
        playerTwo.setFont(customFont);
        labelTwoBox.getChildren().addAll(playerTwo, spacer2, this.playerTwoCountLabel);

        VBox boardRow = new VBox(20.0);
        boardRow.setAlignment(Pos.CENTER);
        boardRow.getChildren().addAll(labelOneBox, boardStack, labelTwoBox);

        BorderPane.setAlignment(boardRow, Pos.CENTER);
        gamePane.setCenter(boardRow);
    }

    public void sowFromAnimated(int startRow, int startCol, Duration stepDelay, Runnable onComplete) {
        List<int[]> path = this.getPathForRow(startRow);
        int index = this.indexOf(path, startRow, startCol);

        int scoredBeads = 0;

        if (!isPitClicked && (startRow == 1 || startRow == 2)) {
            Pit oppPitOne;
            Pit oppPitTwo;
            boolean isPlayerOne = false;

            if (this.currentPlayer == 1) {
                isPlayerOne = true;
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
            this.updateBeadCounts(isPlayerOne, scoredBeads);
        }

        this.isPitClicked = false;

        Pit start = this.pits[startRow][startCol];
        start.setBeadCount(start.getBeadCount() + scoredBeads);

        int beadsToSow = start.getBeadCount();
        start.flashRemoved(0);

        SequentialTransition sequence = new SequentialTransition();
        Pit[] lastPitHolder = new Pit[1];

        for (int i = 0; i < beadsToSow; i++) {
            index = (index + 1) % path.size();
            int[] coord = path.get(index);

            final Pit next = this.pits[coord[0]][coord[1]];
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
                if (lastPitHolder[0].getBeadCount() != 1) {
                    sowFromAnimated(lastPitHolder[0].getRow(), lastPitHolder[0].getCol(), Duration.millis(250), ()-> {
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

    private void handlePitClicked(Pit pit) {
        this.isPitClicked = true;
        if (!gamePlay.gameIsActive() || this.isSowInProgress) {
            return;
        }

        if (!this.isValidMove(pit)) {
            pit.flashInvalid();
            return;
        }

        this.isSowInProgress = true;

        this.sowFromAnimated(pit.getRow(), pit.getCol(), Duration.millis(250), () -> {
            this.isSowInProgress = false;
            this.switchTurn();
        });
    }

    public void flashPits() {
        int rowBoundary;
        if (this.currentPlayer == 1) {
            rowBoundary = 2;
        } else {
            rowBoundary = 4;
        }
        for (int row = rowBoundary - 2; row < rowBoundary; row++) {
            for (int col = 0; col < 8; col++) {
                if (pits[row][col].getBeadCount() > 0) {
                    pits[row][col].flashValid();
                }
            }
        }
    }

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
        gamePlay.switchPlayer();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        Runnable task = this::flashPits;
        scheduler.schedule(task, 1, TimeUnit.SECONDS);
        scheduler.shutdown();
    }

    private void updateBeadCounts(boolean isPlayerOne, int count) {
        if (isPlayerOne) {
            this.playerOneBeads += count;
            this.playerTwoBeads -= count;
        } else {
            this.playerOneBeads -= count;
            this.playerTwoBeads += count;
        }

        this.playerOneCountLabel.setText(String.valueOf(this.playerOneBeads));
        this.playerTwoCountLabel.setText(String.valueOf(this.playerTwoBeads));
    }

    private List<int[]> buildPath(int topRow, int bottomRow) {
        List<int[]> path = new ArrayList<>();

        for (int col = 7; col >= 0; col--) {
            path.add(new int[]{topRow, col});
        }

        for (int col = 0; col < 8; col++) {
            path.add(new int[] {bottomRow, col});
        }

        return path;
    }

    private List<int[]> getPathForRow(int row) {
        return (row < 2) ? this.playerOnePath : this.playerTwoPath;
    }

    private int indexOf(List<int[]> path, int row, int col) {
        for (int i = 0; i < path.size(); i++) {
            if (path.get(i)[0] == row && path.get(i)[1] == col) {
                return i;
            }
        }

        throw new IllegalStateException("Pit (" + row + ", " + col + ") not found in path");
    }
}
