package fr.umontpellier.iut.rails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tunnel extends Route {

    private List<CouleurWagon> prixBonus;

    //////Initialisation d'un tunnel//////


    public Tunnel(Ville ville1, Ville ville2, int longueur, CouleurWagon couleur) {
        super(ville1, ville2, longueur, couleur);
        this.prixBonus = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "[" + super.toString() + "]";
    }

    public void peutPrendreRouteBonus(Joueur joueur, Jeu jeu){
        CouleurWagon carteBonus=null;
        for(int i =0; i<3;i++) {
            carteBonus=jeu.piocherCarteWagon();
            this.prixBonus.add(carteBonus);
            jeu.defausserCarteWagon(carteBonus);
        }
    }

    @Override
    public boolean peutPrendreRoute(Joueur joueur, Jeu jeu){
        List<CouleurWagon> listeCouleurWagons = CouleurWagon.getCouleursSimples();
        List<Route> listeRoutes = jeu.getRoutes();

        if(this.getProprietaire() == null) {
            //Cas normal -> Route grise -> Assez de carte de la même couleur (avec ou sans loco)
            int frequenceBonus =0;
            if (this.getCouleur().equals(CouleurWagon.GRIS)) {
                for (CouleurWagon couleurWagon : listeCouleurWagons) {
                    frequenceBonus = Collections.frequency(prixBonus,couleurWagon);
                    if(couleurWagon!=CouleurWagon.LOCOMOTIVE){
                        frequenceBonus += Collections.frequency(prixBonus,CouleurWagon.LOCOMOTIVE);
                    }
                    if (joueur.nombreCouleurWagonJoueur(CouleurWagon.LOCOMOTIVE) + joueur.nombreCouleurWagonJoueur(couleurWagon) >= this.getLongueur()+frequenceBonus) {
                        return true;
                    }
                }
            }
            //Cas couleur -> Route couleur -> Assez de carte de la même couleur (avec ou sans loco)
            else {
                frequenceBonus = Collections.frequency(prixBonus,this.getCouleur())+Collections.frequency(prixBonus,CouleurWagon.LOCOMOTIVE);
                if (joueur.nombreCouleurWagonJoueur(CouleurWagon.LOCOMOTIVE) + joueur.nombreCouleurWagonJoueur(this.getCouleur()) >= this.getLongueur()+frequenceBonus){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void prendreRoute(Joueur joueur, Jeu jeu){
        int prixRoute =0;
        CouleurWagon couleurChoisie = null;
        List<CouleurWagon> listeCouleurWagons = CouleurWagon.getCouleursSimples();
        ArrayList<String> choixCartesPossibles = new ArrayList<>();



        int frequenceBonus=0;
        if(this.getCouleur() == CouleurWagon.GRIS){

            for(CouleurWagon couleurWagon : listeCouleurWagons){
                frequenceBonus = Collections.frequency(prixBonus,couleurWagon);
                if(couleurWagon!=CouleurWagon.LOCOMOTIVE){
                    frequenceBonus += Collections.frequency(prixBonus,CouleurWagon.LOCOMOTIVE);
                }
                if(joueur.nombreCouleurWagonJoueur(CouleurWagon.LOCOMOTIVE) + joueur.nombreCouleurWagonJoueur(couleurWagon) >= this.getLongueur()+frequenceBonus){
                    choixCartesPossibles.add(couleurWagon.name());
                }
            }
        }
        else{
            frequenceBonus = Collections.frequency(prixBonus,this.getCouleur())+Collections.frequency(prixBonus,CouleurWagon.LOCOMOTIVE);
            if(joueur.nombreCouleurWagonJoueur(CouleurWagon.LOCOMOTIVE) + joueur.nombreCouleurWagonJoueur(this.getCouleur()) >= this.getLongueur()+frequenceBonus){
                choixCartesPossibles.add(this.getCouleur().name());
                couleurChoisie = this.getCouleur();
            }
        }

        choixCartesPossibles.add(CouleurWagon.LOCOMOTIVE.name());



        while(prixRoute < this.getLongueur()){
            String choixJoueur = joueur.choisir("Choisissez " + (this.getLongueur()-prixRoute) + " carte(s) à défausser", choixCartesPossibles, new ArrayList<>(), false);

            if(choixJoueur.equals(CouleurWagon.LOCOMOTIVE.name())){
                joueur.getCartesWagon().remove((CouleurWagon.LOCOMOTIVE));
                jeu.defausserCarteWagon(CouleurWagon.LOCOMOTIVE);
                prixRoute++;
            }

            if(couleurChoisie==null){
                for(CouleurWagon couleurWagon : listeCouleurWagons){
                    if(choixJoueur.equals(couleurWagon.name())){
                        couleurChoisie = couleurWagon;
                        joueur.getCartesWagon().remove(couleurChoisie);
                        jeu.defausserCarteWagon(couleurChoisie);
                        prixRoute++;
                    }
                }
            }

            else if(choixJoueur.equals(couleurChoisie.name())){
                joueur.getCartesWagon().remove(couleurChoisie);
                jeu.defausserCarteWagon(couleurChoisie);
                prixRoute++;
            }
        }

        if(couleurChoisie==null) {
            frequenceBonus = Collections.frequency(prixBonus, couleurChoisie) + Collections.frequency(prixBonus, CouleurWagon.LOCOMOTIVE);
        }
        else {
        frequenceBonus=Collections.frequency(prixBonus,CouleurWagon.LOCOMOTIVE);
        }

        while (prixRoute<this.getLongueur()+frequenceBonus){
            String choixJoueur = joueur.choisir("Choisissez " + (this.getLongueur()+frequenceBonus-prixRoute) + " carte(s) à défausser", choixCartesPossibles, new ArrayList<>(), false);

            if(choixJoueur.equals(CouleurWagon.LOCOMOTIVE.name())){
                joueur.getCartesWagon().remove((CouleurWagon.LOCOMOTIVE));
                jeu.defausserCarteWagon(CouleurWagon.LOCOMOTIVE);
                prixRoute++;
            }

            if(couleurChoisie==null){
                for(CouleurWagon couleurWagon : listeCouleurWagons){
                    if(choixJoueur.equals(couleurWagon.name())){
                        couleurChoisie = couleurWagon;
                        joueur.getCartesWagon().remove(couleurChoisie);
                        jeu.defausserCarteWagon(couleurChoisie);
                        prixRoute++;
                    }
                }
            }

            else if(choixJoueur.equals(couleurChoisie.name())){
                joueur.getCartesWagon().remove(couleurChoisie);
                jeu.defausserCarteWagon(couleurChoisie);
                prixRoute++;
            }
        }


        this.setProprietaire(joueur);
        joueur.setNbWagons(joueur.getNbWagons()-this.getLongueur());
    }
}
