package fr.umontpellier.iut.vues;

import fr.umontpellier.iut.IJeu;
import fr.umontpellier.iut.IJoueur;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Cette classe présente les éléments des joueurs autres que le joueur courant,
 * en cachant ceux que le joueur courant n'a pas à connaitre.
 *
 * On y définit les bindings sur le joueur courant, ainsi que le listener à exécuter lorsque ce joueur change
 */
public class VueAutresJoueurs extends VBox {

    private ChangeListener<IJoueur> changementJoueurCourant;
    private Node joueurCourant;

    public VueAutresJoueurs(IJeu jeu) {
        for (int i = 0; i < jeu.getJoueurs().size(); i++) {
            getChildren().add(new VueJoueur(jeu.getJoueurs().get(i)));
        }
        setSpacing(2);
    }

    public void creerBindings(IJeu jeu) {
        for (Node child : getChildren()) {
            ((VueJoueur) child).creerBindings(jeu);
        }
        prefWidthProperty().bind(((Pane) getParent()).prefWidthProperty());

        changementJoueurCourant = (observableValue, ancienneValeur, nouvelleValeur) -> Platform.runLater(() -> {
            joueurCourant = getChildren().get(jeu.getJoueurs().indexOf(jeu.joueurCourantProperty().get()));
            if(ancienneValeur != null){
                getChildren().get(jeu.getJoueurs().indexOf(ancienneValeur)).setVisible(true);
            }
            joueurCourant.setVisible(false);
        });

        jeu.joueurCourantProperty().addListener(changementJoueurCourant);
    }
}