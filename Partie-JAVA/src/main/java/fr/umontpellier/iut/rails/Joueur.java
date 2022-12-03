package fr.umontpellier.iut.rails;

import java.util.*;
import java.util.stream.Collectors;

public class Joueur {

    // Les couleurs possibles pour les joueurs (pour l'interface graphique)
    public static enum Couleur {
        JAUNE, ROUGE, BLEU, VERT, ROSE;
    }

    // Jeu auquel le joueur est rattaché
    private Jeu jeu;

    // Nom du joueur
    private String nom;

    // CouleurWagon du joueur (pour représentation sur le plateau)
    private Couleur couleur;

    // Nombre de gares que le joueur peut encore poser sur le plateau
    private int nbGares;

    // Nombre de wagons que le joueur peut encore poser sur le plateau
    private int nbWagons;

    // Liste des missions à réaliser pendant la partie
    private List<Destination> destinations;

    // Liste des cartes que le joueur a en main
    private List<CouleurWagon> cartesWagon;

    // Liste temporaire de cartes wagon que le joueur est en train de jouer pour payer la capture d'une route ou la construction d'une gare
    private List<CouleurWagon> cartesWagonPosees;

    // Score courant du joueur (somme des valeurs des routes capturées)
    private int score;

    // Liste des routes empruntées par le joueur via gare
    private List<Route> routeEmpruntees;

    //////Initialisation d'un joueur//////
    public Joueur(String nom, Jeu jeu, Joueur.Couleur couleur) {
        this.nom = nom;
        this.jeu = jeu;
        this.couleur = couleur;
        nbGares = 3;
        nbWagons = 45;
        cartesWagon = new ArrayList<>();
        cartesWagonPosees = new ArrayList<>();
        destinations = new ArrayList<>();
        routeEmpruntees = new ArrayList<>();
        score = 12; // Chaque gare non utilisée vaut 4 points
    }

    //Getters & Setters
    public String getNom() {
        return nom;
    }

    public Couleur getCouleur() {
        return couleur;
    }

    public int getNbWagons() {
        return nbWagons;
    }

    public Jeu getJeu() {
        return jeu;
    }

    public List<CouleurWagon> getCartesWagonPosees() {
        return cartesWagonPosees;
    }

    public List<CouleurWagon> getCartesWagon() {
        return cartesWagon;
    }

    public List<Destination> getDestinations() {
        return destinations;
    }

    public int getNbGares() {
        return nbGares;
    }

    public int getScore() {
        return score;
    }

    public void setCartesWagon(CouleurWagon carteWagon) {
        this.cartesWagon.add(carteWagon);
    }

    public void setDestinations(Destination carteDestination) {
        this.destinations.add(carteDestination);
    }

    public void setNbWagons(int nbWagons) { this.nbWagons = nbWagons;
    }

    public void setRouteEmpruntees(Route route) {
        this.routeEmpruntees.add(route);
    }

    public void setCartesWagonPosees(CouleurWagon cartePosee) {
        this.cartesWagonPosees.add(cartePosee);
    }

