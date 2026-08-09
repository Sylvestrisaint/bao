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
    private VBox boardRow;
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

    /**
     * Initializes the board and establishes each player's path.
     * @param gamePane The root layout pane where the board will be rendered.
     */
    public Board(Game gamePlay, BorderPane gamePane) {
        this.gamePlay = gamePlay;
        this.playerOneBeads = 32;
        this.playerTwoBeads = 32;
        this.setupBoardWithLabels(gamePane);
        this.playerOnePath = this.buildPath(0, 1);
        this.playerTwoPath = this.buildPath(2, 3);
    }

    /**
     * Setups the board with two beads in each pit and labels for players on each side.
     * @param gamePane The root layout pane where the board will be rendered.
     */
    private void setupBoardWithLabels(BorderPane gamePane) {
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

        this.boardRow = new VBox(20.0);
        this.boardRow.setAlignment(Pos.CENTER);
        this.boardRow.getChildren().addAll(labelOneBox, boardStack, labelTwoBox);

        BorderPane.setAlignment(this.boardRow, Pos.CENTER);
        gamePane.setCenter(this.boardRow);
    }

    /**
     * Retrieves the board in its current state
     * @return bao board with its labels
     */
    public VBox getBoard() {
        return this.boardRow;
    }

    /**
     * Retrieve the number of beads on player 1's side
     */
    public int getPlayerOneBeadCount() {
        return this.playerOneBeads;
    }

    /**
     * Retrieve the number of beads on player 2's side
     */
    public int getPlayerTwoBeadCount() {
        return this.playerTwoBeads;
    }

    /**
     * Animates capture and sowing within the current player's valid path. Capture only if the
     * current pit is an inner pit (row 1 or 2) containing at least two beads and was reached
     * by sowing.
     * @param startRow - starting row
     * @param startCol - starting column
     * @param stepDelay - transition delay in sowing animation from one pit to the next
     * @param onComplete - what's to happen when sowing is done?
     */
    public void sowFrom(int startRow, int startCol, Duration stepDelay, Runnable onComplete) {
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
                oppPitTwo = this.pits[startRow + 2][startCol];
            } else {
                oppPitOne = this.pits[startRow - 1][startCol];
                oppPitTwo = this.pits[startRow - 2][startCol];
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
                    sowFrom(lastPitHolder[0].getRow(), lastPitHolder[0].getCol(), Duration.millis(250), ()-> {
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

    /**
     * Event listener for each pit on the board. Sowing is allowed only when the game is active,
     * there is no sowing in progress, and the clicked pit is on the current player's side.
     * @param pit - clicked pit
     */
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

        this.sowFrom(pit.getRow(), pit.getCol(), Duration.millis(250), () -> {
            this.isSowInProgress = false;
            this.switchTurn();
        });
    }

    /**
     * Flashes all pits with at least one bead on the current player's side.
     */
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

    /**
     * Checks whether a player's move is valid. A move is valid if the clicked pit has at least one
     * bead and is on the player's side.
     * @param pit - clicked pit
     * @return whether the move is valid or not
     */
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

    /**
     * Switches turns between players with a slight delay to make the last move more noticeable.
     */
    private void switchTurn() {
        this.currentPlayer = (this.currentPlayer == 1) ? 2 : 1;
        gamePlay.switchPlayer();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        Runnable task = this::flashPits;
        scheduler.schedule(task, 1, TimeUnit.SECONDS);
        scheduler.shutdown();
    }

    /**
     * Updates bead counts for each player on the board whenever a capture occurs
     * @param isPlayerOne - was the capture by player one or two?
     * @param count - number of beads captured
     */
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

    /**
     * Establishes an anticlockwise path for a player, starting from the rightmost pit
     * of their top row, wrapping into their bottom row from left to right.
     * @param topRow - player's top row
     * @param bottomRow - player's bottom row
     * @return the ordered list of {row, col} coordinates that make up the player's path
     */
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

    /**
     * Determines which player's path a given row belongs to.
     * @param row - the row to check
     * @return player one's path if the row is 0 or 1, otherwise player two's path
     */
    private List<int[]> getPathForRow(int row) {
        return (row < 2) ? this.playerOnePath : this.playerTwoPath;
    }

    /**
     * Finds the position of a pit within a given path.
     * @param path - the path to search
     * @param row - row of the pit being searched for
     * @param col - column of the pit being searched for
     * @return the index of the pit in the path
     * @throws IllegalStateException if the pit isn't found in the path
     */
    private int indexOf(List<int[]> path, int row, int col) {
        for (int i = 0; i < path.size(); i++) {
            if (path.get(i)[0] == row && path.get(i)[1] == col) {
                return i;
            }
        }

        throw new IllegalStateException("Pit (" + row + ", " + col + ") not found in path");
    }
}
