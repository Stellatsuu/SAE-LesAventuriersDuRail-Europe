package fr.umontpellier.iut.vues;

import fr.umontpellier.iut.IJeu;
import fr.umontpellier.iut.rails.Joueur;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class VueJoueur extends HBox {

    private Joueur joueur;

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

    private ChangeListener<Number> changementInfos;

    public VueJoueur(Joueur joueur){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/joueur.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.joueur = joueur;
    }

    public void creerBindings(IJeu jeu){
        prefWidthProperty().bind(((VBox) getParent()).prefWidthProperty());

        changementInfos = (observableValue, ancienneValeur, nouvelleValeur) -> Platform.runLater(() -> {
            //Changement des labels du joueur
            wagonsJoueur.setText(String.valueOf(joueur.getNbWagons()));
            scoreJoueur.setText(String.valueOf(joueur.getScore()));
            garesJoueur.setText(String.valueOf(joueur.getNbGares()));
        });

        joueur.scoreProperty().addListener(changementInfos);
        joueur.nbWagonsProperty().addListener(changementInfos);

        //Changement des labels du joueur
        nomJoueur.setText(joueur.getNom());
        wagonsJoueur.setText(String.valueOf(joueur.getNbWagons()));
        scoreJoueur.setText(String.valueOf(joueur.getScore()));
        garesJoueur.setText(String.valueOf(joueur.getNbGares()));
        //Changement des images
        imageJoueur.setImage(new Image("images/images/avatar-"+ joueur.getCouleur() + ".png"));
        imageGares.setImage(new Image("images/gares/gare-" + joueur.getCouleur() + ".png"));
        imageWagons.setImage(new Image("images/wagons/image-wagon-" + joueur.getCouleur() + ".png"));
    }

}
