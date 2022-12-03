package fr.umontpellier.iut.rails;

import com.google.gson.Gson;
import fr.umontpellier.iut.gui.GameServer;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class Jeu implements Runnable {
    // Liste des joueurs
    private List<Joueur> joueurs;

    // Le joueur dont c'est le tour
    private Joueur joueurCourant;

    // Liste des villes représentées sur le plateau de jeu
    private List<Ville> villes;

    // Liste des routes du plateau de jeu
    private List<Route> routes;

    // Pile de pioche (face cachée)
    private List<CouleurWagon> pileCartesWagon;

    // Cartes de la pioche face visible (normalement il y a 5 cartes face visible)
    private List<CouleurWagon> cartesWagonVisibles;

    // Pile de cartes qui ont été défaussée au cours de la partie
    private List<CouleurWagon> defausseCartesWagon;

    // Pile des cartes "Destination" (uniquement les destinations "courtes", les
    // destinations "longues" sont distribuées au début de la partie et ne peuvent
    // plus être piochées après)
    private List<Destination> pileDestinations;

    // File d'attente des instructions recues par le serveur
    private BlockingQueue<String> inputQueue;

    // Messages d'information du jeu
    private List<String> log;

    //////Initialisation du jeu//////
    public Jeu(String[] nomJoueurs) {

        // Initialisation des entrées/sorties
        inputQueue = new LinkedBlockingQueue<>();
        log = new ArrayList<>();

        // Création des cartes
        pileCartesWagon = new ArrayList<>();
        cartesWagonVisibles = new ArrayList<>();
        defausseCartesWagon = new ArrayList<>();
        pileDestinations = new ArrayList<>();

        // Création des joueurs
        ArrayList<Joueur.Couleur> couleurs = new ArrayList<>(Arrays.asList(Joueur.Couleur.values()));
        Collections.shuffle(couleurs);
        joueurs = new ArrayList<>();
        for (String nom : nomJoueurs) {
            Joueur joueur = new Joueur(nom, this, couleurs.remove(0));
            joueurs.add(joueur);
        }
        joueurCourant = joueurs.get(0);

        // Création des villes et des routes
        Plateau plateau = Plateau.makePlateauEurope();
        villes = plateau.getVilles();
        routes = plateau.getRoutes();

        //Initialisation des cartes
        //Cartes wagons : 110 cartes, 12*8 couleurs et 14 locomotives
        for (int i = 0; i < 12; i++) {
            pileCartesWagon.addAll(CouleurWagon.getCouleursSimples());
        }
        for (int i = 0; i < 14; i++) {
            pileCartesWagon.add(CouleurWagon.LOCOMOTIVE);
        }
        Collections.shuffle(pileCartesWagon);

        //Cartes destinations : 46 cartes, 40 normales, 6 longues
        pileDestinations.addAll(Destination.makeDestinationsEurope());
        Collections.shuffle(pileDestinations);

        //Distribution des cartes wagons aux joueurs
        for(Joueur joueur : joueurs){
            for(int i=0; i<4; i++){
                joueur.setCartesWagon(piocherCarteWagon());
            }
        }

        //Mise en place de la pile visible
        for(int i=0; i<5; i++){
            cartesWagonVisibles.add(piocherCarteWagon());
        }
        verificationLocomotivesPiocheVisible();
    }

    //Getters
    public List<CouleurWagon> getPileCartesWagon() {
        return pileCartesWagon;
    }

    public List<CouleurWagon> getCartesWagonVisibles() {
        return cartesWagonVisibles;
    }

    public List<Ville> getVilles() {
        return villes;
    }

    public List<Route> getRoutes() { return routes; }

    public Joueur getJoueurCourant() {
        return joueurCourant;
    }

    public List<CouleurWagon> getDefausseCartesWagon(){
        return defausseCartesWagon;
    }

    public List<Destination> getPileDestinations() {
        return pileDestinations;
    }



    //////Exécute la partie//////
    /* Cette méthode doit :
     * - faire choisir à chaque joueur les destinations initiales qu'il souhaite
     * garder : on pioche 3 destinations "courtes" et 1 destination "longue", puis
     * le
     * joueur peut choisir des destinations à défausser ou passer s'il ne veut plus
     * en défausser. Il doit en garder au moins 2.
     * - exécuter la boucle principale du jeu qui fait jouer le tour de chaque
     * joueur à tour de rôle jusqu'à ce qu'un des joueurs n'ait plus que 2 wagons ou
     * moins
     * - exécuter encore un dernier tour de jeu pour chaque joueur après
     */
    public void run() {
        boolean finDePartie = false;
        int tour = 0;
        List<Destination> destinationsLongues = Destination.makeDestinationsLonguesEurope();

        //Distribution des cartes destination
        for(int i=0; i<joueurs.size(); i++) {
            List<Destination> premierChoix = new ArrayList<>();
            premierChoix.add(destinationsLongues.remove(0));
            for (int j = 0; j < 3; j++){
                premierChoix.add(pileDestinations.remove(0));
            }
            joueurCourant.choisirDestinations(premierChoix,2);
            tour++;
            joueurCourant = joueurs.get(tour%joueurs.size());
        }


        while (!finDePartie) { // Tant que la partie est pas finie
            log("---------- Tour de " + joueurCourant.toLog() + " ----------");
            joueurCourant.jouerTour();
            tour++;
            if(joueurCourant.getNbWagons() <= 2){
                finDePartie = true;
            }
            joueurCourant = joueurs.get(tour%joueurs.size());
        }

        log("---------- Dernier tour ----------");
        for(int k=0; k<joueurs.size(); k++){
            log("---------- Tour de " + joueurCourant.toLog() + " ----------");
            joueurCourant.jouerTour();
            tour++;
            joueurCourant = joueurs.get(tour%joueurs.size());
        }

        List<Ville> villesJoueur = new ArrayList<>();
        List<String> routesJoueur = new ArrayList<>();
        for(Joueur joueur : joueurs){
            joueurCourant = joueur;
            if(joueurCourant.getNbGares()<3) {
                for (Ville ville : this.getVilles()) {
                    if (ville.getProprietaire() == joueurCourant) {
                        villesJoueur.add(ville);
                    }
                }
                for (Route route : this.getRoutes()) {
                    if ((villesJoueur.contains(route.getVille1()) || villesJoueur.contains(route.getVille2())) && route.getProprietaire() != null && route.getProprietaire() != joueurCourant) {
                        routesJoueur.add(route.getNom());
                    }
                }
                for (int i = 0; i < 3 - joueurCourant.getNbGares(); i++) {
                    String choixRoutesGares = joueurCourant.choisir("Veuillez choisir quelle(s) route(s) associer à vos gares", routesJoueur, new ArrayList<>(), true);

                    for (Route route : this.getRoutes()) {
                        if (choixRoutesGares.equals(route.getNom())) {
                            joueurCourant.setRouteEmpruntees(route);
                            routesJoueur.remove(route.getNom());
                            log(joueurCourant.toLog() + " a emprunté la route " + route.toLog());
                        }
                    }
                }
            }
        }

        log("---------- Tableau des scores ----------");
        for(Joueur joueur : joueurs){
            log(joueur.getNom() + " : " + joueur.getScore() + " points");
        }
        prompt("Fin de partie", new ArrayList<>(), false);
    }

    // Ajoute une carte dans la pile de défausse.
    // Dans le cas peu probable, où il y a moins de 5 cartes wagon face visibles
    // (parce que la pioche et la défausse sont vides), alors il faut immédiatement rendre cette carte face visible.

    //@param c carte à défausser
    /**     by lolo     **/
    public void defausserCarteWagon(CouleurWagon c) {
        if (pileCartesWagon.isEmpty() && defausseCartesWagon.isEmpty() && cartesWagonVisibles.size() < 5){
            cartesWagonVisibles.add(c);
        }
        else{
            defausseCartesWagon.add(c);
        }
    }

    // Pioche une carte de la pile de pioche
    // Si la pile est vide, les cartes de la défausse sont replacées dans la pioche
    // puis mélangées avant de piocher une carte

    //@return la carte qui a été piochée (ou null si aucune carte disponible)
    /**     by lolo     **/
    public CouleurWagon piocherCarteWagon() {
        CouleurWagon cartePiochee = null; //Pas encore de carte piochée
        if (pileCartesWagon.isEmpty()){ //Si la pile de cartes est vide
            if(defausseCartesWagon.isEmpty()){ //Si la défausse est aussi vide
                return cartePiochee; //On ne pioche pas
            }
            defausserDansPile();
        }
        cartePiochee = pileCartesWagon.remove(0); //On pioche une carte

        return cartePiochee; //On la retourne
    }

    // Retire une carte wagon de la pile des cartes wagon visibles.
    // Si une carte a été retirée, la pile de cartes wagons visibles est recomplétée
    // (remise à 5, éventuellement remélangée si 3 locomotives visibles)
    /**     by lolo     **/
    public void retirerCarteWagonVisible(CouleurWagon c) {

        if (!cartesWagonVisibles.isEmpty()) { //S'il y a des cartes dans la pioche visible
            joueurCourant.setCartesWagon(c); //On donne la carte au joueur
            cartesWagonVisibles.remove(c); //On la retire de la pile
            if (!pileCartesWagon.isEmpty()) {
                cartesWagonVisibles.add(piocherCarteWagon()); //On en remet une (pour en avoir toujours 5)
            }
            verificationLocomotivesPiocheVisible();
        }
    }

    // Pioche et renvoie la destination du dessus de la pile de destinations.

    //@return la destination qui a été piochée (ou `null` si aucune destination disponible)
    /**     by lolo     **/
    public Destination piocherDestination() {
        Destination destinationPioche = null;
        if(!pileDestinations.isEmpty()){
            destinationPioche = pileDestinations.remove(0);
        }
        return destinationPioche;
    }

    //Getter
    public List<Joueur> getJoueurs() {
        return joueurs;
    }

    public void defausserDansPile(){
        //Si la pile est vide mais pas la défausse
        log(pileCartesWagon.toString());
        pileCartesWagon.addAll(defausseCartesWagon); //La défausse devient la pile
        log(defausseCartesWagon.toString());
        log(pileCartesWagon.toString());
        Collections.shuffle(pileCartesWagon); //On mélange la nouvelle pile
        defausseCartesWagon.clear(); //On vide la défausse
        log(defausseCartesWagon.toString());
        while (cartesWagonVisibles.size() < 5) {
            cartesWagonVisibles.add(piocherCarteWagon());
        }
    }

    public void verificationLocomotivesPiocheVisible(){
        int compteurLocomotives = 0;
        for(CouleurWagon couleur : cartesWagonVisibles){
            if(couleur.name().equals("LOCOMOTIVE")){
                compteurLocomotives++;
            }
        }
        if(compteurLocomotives >= 3){ //S'il y a 3 locomotives dans la pioche visible
            defausseCartesWagon.addAll(cartesWagonVisibles); //On met toute la pioche visible dans la défausse
            cartesWagonVisibles.clear(); //On vide la pioche visible
            for(int i = 0; i<5;i++){ //On rajoute 5 nouvelles cartes dans la pioche visible
                cartesWagonVisibles.add(piocherCarteWagon());
            }
        }
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");
        for (Joueur j : joueurs) {
            joiner.add(j.toString());
        }
        return joiner.toString();
    }

    // Ajoute un message au log du jeu
    public void log(String message) {
        log.add(message);
    }

    // Ajoute un message à la file d'entrées
    public void addInput(String message) {
        inputQueue.add(message);
    }

    // Lit une ligne de l'entrée standard
    // C'est cette méthode qui doit être appelée à chaque fois qu'on veut lire
    // l'entrée clavier de l'utilisateur (par exemple dans {@code Player.choisir})

    // @return une chaîne de caractères correspondant à l'entrée suivante dans la file
    public String lireLigne() {
        try {
            return inputQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Envoie l'état de la partie pour affichage aux joueurs avant de faire un choix

    // @param instruction l'instruction qui est donnée au joueur
    // @param boutons labels des choix proposés s'il y en a
    // @param peutPasser indique si le joueur peut passer sans faire de choix
    public void prompt(String instruction, Collection<String> boutons, boolean peutPasser) {
        System.out.println();
        System.out.println(this);
        if (boutons.isEmpty()) {
            System.out.printf(">>> %s: %s <<<%n", joueurCourant.getNom(), instruction);
        } else {
            StringJoiner joiner = new StringJoiner(" / ");
            for (String bouton : boutons) {
                joiner.add(bouton);
            }
            System.out.printf(">>> %s: %s [%s] <<<%n", joueurCourant.getNom(), instruction, joiner);
        }

        Map<String, Object> data = Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("prompt", Map.ofEntries(
                        new AbstractMap.SimpleEntry<String, Object>("instruction", instruction),
                        new AbstractMap.SimpleEntry<String, Object>("boutons", boutons),
                        new AbstractMap.SimpleEntry<String, Object>("nomJoueurCourant", getJoueurCourant().getNom()),
                        new AbstractMap.SimpleEntry<String, Object>("peutPasser", peutPasser))),
                new AbstractMap.SimpleEntry<>("villes",
                        villes.stream().map(Ville::asPOJO).collect(Collectors.toList())),
                new AbstractMap.SimpleEntry<>("routes",
                        routes.stream().map(Route::asPOJO).collect(Collectors.toList())),
                new AbstractMap.SimpleEntry<String, Object>("joueurs",
                        joueurs.stream().map(Joueur::asPOJO).collect(Collectors.toList())),
                new AbstractMap.SimpleEntry<String, Object>("piles", Map.ofEntries(
                        new AbstractMap.SimpleEntry<String, Object>("pileCartesWagon", pileCartesWagon.size()),
                        new AbstractMap.SimpleEntry<String, Object>("pileDestinations", pileDestinations.size()),
                        new AbstractMap.SimpleEntry<String, Object>("defausseCartesWagon", defausseCartesWagon),
                        new AbstractMap.SimpleEntry<String, Object>("cartesWagonVisibles", cartesWagonVisibles))),
                new AbstractMap.SimpleEntry<String, Object>("log", log));
        GameServer.setEtatJeu(new Gson().toJson(data));
    }
}
