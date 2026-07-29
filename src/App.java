import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        PaneOrganizer organizer = new PaneOrganizer();
        Scene scene = new Scene(organizer.getRoot(), Constants.PANE_WIDTH, Constants.PANE_HEIGHT);
        scene.getStylesheets().add(this.getClass().getResource("css/style.css").
                toExternalForm());
        stage.setMinWidth(Constants.PANE_WIDTH);
        stage.setMinHeight(Constants.PANE_HEIGHT);
        stage.setScene(scene);
        stage.setTitle("BAO");
//        Image appIcon = new Image(getClass().getResourceAsStream("resources/icon.png"));
//        stage.getIcons().add(appIcon);
        stage.show();
    }

    public static void main(String[] argv) {
        launch(argv);
    }
}
