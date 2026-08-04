import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class Info {
    public Info() {

    }

    public void showScreen(BorderPane gamePane) {
        HBox infoPane = new HBox();
        Text label = new Text("INFO");
        infoPane.getChildren().add(label);
        gamePane.setCenter(infoPane);
    }
}
