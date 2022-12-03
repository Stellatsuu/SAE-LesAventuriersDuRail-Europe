package fr.umontpellier.iut.vues;

import fr.umontpellier.iut.IDestination;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Cette classe représente la vue d'une carte Destination.
 *
 * On y définit le listener à exécuter lorsque cette carte a été choisie par l'utilisateur
 */
public class VueDestination extends ImageView {

    private IDestination destination;

    public VueDestination(IDestination destination) {
        this.destination = destination;
        this.setOnMouseClicked(event -> {
            ((VueDuJeu) getScene().getRoot()).getJeu().uneDestinationAEteChoisie(destination.getNom());
        });
        setImage(new Image("images/missions/eu-" + (destination.getNom().replaceAll("[[^\\-]&&\\P{L}]", "")).toLowerCase() +".png"));
    }

    public IDestination getDestination() {
        return destination;
    }

    public void creerBindings(){
        setPreserveRatio(true);
        fitWidthProperty().bind(((VBox) getParent()).prefWidthProperty().divide(1.75));
    }

    public void creerSPBindings(){
        setPreserveRatio(true);
        fitWidthProperty().bind(((StackPane) getParent()).prefWidthProperty().divide(1.25));
    }
}
