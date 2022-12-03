package fr.umontpellier.iut.vues;

import fr.umontpellier.iut.ICouleurWagon;
import fr.umontpellier.iut.IDestination;
import fr.umontpellier.iut.IJeu;
import fr.umontpellier.iut.rails.CouleurWagon;
import fr.umontpellier.iut.rails.Destination;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;

import java.io.IOException;
import java.util.List;

/**
 * Cette classe correspond à la fenêtre principale de l'application.
 * <p>
 * Elle est initialisée avec une référence sur la partie en cours (Jeu).
 * <p>
 * On y définit les bindings sur les éléments internes qui peuvent changer
 * (le joueur courant, les 5 cartes Wagons visibles, les destinations lors de l'étape d'initialisation de la partie, ...)
 * ainsi que les listeners à exécuter lorsque ces éléments changent
 */
public class VueDuJeu extends HBox {
    //Conteneurs du jeu
    @FXML
    private BorderPane borderPane;
    @FXML
    private VBox conteneurPioches;
    @FXML
    private VBox conteneurLeft;
    @FXML
    private VBox conteneurJoueurCourant;
    @FXML
    private Pane conteneurPlateau;
    @FXML
    private Pane conteneurAutresJoueurs;

    //Vues
    private IJeu jeu;
    @FXML
    private VuePlateau plateau;
    @FXML
    private VueJoueurCourant vueJoueurCourant;

    private VueAutresJoueurs vueAutresJoueurs;

    //Instructions
    private ChangeListener<String> instructionsListener;
    @FXML
    private Label instructions;

    //Pioches
    @FXML
    private ImageView piocheCarte;
    @FXML
    private ImageView piocheDestination;

    //Pioche visible
    private ListChangeListener<CouleurWagon> piocheVisibleListener;
    @FXML
    private VBox piocheVisible;

    //Choix des destinations
    private ListChangeListener<Destination> choixDestinationsListener;
    @FXML
    private VBox destinationsInitiales;

    //Bouton passer tour
    @FXML
    private Button boutonPasser;

    public VueDuJeu(IJeu jeu) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/jeu.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.jeu = jeu;

