package fr.umontpellier.iut.rails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Route {

    // Première extrémité
    private Ville ville1;

    // Deuxième extrémité
    private Ville ville2;

    // Nombre de segments
    private int longueur;

    // CouleurWagon pour capturer la route (éventuellement GRIS, mais pas LOCOMOTIVE)
    private CouleurWagon couleur;

    // Joueur qui a capturé la route (`null` si la route est encore à prendre)
    private Joueur proprietaire;

    // Nom unique de la route. Ce nom est nécessaire pour résoudre l'ambiguïté entre les routes doubles (voir la classe Plateau pour plus de clarté)
    private String nom;

    //////Initialisation d'une route//////
    public Route(Ville ville1, Ville ville2, int longueur, CouleurWagon couleur) {
        this.ville1 = ville1;
        this.ville2 = ville2;
        this.longueur = longueur;
        this.couleur = couleur;
        nom = ville1.getNom() + " - " + ville2.getNom();
        proprietaire = null;
    }

    //Getters & Setters
    public Ville getVille1() {
        return ville1;
    }

    public Ville getVille2() {
        return ville2;
    }

    public int getLongueur() {
        return longueur;
    }

    public CouleurWagon getCouleur() {
        return couleur;
    }

    public Joueur getProprietaire() {
        return proprietaire;
    }

    public void setProprietaire(Joueur proprietaire) {
        this.proprietaire = proprietaire;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String toLog() {
        return String.format("<span class=\"route\">%s - %s</span>", ville1.getNom(), ville2.getNom());
    }

    public void prendreRoute(Joueur joueur, Jeu jeu){
        int prixRoute = this.getLongueur();
        CouleurWagon couleurChoisie = null;
        List<CouleurWagon> listeCouleurWagons = CouleurWagon.getCouleursSimples();
        ArrayList<String> choixCartesPossibles = new ArrayList<>();
        if(this.getCouleur() == CouleurWagon.GRIS){
            for(CouleurWagon couleurWagon : listeCouleurWagons){
                if(joueur.nombreCouleurWagonJoueur(CouleurWagon.LOCOMOTIVE) + joueur.nombreCouleurWagonJoueur(couleurWagon) >= prixRoute){
                    choixCartesPossibles.add(couleurWagon.name());
                }
            }
        }
        else{
            if(joueur.nombreCouleurWagonJoueur(CouleurWagon.LOCOMOTIVE) + joueur.nombreCouleurWagonJoueur(this.getCouleur()) >= prixRoute){
                choixCartesPossibles.add(this.getCouleur().name());
                couleurChoisie = this.getCouleur();
            }
        }

        choixCartesPossibles.add(CouleurWagon.LOCOMOTIVE.name());

        while(prixRoute != 0){
            String choixJoueur = joueur.choisir("Choisissez " + prixRoute + " carte(s) à défausser", choixCartesPossibles, new ArrayList<>(), false);

            if(choixJoueur.equals(CouleurWagon.LOCOMOTIVE.name())){
                joueur.getCartesWagon().remove((CouleurWagon.LOCOMOTIVE));
                jeu.defausserCarteWagon(CouleurWagon.LOCOMOTIVE);
                prixRoute--;
            }

            if(couleurChoisie==null){
                for(CouleurWagon couleurWagon : listeCouleurWagons){
                    if(choixJoueur.equals(couleurWagon.name())){
                        couleurChoisie = couleurWagon;
                        joueur.getCartesWagon().remove(couleurChoisie);
                        jeu.defausserCarteWagon(couleurChoisie);
                        prixRoute--;
                    }
                }
            }

            else if(choixJoueur.equals(couleurChoisie.name())){
                joueur.getCartesWagon().remove(couleurChoisie);
                jeu.defausserCarteWagon(couleurChoisie);
                prixRoute--;
            }
        }
        this.setProprietaire(joueur);
        joueur.setNbWagons(joueur.getNbWagons()-this.getLongueur());
    }

    public boolean peutPrendreRoute(Joueur joueur, Jeu jeu){
        List<CouleurWagon> listeCouleurWagons = CouleurWagon.getCouleursSimples();
        
        if(this.getProprietaire() == null) {
        //Cas normal -> Route grise -> Assez de carte de la même couleur (avec ou sans loco)
            if (this.getCouleur().equals(CouleurWagon.GRIS)) {
                for (CouleurWagon couleurWagon : listeCouleurWagons) {
                    if (joueur.nombreCouleurWagonJoueur(CouleurWagon.LOCOMOTIVE) + joueur.nombreCouleurWagonJoueur(couleurWagon) >= this.getLongueur()) {
                        return true;
                    }
                }
            }
            //Cas couleur -> Route couleur -> Assez de carte de la même couleur (avec ou sans loco)
            else {
                if (joueur.nombreCouleurWagonJoueur(CouleurWagon.LOCOMOTIVE) + joueur.nombreCouleurWagonJoueur(this.getCouleur()) >= this.getLongueur());{
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("[%s - %s (%d, %s)]", ville1, ville2, longueur, couleur);
    }

    // @return un objet simple représentant les informations de la route
    public Object asPOJO() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("nom", getNom());
        if (proprietaire != null) {
            data.put("proprietaire", proprietaire.getCouleur());
        }
        return data;
    }
}