    /**
     * Attend une entrée de la part du joueur (au clavier ou sur la websocket) et
     * renvoie le choix du joueur.
     * <p>
     * Cette méthode lit les entrées du jeu ({@code Jeu.lireligne()}) jusqu'à ce
     * qu'un choix valide (un élément de {@code choix} ou de {@code boutons} ou
     * éventuellement la chaîne vide si l'utilisateur est autorisé à passer) soit
     * reçu.
     * Lorsqu'un choix valide est obtenu, il est renvoyé par la fonction.
     * <p>
     * Si l'ensemble des choix valides ({@code choix} + {@code boutons}) ne comporte
     * qu'un seul élément et que {@code canPass} est faux, l'unique choix valide est
     * automatiquement renvoyé sans lire l'entrée de l'utilisateur.
     * <p>
     * Si l'ensemble des choix est vide, la chaîne vide ("") est automatiquement
     * renvoyée par la méthode (indépendamment de la valeur de {@code canPass}).
     * <p>
     * Exemple d'utilisation pour demander à un joueur de répondre à une question
     * par "oui" ou "non" :
     * <p>
     * {@code
     * List<String> choix = Arrays.asList("Oui", "Non");
     * String input = choisir("Voulez vous faire ceci ?", choix, new ArrayList<>(), false);
     * }
     * <p>
     * <p>
     * Si par contre on voulait proposer les réponses à l'aide de boutons, on
     * pourrait utiliser :
     * <p>
     * {@code
     * List<String> boutons = Arrays.asList("1", "2", "3");
     * String input = choisir("Choisissez un nombre.", new ArrayList<>(), boutons, false);
     * }
     *
     * @param instruction message à afficher à l'écran pour indiquer au joueur la
     *                    nature du choix qui est attendu
     * @param choix       une collection de chaînes de caractères correspondant aux
     *                    choix valides attendus du joueur
     * @param boutons     une collection de chaînes de caractères correspondant aux
     *                    choix valides attendus du joueur qui doivent être
     *                    représentés par des boutons sur l'interface graphique.
     * @param peutPasser  booléen indiquant si le joueur a le droit de passer sans
     *                    faire de choix. S'il est autorisé à passer, c'est la
     *                    chaîne de caractères vide ("") qui signifie qu'il désire
     *                    passer.
     * @return le choix de l'utilisateur (un élément de {@code choix}, ou de
     * {@code boutons} ou la chaîne vide)
     */
    public String choisir(String instruction, Collection<String> choix, Collection<String> boutons,
                          boolean peutPasser) {
        // On retire les doublons de la liste des choix
        HashSet<String> choixDistincts = new HashSet<>();
        choixDistincts.addAll(choix);
        choixDistincts.addAll(boutons);

        // Aucun choix disponible
        if (choixDistincts.isEmpty()) {
            return "";
        } else {
            // Un seul choix possible (renvoyer cet unique élément)
            if (choixDistincts.size() == 1 && !peutPasser)
                return choixDistincts.iterator().next();
            else {
                String entree;
                // Lit l'entrée de l'utilisateur jusqu'à obtenir un choix valide
                while (true) {
                    jeu.prompt(instruction, boutons, peutPasser);
                    entree = jeu.lireLigne();
                    // Si une réponse valide est obtenue, elle est renvoyée
                    if (choixDistincts.contains(entree) || (peutPasser && entree.equals("")))
                        return entree;
                }
            }
        }
    }

    // Affiche un message dans le log du jeu (visible sur l'interface graphique)

    // @param message le message à afficher (peut contenir des balises html pour la mise en forme)
    public void log(String message) {
        jeu.log(message);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add(String.format("=== %s (%d pts) ===", nom, score));
        joiner.add(String.format("  Gares: %d, Wagons: %d", nbGares, nbWagons));
        joiner.add("  Destinations: "
                + destinations.stream().map(Destination::toString).collect(Collectors.joining(", ")));
        joiner.add("  Cartes wagon: " + CouleurWagon.listToString(cartesWagon));
        return joiner.toString();
    }

    // @return une chaîne de caractères contenant le nom du joueur, avec des balises HTML pour être mis en forme dans le log
    public String toLog() {
        return String.format("<span class=\"joueur\">%s</span>", nom);
    }

