package fr.umontpellier.iut.rails;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.List;

class JeuEtudiantTest {
    Jeu Partie = new Jeu(new String[]{"Lolo", "Stella"});
    List<CouleurWagon> pileCartesWagon = Partie.getPileCartesWagon();
    List<CouleurWagon> defausseCartesWagon = Partie.getDefausseCartesWagon();
    List<CouleurWagon> cartesWagonVisibles = Partie.getCartesWagonVisibles();
    List<Destination> pileDestinations = Partie.getPileDestinations();
    //Nombre cartes après distrib aux joueurs : 102
    //Nombre cartes après distrib cartes visibles : 97

    /////defausserCarteWagon/////
     //Fonctionne
    @Test
    public void test_ajouter_carte_dans_defausse_vide() {
        Partie.defausserCarteWagon(CouleurWagon.ROUGE);
        System.out.println(defausseCartesWagon);
        assertEquals(1, defausseCartesWagon.size());
    }

     //Fonctionne
    @Test
    public void test_ne_pas_ajouter_carte_dans_cartes_visibles_car_egale_a_5_malgre_pile_et_defausse_vides() {
        pileCartesWagon.clear();
        defausseCartesWagon.clear();
        for (int i = 0; i < 5; i++) {
            cartesWagonVisibles.add(CouleurWagon.ROSE);
        }
        Partie.defausserCarteWagon(CouleurWagon.ROUGE);
        System.out.println(cartesWagonVisibles);
        System.out.println(defausseCartesWagon);
        assertEquals(1, defausseCartesWagon.size());
    }

    /////piocherCarteWagon/////
    //Fonctionne
    @Test
    public void test_pile_et_defausse_remplies() {
        Partie.piocherCarteWagon();
        System.out.println(pileCartesWagon);

        assertEquals(96, pileCartesWagon.size());
    }

    //Fonctionne
    @Test
    public void test_pile_vide_et_defausse_remplie() {
        defausseCartesWagon.addAll(pileCartesWagon);
        pileCartesWagon.clear();
        Partie.piocherCarteWagon();
        assertEquals(96, pileCartesWagon.size());
    }

    //Fonctionne
    @Test
    public void test_pile_et_defausse_vides() {
        pileCartesWagon.clear();
        defausseCartesWagon.clear();
        assertNull(Partie.piocherCarteWagon());
    }

    /////retirerCarteWagonVisible/////
    //Fonctionne
    @Test
    public void test_piocher_carte_wagon_visible() {
        Partie.retirerCarteWagonVisible(cartesWagonVisibles.get(0));
        System.out.println(cartesWagonVisibles);
        assertEquals(5, cartesWagonVisibles.size());
    }

    //Fonctionne
    @Test
    public void test_3_locomotives() {
        cartesWagonVisibles.clear();
        for (int i = 0; i < 3; i++) {
            cartesWagonVisibles.add(CouleurWagon.LOCOMOTIVE);
        }
        for (int i = 0; i < 2; i++) {
            cartesWagonVisibles.add(CouleurWagon.ROUGE);
        }
        System.out.println(cartesWagonVisibles);
        Partie.retirerCarteWagonVisible(CouleurWagon.ROUGE);
        System.out.println(cartesWagonVisibles);
    }

    /////piocherDestination/////
    //Fonctionne
    @Test
    public void test_pile_destination_remplie() {
        Partie.piocherDestination();
        assertEquals(39, pileDestinations.size());
    }

    //Fonctionne
    @Test
    public void test_pile_destination_vide() {
        pileDestinations.clear();
        assertNull(Partie.piocherDestination());
    }


}
