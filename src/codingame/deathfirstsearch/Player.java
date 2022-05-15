package codingame.deathfirstsearch;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Algorithme Minimax / algorithme Alha/B�ta.
 **/
class Player {

    private static final int MAX_PROF = 2;
    int nbSommets = 0;
    int nbArcs = 0;
    int nbExits = 0;
    
    int nbPositionsEtude = 0;
    
    EtatReseau etatReseau = new EtatReseau();
    
    void joue(InputStream inStream) {
        Scanner in = new Scanner(inStream);
        boolean debug = true;
        
        nbSommets = in.nextInt(); // the total number of nodes in the level, including the gateways
        nbArcs = in.nextInt(); // the number of links
        nbExits = in.nextInt(); // the number of exit gateways
        System.err.println(""+nbSommets+" "+nbArcs+" "+nbExits);
        
        for (int i = 0; i < nbArcs; i++) {
            int n1 = in.nextInt(); // N1 and N2 defines a link between these nodes
            int n2 = in.nextInt();
            System.err.println(""+n1+" "+n2);
            
            etatReseau.arcs.add(new int[] {n1, n2});
        }
        
        for (int i = 0; i < nbExits; i++) {
            int exit = in.nextInt(); // the index of a gateway node
            etatReseau.exits.add(exit);
            System.err.println(""+exit);
        }

        // game loop
        while (true) {
            etatReseau.noeudVirus = in.nextInt(); // The index of the node on which the Skynet agent is positioned this turn
            System.err.println(""+etatReseau.noeudVirus);
            
            nbPositionsEtude = 0;
            long ts = System.nanoTime();
            int[] arc = strategie(etatReseau);
            System.err.println("nbPositionsEtude:"+nbPositionsEtude+", duree ms:"+((System.nanoTime()- ts) / 1_000_000));
            
            System.out.println(arc[0]+" "+arc[1]);
            etatReseau.supprimeArc(arc);
        }
        
    }
        
    private int[] strategie(EtatReseau etatReseau) {
        
        // Algorithme Min/Max.
        MeilleurCoupReseau meilleurCoup = rechercheMeilleurCoupReseau(etatReseau, 1, false);
        return meilleurCoup.coup;
    }
    
    MeilleurCoupReseau rechercheMeilleurCoupReseau(EtatReseau etatReseau, int prof,
            boolean etudeCoupsForcesUniquement) {
        String chevrons = getChevrons(prof);
        
        List<int[]> coups = etatReseau.getArcsACouper();
        nbPositionsEtude += coups.size();
        log(chevrons, "nb coups Reseau:", coups.size());
        
        // 1/ Le r�seau a coup� tous les arcs menant aux sorties. Il a gagn�. On sort.
        
        if (coups.size() == 0) {
            log(chevrons, "Le reseau a gagn�: plus d'arcs menant aux sortie");
            return new MeilleurCoupReseau(1, etatReseau.arcs.get(0)); // Peu importe
        }
        
        // 2/ Recherche de coups forc�s "�vidents" et sortie imm�diate.
        List<int[]> coupsForces = coups.stream()
                    .filter(coup -> coup[0] == etatReseau.noeudVirus || coup[1] == etatReseau.noeudVirus)
                    .collect(Collectors.toList());
                    
        if (coupsForces.size() == 2) {
            log(chevrons, "2 coup forc�s pour le reseau = perte:", Arrays.toString(coupsForces.get(0)));
            return new MeilleurCoupReseau(-1, coupsForces.get(0));
        }
        
        boolean coupForce = false;
        if (coupsForces.size() == 1) {
            log(chevrons, "Coup forc� pour le reseau:", Arrays.toString(coupsForces.get(0)));
            // On va �tudier uniquement les cons�quences de ce coup forc�
            coups = coupsForces;
            coupForce = true;
        }
        
        if (etudeCoupsForcesUniquement && !coupForce) {
            return new MeilleurCoupReseau(0, coups.get(0)); // Peu importe 
        }
        
        // 3/ Sinon analyse r�cursive des coups possibles du r�seau 
        
        // Etude des coups du r�seau
        
        int[] coupSelect = null;
        int meilleurCoup = Integer.MIN_VALUE;
        
        for (int[] coup : coups) {
            log(chevrons, "coup reseau:", Arrays.toString(coup));
            
            // On simule un coup du r�seau et on obtient un nouvel �tat a �tudier
            etatReseau.supprimeArc(coup);
            // On �tudie ce coup r�cursivment
            int valCoup = rechercheMeilleurCoupBobnet(etatReseau, prof,
                    etudeCoupsForcesUniquement || (prof >= MAX_PROF && coupForce));
            // On revient en arri�re sur le coup
            etatReseau.arcs.add(coup);
            
            if (valCoup > meilleurCoup) {
                log(chevrons, " -- select. coup: ", Arrays.toString(coup), ", val:", valCoup);
                coupSelect = coup;
                meilleurCoup = valCoup;
                // Ersatz d'algorithme alpha-beta
                if (meilleurCoup >= 0) {
                    log(chevrons, " -- fin. on trouvera pas mieux.");
                    break;
                }
            }
        }
        
        return new MeilleurCoupReseau(meilleurCoup, coupSelect);
    }

    
    int rechercheMeilleurCoupBobnet(EtatReseau etatReseau, int prof,
            boolean etudeCoupsForcesUniquement) {
        String chevrons = ">" + getChevrons(prof);
        
        List<Integer> coups = etatReseau.getCoupsVirus();
        log(chevrons, "nb coups Bobnet:", coups.size(), "=", Arrays.toString(coups.toArray()));
        nbPositionsEtude += coups.size();
        
        // 1/ Recherche de coups forc�s "�vidents" et sortie imm�diate.
        
        Optional<Integer> coupGagnant = coups.stream()
            .filter(coup -> etatReseau.exits.contains(coup))
            .findAny();
        if (coupGagnant.isPresent()) {
            log(chevrons, " > COUP GAGNANT BOBNET:", coupGagnant.get());
            return -1;
        }
        
        // 2/ Sinon analyse r�cursive des coups du virus pour rechercher un coup gagnant 
        
        if (prof <= MAX_PROF || etudeCoupsForcesUniquement) {
            
            int meilleurCoup = Integer.MAX_VALUE;
            
            for (int coup : coups) {
                log(chevrons, "coup Bobnet:", coup);
                int localisationVirusActuel = etatReseau.noeudVirus;
                etatReseau.deplaceVirus(coup);
                MeilleurCoupReseau meilleur = rechercheMeilleurCoupReseau(etatReseau, prof + 1, etudeCoupsForcesUniquement);
                etatReseau.deplaceVirus(localisationVirusActuel);
                log(chevrons, "meilleur.val:", meilleur.val);
                // Ersatz d'algorithme alpha-beta
                // Si le meilleur coup est val = -1, on peut sortir tout de suite
                if (meilleur.val == -1) {
                    log(chevrons, "return: -1");
                    return -1;
                }
                if (meilleur.val < meilleurCoup) {
                    meilleurCoup = meilleur.val;
                }
            }
            log(chevrons, "return:", meilleurCoup);
            return meilleurCoup;
            
        }

        log(chevrons, " > pas de coup gagnant");
        return 0;
    }
    
