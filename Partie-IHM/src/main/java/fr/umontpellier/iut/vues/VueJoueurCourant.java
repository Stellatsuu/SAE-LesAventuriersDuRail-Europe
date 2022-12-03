package fr.umontpellier.iut.vues;

import fr.umontpellier.iut.ICouleurWagon;
import fr.umontpellier.iut.IDestination;
import fr.umontpellier.iut.IJeu;
import fr.umontpellier.iut.IJoueur;
import fr.umontpellier.iut.rails.CouleurWagon;
import fr.umontpellier.iut.rails.Destination;
import fr.umontpellier.iut.rails.Joueur;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Cette classe présente les éléments appartenant au joueur courant.
 *
 * On y définit les bindings sur le joueur courant, ainsi que le listener à exécuter lorsque ce joueur change
 */
public class VueJoueurCourant extends HBox {

    //Affichages labels
    @FXML
    private Label nomJoueur;
    @FXML
    private Label wagonsJoueur;
    @FXML
    private Label scoreJoueur;
    @FXML
    private Label garesJoueur;

    //Affichages images
    @FXML
    private ImageView imageJoueur;
    @FXML
    private ImageView imageWagons;
    @FXML
    private ImageView imageGares;
    @FXML
    private ImageView imageScore;

    //Conteneurs
    @FXML
    private HBox cartesJoueurCourant;
    @FXML
    private StackPane destinationsJoueurCourant;
    @FXML
    private HBox conteneurJoueur;

    //Listeners
    private ChangeListener<IJoueur> changementJoueurCourantListener;
    private ListChangeListener<CouleurWagon> changementCartesJoueursListener;

    //Joueur
    private IJoueur joueurCourant;
    private Map<CouleurWagon, Integer> nombreCartesJoueur;

    public VueJoueurCourant(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/joueurCourant.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void creerBindings(IJeu jeu){

        changementJoueurCourantListener = (observableValue, ancienneValeur, nouvelleValeur) -> Platform.runLater(() -> {
            //Changement des labels du joueur
            nomJoueur.setText(nouvelleValeur.getNom());
            wagonsJoueur.setText(String.valueOf(nouvelleValeur.getNbWagons()));
            scoreJoueur.setText(String.valueOf(nouvelleValeur.getScore()));
            garesJoueur.setText(String.valueOf(nouvelleValeur.getNbGares()));

            //Changement des images
            imageJoueur.setImage(new Image("images/images/avatar-"+ nouvelleValeur.getCouleur() + ".png"));
            imageGares.setImage(new Image("images/gares/gare-" + nouvelleValeur.getCouleur() + ".png"));
            imageWagons.setImage(new Image("images/wagons/image-wagon-" + nouvelleValeur.getCouleur() + ".png"));
            //imageScore.setImage(new Image());

            //Nettoyage des cartes et destinations du joueur précédent
            cartesJoueurCourant.getChildren().clear();
            destinationsJoueurCourant.getChildren().clear();

            nombreCartesJoueur = CouleurWagon.compteur(nouvelleValeur.getCartesWagon());

            //Mise en place des cartes et destinations du joueur actuel
            for(CouleurWagon carteJoueur : nouvelleValeur.getCartesWagon()){
                if(cartesJoueurCourant.getChildren().contains(carteVersVue(carteJoueur))){
                    carteVersVue(carteJoueur).setNombre(nombreCartesJoueur.get(carteJoueur));
                }
                else {
                    VueCarteWagonJoueur carte = new VueCarteWagonJoueur(carteJoueur, nombreCartesJoueur.get(carteJoueur));
                    cartesJoueurCourant.getChildren().add(carte);
                    carte.creerBindings();
                }
            }

            for(IDestination destinationJoueur : nouvelleValeur.getDestinations()) {
                VueDestination destination = new VueDestination(destinationJoueur);
                destinationsJoueurCourant.getChildren().add(destination);
                destination.creerSPBindings();
            }
        });

        changementCartesJoueursListener = elementChange -> Platform.runLater(() -> {
          while(elementChange.next()){
              nombreCartesJoueur = CouleurWagon.compteur(jeu.joueurCourantProperty().getValue().getCartesWagon()); //Compte le nombre de cartes de chaque couleur du joueur

              if (elementChange.wasAdded()) {
                  for(CouleurWagon carte : elementChange.getAddedSubList()) {
                      if(cartesJoueurCourant.getChildren().contains(carteVersVue(carte))){ //Si l'affichage contient déjà une carte de cette couleur
                          carteVersVue(carte).setNombre(nombreCartesJoueur.get(carte)); //On met à jour son nombre
                      }
                      else{
                          VueCarteWagonJoueur nouvelleCarte = new VueCarteWagonJoueur(carte, nombreCartesJoueur.get(carte)); //On ajoute l'affichage de la carte
                          cartesJoueurCourant.getChildren().add(nouvelleCarte);
                          nouvelleCarte.creerBindings();
                      }
                  }
              }
              else if(elementChange.wasRemoved()){
                  for(CouleurWagon carte : elementChange.getRemoved()) {
                      if(cartesJoueurCourant.getChildren().contains(carteVersVue(carte))){
                          carteVersVue(carte).setNombre(nombreCartesJoueur.get(carte));
                      }
                      else{
                          cartesJoueurCourant.getChildren().remove(carteVersVue(carte));
                      }
                  }
              }
          }
        });

        for(Joueur joueur : jeu.getJoueurs()){
            joueur.cartesWagonProperty().addListener(changementCartesJoueursListener);
        }

        jeu.joueurCourantProperty().addListener(changementJoueurCourantListener);

        bindTailles();
    }

    public VueCarteWagonJoueur carteVersVue(ICouleurWagon carte) {
        for (Node vueCarteWagonJoueur : cartesJoueurCourant.getChildren()) {
            if (((VueCarteWagonJoueur) vueCarteWagonJoueur).getCouleurWagon().equals(carte)) {
                return (VueCarteWagonJoueur) vueCarteWagonJoueur;
            }
        }
        return null;
    }

    @FXML
    public void switchDestination(){
        if(destinationsJoueurCourant.getChildren().size()>1){
            destinationsJoueurCourant.getChildren().get(destinationsJoueurCourant.getChildren().size()-1).toBack();
        }
    }

    public void bindTailles(){
        prefWidthProperty().bind(((VBox) getParent()).prefWidthProperty());
        prefHeightProperty().bind(((VBox) getParent()).prefHeightProperty());

        conteneurJoueur.prefWidthProperty().bind(prefWidthProperty().multiply(0.20));
        conteneurJoueur.prefHeightProperty().bind(prefHeightProperty().multiply(0.9));
        destinationsJoueurCourant.prefWidthProperty().bind(prefWidthProperty().multiply(0.18));
        cartesJoueurCourant.prefWidthProperty().bind(prefWidthProperty().multiply(0.62));
        cartesJoueurCourant.prefHeightProperty().bind(prefHeightProperty().multiply(0.8));
    }
}