        boutonPasser = new Button("Passer");
        vueAutresJoueurs = new VueAutresJoueurs(this.getJeu());
    }

    public IJeu getJeu() {
        return jeu;
    }

    @FXML
    public void passer(){
        jeu.passerAEteChoisi();
    }

    @FXML
    public void piocherDestination(){ jeu.uneDestinationAEtePiochee();}

    @FXML
    public void piocherCarte(){ jeu.uneCarteWagonAEtePiochee();}

    public void creerBindings() {
        //Pioche des destinations
        choixDestinationsListener = elementChange -> Platform.runLater(() -> {
            while (elementChange.next()) {
                if (elementChange.wasAdded()) {
                    List<? extends IDestination> listeAjouts = elementChange.getAddedSubList();
                    for (IDestination destination : listeAjouts) {
                        VueDestination vueDestination = new VueDestination(destination);
                        destinationsInitiales.getChildren().add(vueDestination);
                        vueDestination.creerBindings();
                    }
                }
                if (elementChange.wasRemoved()) {
                    List<? extends IDestination> listeSuppressions = elementChange.getRemoved();
                    for(IDestination destination : listeSuppressions) {
                        destinationsInitiales.getChildren().remove(destinationVersVue(destination));
                    }
                }
            }
        });

        //Pioche des cartes visibles
        piocheVisibleListener = elementChange -> Platform.runLater(() -> {
            while (elementChange.next()) {
                if (elementChange.wasAdded()) {
                    List<? extends ICouleurWagon> listeAjouts = elementChange.getAddedSubList();
                    for (ICouleurWagon carte : listeAjouts) {
                        VueCarteWagon vueCarte = new VueCarteWagon(carte);
                        piocheVisible.getChildren().add(vueCarte);
                        vueCarte.creerBindings();
                        piocheDestination.setVisible(true);
                        piocheCarte.setVisible(true);
                    }
                }
                if (elementChange.wasRemoved()) {
                    List<? extends ICouleurWagon> listeSuppressions = elementChange.getRemoved();
                    if (listeSuppressions.size() == 5) {
                        piocheVisible.getChildren().clear();
                    } else {
                        for (ICouleurWagon carte : listeSuppressions) {
                            piocheVisible.getChildren().remove(carteVersVue(carte));
                        }
                    }
                }
            }
        });
        instructionsListener = (observableValue, ancienneValeur, nouvelleValeur) -> Platform.runLater(() -> {
            instructions.setText(nouvelleValeur);
        });

        jeu.instructionProperty().addListener(instructionsListener);
        jeu.destinationsInitialesProperty().addListener(choixDestinationsListener);
        jeu.cartesWagonVisiblesProperty().addListener(piocheVisibleListener);

        //Création des bindings
        conteneurAutresJoueurs.getChildren().add(vueAutresJoueurs);
        plateau.creerBindings();
        vueJoueurCourant.creerBindings(this.getJeu());
        vueAutresJoueurs.creerBindings(this.getJeu());
        bindTailles();
    }

    //Recherche d'une destination en sa vue correspondante
    public VueDestination destinationVersVue(IDestination destination) {
        for (Node vueDestination : destinationsInitiales.getChildren()) {
            if (((VueDestination) vueDestination).getDestination().equals(destination)) {
                return (VueDestination) vueDestination;
            }
        }
        return null;
    }

    public VueCarteWagon carteVersVue(ICouleurWagon carte) {
        for (Node vueCarteWagon : piocheVisible.getChildren()) {
            if (((VueCarteWagon) vueCarteWagon).getCouleurWagon().equals(carte)) {
                return (VueCarteWagon) vueCarteWagon;
            }
        }
        return null;
    }

    public void bindTailles() {

        setPrefWidth(Screen.getPrimary().getBounds().getWidth()*0.85);
        setPrefHeight(Screen.getPrimary().getBounds().getHeight()*0.85);

        //Partie gauche et partie droite
        borderPane.prefWidthProperty().bind(prefWidthProperty().multiply(0.85));
        borderPane.prefHeightProperty().bind(prefHeightProperty());
        conteneurPioches.prefWidthProperty().bind(prefWidthProperty().multiply(0.15));
        conteneurPioches.prefHeightProperty().bind(prefHeightProperty());

        //Contenu partie droite
        piocheCarte.setPreserveRatio(true);
        piocheCarte.fitWidthProperty().bind(conteneurPioches.prefWidthProperty().divide(1.75));
        piocheDestination.fitWidthProperty().bind(conteneurPioches.prefWidthProperty().divide(1.75));
        piocheDestination.setPreserveRatio(true);
        piocheVisible.prefWidthProperty().bind(conteneurPioches.prefWidthProperty());
        piocheVisible.prefHeightProperty().bind(conteneurPioches.prefHeightProperty().multiply(0.55));

        //Contenu partie gauche
        //Center
        conteneurPlateau.prefWidthProperty().bind(borderPane.prefWidthProperty().multiply(0.785));
        conteneurPlateau.prefHeightProperty().bind(borderPane.prefHeightProperty().multiply(0.8));

        //Left
        conteneurLeft.prefWidthProperty().bind(borderPane.prefWidthProperty().multiply(0.215));
        conteneurLeft.prefHeightProperty().bind(borderPane.prefHeightProperty().multiply(0.8));
        //Contenu left
        conteneurAutresJoueurs.prefHeightProperty().bind(conteneurLeft.prefHeightProperty().multiply(0.7));
        conteneurAutresJoueurs.prefWidthProperty().bind(conteneurLeft.prefWidthProperty());
        destinationsInitiales.prefWidthProperty().bind(conteneurPioches.prefWidthProperty());

        //Bottom
        conteneurJoueurCourant.prefWidthProperty().bind(borderPane.prefWidthProperty());
        conteneurJoueurCourant.prefHeightProperty().bind(borderPane.prefHeightProperty().multiply(0.2));

    }
}