import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class Pit {
    private final int row, col;
    private int beadCount;
    private final StackPane view;

    public Pit(int row, int col, int beadCount) {
        this.row = row;
        this.col = col;
        this.beadCount = beadCount;
        this.view = new StackPane();
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public int getBeadCount() { return beadCount; }
    public StackPane getView() { return view; }

    public void setBeadCount(int count) {
        this.beadCount = count;
        this.refreshVisual();
    }

    private void refreshVisual() {
        this.view.getChildren().clear();
        Label countText = new Label(String.valueOf(this.beadCount));
        this.view.getChildren().add(countText);
    }
}