    // Renvoie une représentation du joueur sous la forme d'un objet Java simple (POJO)
    public Object asPOJO() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("nom", nom);
        data.put("couleur", couleur);
        data.put("score", score);
        data.put("nbGares", nbGares);
        data.put("nbWagons", nbWagons);
        data.put("estJoueurCourant", this == jeu.getJoueurCourant());
        data.put("destinations", destinations.stream().map(Destination::asPOJO).collect(Collectors.toList()));
        data.put("cartesWagon", cartesWagon.stream().sorted().map(CouleurWagon::name).collect(Collectors.toList()));
        data.put("cartesWagonPosees",
                cartesWagonPosees.stream().sorted().map(CouleurWagon::name).collect(Collectors.toList()));
        return data;
    }

    /**
     * Propose une liste de cartes destinations, parmi lesquelles le joueur doit en
     * garder un nombre minimum n.
     * <p>
     * Tant que le nombre de destinations proposées est strictement supérieur à n,
     * le joueur peut choisir une des destinations qu'il retire de la liste des
     * choix, ou passer (en renvoyant la chaîne de caractères vide).
     * <p>
     * Les destinations qui ne sont pas écartées sont ajoutées à la liste des
     * destinations du joueur. Les destinations écartées sont renvoyées par la
     * fonction.
     *
     * @param destinationsPossibles liste de destinations proposées parmi lesquelles
     *                              le joueur peut choisir d'en écarter certaines
     * @param n                     nombre minimum de destinations que le joueur
     *                              doit garder
     * @return liste des destinations qui n'ont pas été gardées par le joueur
     */
    public List<Destination> choisirDestinations(List<Destination> destinationsPossibles, int n) {
        List<Destination> destinationsNonChoisies = new ArrayList<>();
        String choix = null;
        while (choix == null || !choix.equals("") && destinationsPossibles.size() > n) {
            HashSet<String> choixDestination = new HashSet<>();
            for (Destination destination : destinationsPossibles) {
                choixDestination.add(destination.getNom());
            }
            choix = choisir("Quelles destinations voulez-vous enlever (2 max) ?", new ArrayList<>(), choixDestination, true);
            for (Destination destination : destinationsPossibles) {
                if (destination.getNom().equals(choix)) {
                    destinationsPossibles.remove(destination);
                    destinationsNonChoisies.add(destination);
                    break;
                }
            }
        }
        destinations.addAll(destinationsPossibles);
        return destinationsNonChoisies;
    }

    /**
     * Exécute un tour de jeu du joueur.
     * <p>
     * Cette méthode attend que le joueur choisisse une des options suivantes :
     * - le nom d'une carte wagon face visible à prendre ;
     * - le nom "GRIS" pour piocher une carte wagon face cachée s'il reste des
     * cartes à piocher dans la pile de pioche ou dans la pile de défausse ;
     * - la chaîne "destinations" pour piocher des cartes destination ;
     * - le nom d'une ville sur laquelle il peut construire une gare (ville non
     * prise par un autre joueur, le joueur a encore des gares en réserve et assez
     * de cartes wagon pour construire la gare) ;
     * - le nom d'une route que le joueur peut capturer (pas déjà capturée, assez de
     * wagons et assez de cartes wagon) ;
     * - la chaîne de caractères vide pour passer son tour
     * <p>
     * Lorsqu'un choix valide est reçu, l'action est exécutée (il est possible que
     * l'action nécessite d'autres choix de la part de l'utilisateur, comme "choisir les cartes wagon à défausser pour capturer une route" ou
     * "construire une gare", "choisir les destinations à défausser", etc.)
     */
    public void jouerTour() {
        List<CouleurWagon> cartesWagonVisibles = jeu.getCartesWagonVisibles(); //Récupération des cartes wagons visibles
        List<Ville> listeVilles = jeu.getVilles(); //Récupération des villes
        List<Route> listeRoutes = jeu.getRoutes(); //Récupération des routes
        String choixTour = choixTour();

        if(choixTour.equals("LOCOMOTIVE")){ //Le joueur pioche UNE SEULE locomotive de la pioche visible
            jeu.retirerCarteWagonVisible(CouleurWagon.LOCOMOTIVE); //On lui rajoute
            log(this.toLog() + " a pioché " + CouleurWagon.LOCOMOTIVE.toLog());
        }

        if(!cartesWagonVisibles.isEmpty()){
            for(CouleurWagon couleurWagon : cartesWagonVisibles) {
                if (couleurWagon.name().equals(choixTour) && !couleurWagon.name().equals("LOCOMOTIVE")) { //Le joueur pioche une carte visible
                    jeu.retirerCarteWagonVisible(couleurWagon); //On lui rajoute
                    log(this.toLog() + " a pioché " + couleurWagon.toLog());
                    piocherDeuxiemeCarte(); //Il pioche une 2e carte
                    break;
                }
            }
        }

        if(choixTour.equals("GRIS")){ //Le joueur pioche une carte dans la pioche
            this.cartesWagon.add(jeu.piocherCarteWagon()); //On lui rajoute
            log(this.toLog() + " a pioché");
            piocherDeuxiemeCarte(); //Il pioche une 2e carte
        }

        if(choixTour.equals("destinations")){ //Le joueur pioche 3 cartes destination
            List<Destination> destinationsPossibles = new ArrayList<>();
            for(int i = 0; i < 3; i++){
                if(!jeu.getPileDestinations().isEmpty()){
                    destinationsPossibles.add(jeu.getPileDestinations().remove(0));
                }
            }
            jeu.getPileDestinations().addAll(choisirDestinations(destinationsPossibles,1));
            log(this.toLog() + " a pioché des destinations");
        }

        for(Ville ville : listeVilles){
            if(ville.getNom().equals(choixTour)){
                poserGares(ville);
                log(this.toLog() + " a posé une gare sur " + ville.toLog());
                break;
            }
        }

        for(Route route : listeRoutes){
            if(route.getNom().equals(choixTour)){
                route.prendreRoute(this, jeu);
                calculerPointsRoute(route);
                log(this.toLog() + " a capturé la route " + route.toLog());
                break;
            }
        }
    }

    public String choixTour(){
        ///////////////INITIALISATION DES CHOIX///////////////
        List<CouleurWagon> cartesWagonVisibles = jeu.getCartesWagonVisibles(); //Récupération des cartes wagons visibles
        List<Ville> listeVilles = jeu.getVilles(); //Récupération des villes
        List<Route> listeRoutes = jeu.getRoutes(); //Récupération des routes
        ArrayList<String> choix = new ArrayList<>(); //Création d'une liste de string

        if(!cartesWagonVisibles.isEmpty()) {
            for (CouleurWagon carte : cartesWagonVisibles) {
                choix.add(carte.name()); //Ajout de toutes les cartes wagons visibles à la liste
            }
        }

        for(Ville ville : listeVilles){
            if(this.peutPoserGare(ville)){ //Route sans propriétaire, où le joueur peut poser sa gare (assez de cartes et de gares)
                choix.add(ville.getNom()); //Ajout de toutes les villes possibles
            }
        }

        for(Route route : listeRoutes){
            if(route.peutPrendreRoute(this, jeu)){  //Route sans propriétaire, que le joueur peut prendre (assez de cartes, assez de wagon et pas une route double déjà prise)
                choix.add(route.getNom()); //Ajout de toutes les routes possibles
            }
        }

        if(!jeu.getPileCartesWagon().isEmpty() || !jeu.getDefausseCartesWagon().isEmpty()) {
            choix.add("GRIS"); //Ajout de la pioche de cartes wagon
        }

        if(!jeu.getPileDestinations().isEmpty()){
            choix.add("destinations"); //Ajout de la pioche de destinations
        }

        choix.add(""); //Ajout de l'option passer

        return this.choisir("Que voulez-vous faire à ce tour ?", choix, new ArrayList<>(),true);
    }

    public void piocherDeuxiemeCarte(){
        List<CouleurWagon> cartesWagonVisibles = jeu.getCartesWagonVisibles(); //Récupération de la nouvelle pile visible
        ArrayList<String> choix = new ArrayList<>(); //Création d'un nouveau choix
        if(!cartesWagonVisibles.isEmpty()) {
            for (CouleurWagon carte : cartesWagonVisibles) {
                if (!carte.name().equals("LOCOMOTIVE")) { //Tant que ce n'est pas une locomotive
                    choix.add(carte.name()); //Ajout de toutes les cartes wagons visibles à la liste (hors locomotives)
                }
            }
        }
        if(!jeu.getPileCartesWagon().isEmpty() || !jeu.getDefausseCartesWagon().isEmpty()) {
            choix.add("GRIS"); //Ajout de la pioche de cartes wagon
        }
        choix.add("");
        String choixPioche = this.choisir("Piochez une deuxième carte (dans la pile ou dans la pioche, hors locomotive)", choix, new ArrayList<>(), true);

        if(choixPioche.equals("GRIS")){
            this.cartesWagon.add(jeu.piocherCarteWagon());
            log(this.toLog() + " a pioché ");
        }

        for(CouleurWagon couleurWagon : cartesWagonVisibles) {
            if (couleurWagon.name().equals(choixPioche)) {
                jeu.retirerCarteWagonVisible(couleurWagon);
                log(this.toLog() + " a pioché " + couleurWagon.toLog());
                break;
            }
        }
    }

    public boolean peutPoserGare(Ville ville){
        int prixGare = 4-nbGares;
        List<CouleurWagon> listeCouleurWagons = CouleurWagon.getCouleursSimples();
        if(ville.getProprietaire() == null && nbGares>0) {
            for(CouleurWagon couleurWagon : listeCouleurWagons){
                    if(nombreCouleurWagonJoueur(CouleurWagon.LOCOMOTIVE) + nombreCouleurWagonJoueur(couleurWagon) >= prixGare){
                        return true;
                    }
                }
            }
        return false;
    }

    public void poserGares(Ville ville){
        int prixGare = 4-nbGares;
        CouleurWagon couleurChoisie = null;
        List<CouleurWagon> listeCouleurWagons = CouleurWagon.getCouleursSimples();
        ArrayList<String> choixCartesPossibles = new ArrayList<>();
        for(CouleurWagon couleurWagon : listeCouleurWagons){
            if(nombreCouleurWagonJoueur(CouleurWagon.LOCOMOTIVE) + nombreCouleurWagonJoueur(couleurWagon) >= prixGare){
                choixCartesPossibles.add(couleurWagon.name());
            }
        }
        choixCartesPossibles.add(CouleurWagon.LOCOMOTIVE.name());

        while(prixGare != 0){
            String choixJoueur = choisir("Choisissez " + prixGare + " carte(s) à défausser", choixCartesPossibles, new ArrayList<>(), false);

            if(choixJoueur.equals(CouleurWagon.LOCOMOTIVE.name())){
                this.getCartesWagon().remove((CouleurWagon.LOCOMOTIVE));
                jeu.defausserCarteWagon(CouleurWagon.LOCOMOTIVE);
                prixGare--;
            }

            if(couleurChoisie==null){
                for(CouleurWagon couleurWagon : listeCouleurWagons){
                    if(choixJoueur.equals(couleurWagon.name())){
                        couleurChoisie = couleurWagon;
                        this.getCartesWagon().remove(couleurChoisie);
                        jeu.defausserCarteWagon(couleurChoisie);
                        prixGare--;
                    }
                }
            }
            else if(choixJoueur.equals(couleurChoisie.name())){
                this.getCartesWagon().remove(couleurChoisie);
                jeu.defausserCarteWagon(couleurChoisie);
                prixGare--;
            }
        }
        ville.setProprietaire(this);
        nbGares--;
        score -= 4;
    }


    public void calculerPointsRoute(Route route){
        switch (route.getLongueur()) {
            case 1 -> score++;
            case 2 -> score += 2;
            case 3 -> score += 4;
            case 4 -> score += 7;
            case 6 -> score += 15;
            case 8 -> score += 21;
        }
    }

    public int nombreCouleurWagonJoueur(CouleurWagon couleur){
        List<CouleurWagon> listeCartesWagon = this.getCartesWagon();
        return Collections.frequency(listeCartesWagon, couleur);
    }
}

