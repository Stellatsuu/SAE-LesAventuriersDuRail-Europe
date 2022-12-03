package fr.umontpellier.iut.vues;

import fr.umontpellier.iut.ICouleurWagon;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import java.io.IOException;


/**
 * Cette classe représente la vue d'une carte Wagon.
 *
 * On y définit le listener à exécuter lorsque cette carte a été choisie par l'utilisateur
 */
public class VueCarteWagonJoueur extends StackPane {

    private ICouleurWagon couleurWagon;
    private int nombre;

    @FXML
    private Label labelNombre;
    @FXML
    private ImageView imageCarte;

    public VueCarteWagonJoueur(ICouleurWagon couleurWagon, int nombre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/carteWagonJoueur.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.couleurWagon = couleurWagon;
        this.nombre = nombre;

        this.setOnMouseClicked(event -> {
            ((VueDuJeu) getScene().getRoot()).getJeu().uneCarteWagonAEteChoisie(couleurWagon);
        });

        imageCarte.setImage((new Image("images/cartesWagons/carte-wagon-" + couleurWagon.toString().toUpperCase() + ".png")));
        labelNombre.setText(String.valueOf(nombre));
    }

    public void setNombre(int nombre){
        Platform.runLater(() -> {
            this.nombre = nombre;
            labelNombre.setText(String.valueOf(nombre));
        });
    }

    public ICouleurWagon getCouleurWagon() {
        return couleurWagon;
    }

    public void creerBindings(){
        imageCarte.setPreserveRatio(true);
        imageCarte.setRotate(90);
        imageCarte.fitHeightProperty().bind(((HBox) getParent()).prefHeightProperty().divide(1.5));
    }
}
