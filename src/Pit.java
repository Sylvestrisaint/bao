import javafx.animation.FadeTransition;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import java.util.function.Consumer;

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

    /**
     * Represents a single pit on the board, responsible for tracking its own bead count
     * and rendering itself, including bead imagery, overflow count, and click/flash animations.
     */
    public Pit(int row, int col, int beadCount) {
        this.row = row;
        this.col = col;
        this.view = new StackPane();
        this.view.setOnMouseClicked(event -> {
            if (this.clickHandler != null) {
                this.clickHandler.accept(this);
            }
        });
        this.view.setCursor(Cursor.HAND);

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

    /**
     * Retrieves the visual node representing this pit.
     * @return the pit's view
     */
    public StackPane getView() { return view; }
    /**
     * Retrieves the row this pit occupies.
     * @return row index
     */
    public int getRow() { return this.row; }
    /**
     * Retrieves the column this pit occupies.
     * @return column index
     */
    public int getCol() { return this.col; }
    /**
     * Retrieves the current number of beads in this pit.
     * @return bead count
     */
    public int getBeadCount() { return this.beadCount; }

    /**
     * Updates the bead count for this pit and refreshes its visual to match.
     * @param count - new bead count
     */
    public void setBeadCount(int count) {
        this.beadCount = count;
        this.refreshVisual();
    }

    private Consumer<Pit> clickHandler;

    /**
     * Registers a handler to be notified whenever this pit is clicked.
     * @param handler - callback to run with this pit as its argument
     */
    public void setOnPitClicked(Consumer<Pit> handler) {
        this.clickHandler = handler;
    }

    /**
     * Sets the pit's bead count to reflect a capture and flashes it red to indicate beads were removed.
     * @param newCount - bead count after the capture
     */
    public void flashRemoved(int newCount) {
        this.setBeadCount(newCount);
        this.flash(Color.rgb(220, 40, 40, 0.45));
    }

    /**
     * Sets the pit's bead count to reflect a sow and flashes it green to indicate a bead was added.
     * @param newCount - bead count after the bead is added
     */

    public void flashAdded(int newCount) {
        this.setBeadCount(newCount);
        this.flash(Color.rgb(40, 180, 80, 0.45));
    }

    /**
     * Flashes the pit gray to indicate an invalid move was attempted on it.
     */
    public void flashInvalid() {
        this.flash(Color.GRAY);
    }

    /**
     * Flashes a yellow outline around the pit to indicate it's a valid move for the current player.
     */
    public void flashValid() {
        this.highlightOverlay.setFill(Color.TRANSPARENT);
        this.highlightOverlay.setStroke(Color.LIGHTYELLOW);
        this.highlightOverlay.setStrokeWidth(3.0);
        this.highlightOverlay.setOpacity(0.1);

        FadeTransition fade = new FadeTransition(FLASH_DURATION, this.highlightOverlay);
        fade.setFromValue(0.1);
        fade.setToValue(0.0);
        fade.play();
    }

    /**
     * Fades the highlight overlay in and out using the given color. Shared by the flash methods to
     * avoid duplicating the fade animation setup.
     * @param color - color to flash the overlay
     */
    private void flash(Color color) {
        this.highlightOverlay.setFill(color);
        this.highlightOverlay.setStroke(Color.TRANSPARENT);
        this.highlightOverlay.setOpacity(1.0);

        FadeTransition fade = new FadeTransition(FLASH_DURATION, this.highlightOverlay);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.play();
    }

    /**
     * Updates the pit's bead image, overflow label, and tooltip to match its
     * current bead count. Beads beyond MAX_BEADS are shown as a capped image
     * with the overflow count layered on top.
     */
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
