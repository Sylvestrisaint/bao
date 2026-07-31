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

    public StackPane getView() { return view; }
    public int getRow() { return this.row; }
    public int getCol() { return this.col; }
    public int getBeadCount() { return this.beadCount; }

    public void setBeadCount(int count) {
        this.beadCount = count;
        this.refreshVisual();
    }

    private Consumer<Pit> clickHandler;

    public void setOnPitClicked(Consumer<Pit> handler) {
        this.clickHandler = handler;
    }

    public void flashRemoved(int newCount) {
        this.setBeadCount(newCount);
        this.flash(Color.rgb(220, 40, 40, 0.45));
    }

    public void flashAdded(int newCount) {
        this.setBeadCount(newCount);
        this.flash(Color.rgb(40, 180, 80, 0.45));
    }

    public void flashInvalid() {
        this.flash(Color.GRAY);
    }

    public void flashValid() {
        this.flash(Color.LIGHTYELLOW);
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
