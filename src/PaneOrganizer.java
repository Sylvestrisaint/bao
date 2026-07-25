import javafx.scene.layout.BorderPane;

public class PaneOrganizer {
    private BorderPane root;

    /**
     * Sets up the main window layout and starts a new game inside it.
     */
    public PaneOrganizer() {
        this.root = new BorderPane();
        new Game(this);
    }

    /**
     * Grabs the main root layout pane.
     * @return The primary BorderPane that holds all the application scenes.
     */
    public BorderPane getRoot() { return this.root; }

    /**
     * Swaps out the current screen display by placing a new pane right in the center.
     * @param pane The new layout window pane to display on the screen.
     */
    public void showScreen(BorderPane pane) { this.root.setCenter(pane);}
}