    class MeilleurCoupReseau {
        int val;
        int[] coup;

        public MeilleurCoupReseau(int val, int[] coup) {
            super();
            this.val = val;
            this.coup = coup;
        }
        
    }
    
    /**
     * Classe qui symbolise l'�tat du r�seau
     *
     */
    class EtatReseau {
        int noeudVirus;
        List<Integer> exits = new ArrayList<>();
        LinkedList<int[]> arcs = new LinkedList<>();
        
        EtatReseau cloneEtat() {
            EtatReseau newEtat = new EtatReseau();
            newEtat.noeudVirus = noeudVirus;
            arcs.forEach(newEtat.arcs::add);
            newEtat.exits = exits;
            return newEtat;
        }

        LinkedList<int[]> getArcsACouper() {
            LinkedList<int[]> ret = new LinkedList<>();
            
            for (int exit : exits) {
                for (int[] arc : arcs) {
                    if (arc[0] == exit || arc[1] == exit) {
                        ret.add(arc);
                    }
                }
            }
        
            return ret;
        }
        
        List<Integer> getCoupsVirus() {
            List<Integer> coups = new LinkedList<>();
            arcs.stream().filter(arc -> arc[0] == noeudVirus).forEach(arc -> coups.add(arc[1]));
            arcs.stream().filter(arc -> arc[1] == noeudVirus).forEach(arc -> coups.add(arc[0]));
            return coups;
        }
        
        EtatReseau supprimeArc(int[] arc) {
            int iSelect = -1;
            
            for (int i = 0; i<arcs.size(); i++) {
                if (arcs.get(i)[0] == arc[0] && arcs.get(i)[1] == arc[1]) {
                    iSelect = i;
                }
            }
            if (iSelect > -1) arcs.remove(iSelect);
            return this;
        }
        
        EtatReseau deplaceVirus(int coup) {
            noeudVirus = coup;
            return this;
        }
       
    }
    
    /* Codingame common */
    
    static boolean doLog = false;
    static void log(Object... objects) {
        if (doLog) {
            for (Object o : objects) {
                System.err.print(o.toString() + " ");
            }
            System.err.println();
        }
    }

    static String getChevrons(int nbChevrons) {
        String ret = "";
        for (int i = 0; i<nbChevrons; i++) ret += ">";
        return ret;
    }
    
    static void activateLog() {
        doLog = true;
    }
    
    public static void main(String args[]) {
        Player player = new Player();
        player.joue(System.in);
    }

}
