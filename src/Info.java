import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class Info {
    public Info() {

    }

    public void showScreen(BorderPane gamePane) {
        HBox infoPane = new HBox();
        Image image = new Image(this.getClass().getResourceAsStream("/resources/info-page.png"));
        ImageView infoImageView = new ImageView(image);
        infoPane.setAlignment(Pos.CENTER);
        infoPane.getChildren().add(infoImageView);
        gamePane.setCenter(infoPane);
    }
}
