package codingame.madpodracing;

import java.io.InputStream;
import java.util.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {
    enum Mode {COURSE, ATTAQUE};
    
    static boolean doLog = true;
    static boolean doLogDebug = false;
    protected static final int PAS_ATTEINT = 999;
    private static final double FROTTEMENT = 0.85D;
    private static final int MAX_ANGLE = 80;

    int laps;
    int round = 1;
    int checkpointCount;
    List<Coords> checkpoints = new ArrayList<>();
    List<Unit> opponents;
    List<Pod> pods;
    List<Pod> podsPrec = null;
    
    LinkedList<Integer>[] checkpointsDone = new LinkedList[4];
    
    boolean[] boostFait = new boolean[2];
    
    Player() {

        for (int i = 0; i < checkpointsDone.length; i++) {
            checkpointsDone[i] = new LinkedList<Integer>();
            checkpointsDone[i].add(0); 
        }
        
    }
    
    void joue(InputStream inStream) {
        Scanner in = new Scanner(inStream);
        
        laps = in.nextInt();
        checkpointCount = in.nextInt();
        System.err.println(laps + " " + checkpointCount);
        
        for (int i = 0; i < checkpointCount; i++) {
            int checkpointX = in.nextInt();
            int checkpointY = in.nextInt();
            
            System.err.println(checkpointX + " " + checkpointY);

            checkpoints.add(new Coords(checkpointX, checkpointY));
        }

        // game loop
        
        podsPrec = null;
        
        while (true) {
            pods = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                Pod pod = new Pod();
                pod.id= i; 
                pod.x = in.nextInt(); // x position of your pod
                pod.y = in.nextInt(); // y position of your pod
                pod.v.x = in.nextInt(); // x speed of your pod
                pod.v.y = in.nextInt(); // y speed of your pod
                pod.angle = in.nextInt(); // angle of your pod
                pod.nextCheckPointId = in.nextInt(); // next check point id of your pod
                
                System.err.println((int) pod.x + " " + (int) pod.y + " " + pod.v.x
                        + " " + pod.v.y + " " + pod.angle + " " + pod.nextCheckPointId);

                pods.add(pod);
            }
            
            opponents = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                Unit opponent = new Unit();
                opponent.id= i; 
                opponent.x = in.nextInt(); // x position of the opponent's pod
                opponent.y = in.nextInt(); // y position of the opponent's pod
                opponent.v.x = in.nextInt(); // x speed of the opponent's pod
                opponent.v.y = in.nextInt(); // y speed of the opponent's pod
                opponent.angle = in.nextInt(); // angle of the opponent's pod
                opponent.nextCheckPointId = in.nextInt(); // next check point id of the opponent's pod
                
                System.err.println((int) opponent.x + " " + (int) opponent.y + " " + opponent.v.x
                        + " " + opponent.v.y + " " + opponent.angle + " " + opponent.nextCheckPointId);
                
                opponents.add(opponent);
            }
            
            for (int i = 0; i < 2; i++) {
                log("### pod:", i+1);
                strategie(pods.get(i), podsPrec == null ? null : podsPrec.get(i));
                System.out.println(pods.get(i).ordre.toCommand());
            }
             
            podsPrec = pods;
            round++;
        }
    }
    
    private void strategie(Pod pod, Pod podPrec) {
        
        // Neurones couche 1 = Enrichissement
        recupInfosCircuit(pod, podPrec);
        classementPods(pod, podPrec);
        ChangementMode(pod, podPrec);
        AjoutOpponent(pod, podPrec);
        enrichissement(pod, podPrec);
        
        // Neurones couche 2 = Stratégie élaborée 
        strategieBoostUnPod(pod, podPrec);
        strategieBoostEnLigneDroite(pod, podPrec);
        strategieVirageParfait(pod, podPrec);
        strategieCorrectionDerive(pod, podPrec);
        strategieEpauleContreEpaule(pod, podPrec);
        
        // Neurones couche 3 = Stratégies minimalistes (par défaut)
        strategieCibleParDefaut(pod, podPrec);
        defautThrust(pod, podPrec);
        
        // Neurones du mode attaque
        shield(pod, podPrec);
        modeAttaqueInterception(pod, podPrec);
        
        // Neurone de sortie
        ordreThrust(pod, podPrec);
        
    }
    
    private void recupInfosCircuit(Pod pod, Pod podPrec) {

        // Récup coords du prochaine checkpoint
        pod.nextCheckpoint = checkpoints.get(pod.nextCheckPointId);
        
        Coords vecteurVersCheckpoint = pod.createVecteurVers(pod.nextCheckpoint);
        
        pod.nextCheckpointDist = vecteurVersCheckpoint.getVNorme();
        
        AngleEnDegres angleCheckpoint = vecteurVersCheckpoint.getVAngleDegres();
        AngleEnDegres angleRelatifCheckpoint = angleCheckpoint.retire(pod.angle);
        
        // angle between your pod orientation and the direction of the next checkpoint
        pod.nextCheckpointAngle = Math.abs((int) angleRelatifCheckpoint.angle);
        
        logDebug("angCP:", angleCheckpoint, "angRelCP:", angleRelatifCheckpoint, "nextCPAng:", pod.nextCheckpointAngle);
        
        // Récup coords du checkpoint futur
        for (int i=0; i<checkpoints.size(); i++) {
            if (checkpoints.get(i).equals(pod.nextCheckpoint)) {
                if (i < checkpoints.size()-1) {
                    pod.futureCheckpoint = checkpoints.get(i+1);
                } else {
                    pod.futureCheckpoint = checkpoints.get(0);
                }
                break;
            }
        }
        
        pod.angleFutureCheckpoint = pod.nextCheckpoint.angleVecteurs(pod, pod.futureCheckpoint);
        logDebug(">> future checkpoint:", pod.futureCheckpoint, "angleFutureCheckpoint:", pod.angleFutureCheckpoint);
    }

    private void classementPods(Pod pod, Pod podPrec) {
        
        for (int i=0; i<2; i++) {
            int lastCheckpointId = checkpointsDone[i].getFirst();
            if (pods.get(i).nextCheckPointId != lastCheckpointId) {
                checkpointsDone[i].addFirst(pods.get(i).nextCheckPointId);               
            }
            logDebug("checkpointsDone", i, "size", checkpointsDone[i].size());
            
            lastCheckpointId = checkpointsDone[i+2].getFirst();
            if (opponents.get(i).nextCheckPointId != lastCheckpointId) {
                checkpointsDone[i+2].addFirst(opponents.get(i).nextCheckPointId);               
            }
            logDebug("checkpointsDone", i+2, "size", checkpointsDone[i+2].size());
        }
        
        logDebug("pods.get(0)", pods.get(0), "pods.get(0).nextCheckpoint", pods.get(0).nextCheckpoint);
        logDebug("pods.get(1)", pods.get(1));
        
        if (checkpointsDone[0].size() > checkpointsDone[1].size()
         || (checkpointsDone[0].size() == checkpointsDone[1].size()
                 && pods.get(0).nextCheckpoint.compareDistance(pods.get(0), pods.get(1)) < 0 )) {
            pods.get(0).rang = 1;
            pods.get(1).rang = 2;
        } else {             
            pods.get(0).rang = 2;
            pods.get(1).rang = 1;
        }
        
        if (checkpointsDone[2].size() > checkpointsDone[3].size()
         || (checkpointsDone[2].size() == checkpointsDone[3].size()
                && checkpoints.get(opponents.get(0).nextCheckPointId).compareDistance(opponents.get(0), opponents.get(1)) < 0 )) {
            opponents.get(0).rang = 1;
            opponents.get(1).rang = 2;
        } else {             
            opponents.get(0).rang = 2;
            opponents.get(1).rang = 1;
        }
        
        log("Classement:", pods.get(0).rang, pods.get(1).rang, opponents.get(0).rang, opponents.get(1).rang);
    }

    private void ChangementMode(Pod pod, Pod podPrec) {
        pod.mode = Mode.COURSE;
        
        if (pod.rang == 2 && checkpointsDone[pod.id].size() > 3) {
            pod.mode = Mode.ATTAQUE;
        }
        log("mode:", pod.mode);
    }

    private void AjoutOpponent(Pod pod, Pod podPrec) {
        Unit o1 = opponents.get(0);
        Unit o2 = opponents.get(1);
        
        double w1 = 0;
        if (checkpoints.get(o1.nextCheckPointId).equals(pod.nextCheckpoint) 
            && checkpoints.get(o1.nextCheckPointId).equals(pod.nextCheckpoint)) {
            pod.opponent = pod.compareDistance(o1, o2) < 0 ? o1 : o2;
        } else if (checkpoints.get(o1.nextCheckPointId).equals(pod.nextCheckpoint)) {
            pod.opponent = o1;
        } else if (checkpoints.get(o2.nextCheckPointId).equals(pod.nextCheckpoint)) {
            pod.opponent = o2;
        } else {
            return;
        }
        
        log("Opponent:", pod.opponent.id+1);
        pod.distOpponentAuCheckpoint = pod.opponent.distance(pod.nextCheckpoint); 
        pod.distOpponentAuPod = pod.opponent.distance(pod); 
    }

    private void enrichissement(Pod pod, Pod podPrec) {
        
        pod.distOpponentAuCheckpoint = pod.opponent.distance(pod.nextCheckpoint);
        pod.distOpponentAuPod = pod.opponent.distance(pod);
        
        // Validation des conditions d'activation du neurone
        if (podPrec != null) /* continue */;
        else return;
        
        pod.vitesse = podPrec.distance(pod);
        
        //---- Calcul vitesse relative au nextCheckpoint
        
        double distCheckPoint = pod.distance(pod.nextCheckpoint);
        double distCheckPointPrec = podPrec.distance(pod.nextCheckpoint);
        
        logDebug("distCheckPoint:", distCheckPoint, "distCheckPointPrec:", distCheckPointPrec);
        
        pod.vitesseRelative = distCheckPointPrec - distCheckPoint;
        logDebug("vitesse:", pod.vitesse, "vitesseRelative:", pod.vitesseRelative);
    }

    private void strategieBoostUnPod(Pod pod, Pod podPrec) {
        // Validation des conditions d'activation du neurone
        if (pod.id == 0 && round == 1) /* continue */;
        else return;
        
        log("---- Strategie boost POD 1 ----");
        pod.ordre.boost = true;
    }

    private void strategieBoostEnLigneDroite(Pod pod, Pod podPrec) {
        // Validation des conditions d'activation du neurone
        if (boostFait[pod.id]) return;
        if (pod.mode == Mode.ATTAQUE) return;
         
        double distPodToOponent = pod.distance(pod.opponent);
        log("distPodToOponent:", distPodToOponent, "distPodToCheckpoint:", pod.nextCheckpointDist);
        
        if (distPodToOponent > 2_000
            && pod.nextCheckpointDist > 5_000
            && Math.abs(pod.nextCheckpointAngle) < 3) /* continue */;
        else return;
        
        log("---- Strategie Boost En Ligne Droite ----");
        
        // Calculs
        
        // Données en sortie
        pod.ordre.boost = true;
    }

    private void strategieVirageParfait(Pod pod, Pod podPrec) {
        if (pod.mode == Mode.ATTAQUE) return;
        
        int DISTANCE_DECLENCHEMENT = 4_000;
        int SECONDES_MAX = 20;

        if (pod.compareDistance(pod.nextCheckpoint, DISTANCE_DECLENCHEMENT) < 0) /* continue */;
        else return;
        
        int tempsAvecFullThrust = 999;
        
        int noThrust = 0;
        boolean continuer = true;
        
        // Simulation pour savoir s'il faut couper le thrust ou pas pour le virage
        
        while (noThrust < 5 && continuer) {
            
            // Simulation Checkpoint et nexCheckpoint avec un nb de "noThrust" sans thrust.
            Simulation simul = simulationCheckpointEtNext(pod, pod.nextCheckpoint,
                    pod.futureCheckpoint, noThrust, SECONDES_MAX, 500);
            
            int t = simul.tempsCheckpoint;
            log("Temps virage parfait:", t, "pour thrust:", noThrust, "simul", simul);
            
            if (t <= SECONDES_MAX) {
                if (noThrust == 0) {
                    log("---- Strategie Virage Parfait ----");
                    pod.ordre.cible = pod.futureCheckpoint;
                    pod.ordre.thrust = 100;

                    tempsAvecFullThrust = t;
                } else {
                    if (t < tempsAvecFullThrust) {
                        log("Meilleur de couper le thrust");
                        pod.ordre.thrust = 0;
                        continuer = false;
                    }
                }
            }
            noThrust++;
        }
    }
         
    private void strategieCorrectionDerive(Pod pod, Pod podPrec) {
        
        // Validation des conditions d'activation du neurone
        if (pod.mode == Mode.ATTAQUE) return;
        if (podPrec != null && pod.nextCheckpointDist > 2_000) /* continue */;
        else return;
        
        Coords vecteurDirection = pod.createVecteurVers(pod.nextCheckpoint);
        logDebug("vecteurDirection", vecteurDirection.showVector());
        logDebug("dir pod", pod.nextCheckpointAngle);
        
        Coords vecteurVitesse = podPrec.createVecteurVers(pod);
        logDebug("vecteurVitesse", vecteurVitesse.showVector());

        double ecart = vecteurDirection.getVAngleDegres().retire(vecteurVitesse.getVAngleDegres()).angle;
        logDebug("Ecart", ecart);
        if (Math.abs(ecart) < 6) return;
        
        Coords vecteurDeCorrection = new Coords(-vecteurVitesse.x*2, -vecteurVitesse.y*2);
        Coords cibleCorrige = pod.nextCheckpoint.doVtranslation(vecteurDeCorrection);
        Coords vecteurOptimal = pod.createVecteurVers(cibleCorrige, pod.nextCheckpointDist);
        Coords newCible = pod.doVtranslation(vecteurOptimal);
        double distCibleVsNextCheckpoint = newCible.distance(pod.nextCheckpoint);
        
        logDebug("vecteurDeCorrection", vecteurDeCorrection.showVector());
        logDebug("cibleCorrige", cibleCorrige);
        logDebug("vecteurOptimal", vecteurOptimal.showVector());
        logDebug("Dist cible vs nextCheckpoint", distCibleVsNextCheckpoint);
        if (distCibleVsNextCheckpoint < 100) return;

        log("---- Strategie Correction Derive ----");
        
        pod.ordre.cible = newCible;
    }

    private void strategieEpauleContreEpaule(Pod pod, Pod podPrec) {
        if (pod.mode == Mode.ATTAQUE) return;
        
        logDebug("** Distance nextCheckpoint:", pod.nextCheckpointDist);
        logDebug("** Distance opponent nextCheckpoint:", pod.distOpponentAuCheckpoint);
        logDebug("** Distance opponent//pod:", pod.distOpponentAuPod);
        
        double angle = pod.angleVecteurs(pod.nextCheckpoint, pod.opponent).angle;
        logDebug("** Angle:", angle);
        
        double deltaDistanceVersCheckpoint = Math.abs(pod.nextCheckpointDist - pod.distOpponentAuCheckpoint);
        logDebug("** deltaDistanceVersCheckpoint:", deltaDistanceVersCheckpoint);
        logDebug("** cible actuelle:", ""+pod.ordre.cible);
        
        if (pod.ordre.cible == null
            && Math.abs(angle) > 40
            && Math.abs(angle) < 130
            //&& pod.compareDistance(pod.opponent, 2_000) < 0
            && Math.abs(pod.nextCheckpointDist - pod.distOpponentAuCheckpoint) < 1_000) /* continue */;
        else return; 

        log("---- Strategie Epaule Contre Epaule ----");
                        
        Coords v = pod.nextCheckpoint.createVecteurVers(pod, 400);
        if (angle > 0) 
            v = v.rotation(-Math.PI / 2);
        else v = v.rotation(Math.PI / 2);
        pod.ordre.cible = v.doVtranslation(pod.nextCheckpoint);
    }
    
    private void strategieCibleParDefaut(Pod pod, Pod podPrec) {
        
        // Validation des conditions d'activation du neurone
        if (pod.mode == Mode.ATTAQUE) return;
        if (pod.ordre.cible == null) /* continue */;
        else return;

        log("---- Strategie Cible Par Defaut ----");
        
        // Calculs
        
        // Données en sortie
        log("cible par defaut:", pod.nextCheckpoint);
        pod.ordre.cible = pod.nextCheckpoint;
    }
    
    private void defautThrust(Pod pod, Pod podPrec) {
        
        // Validation des conditions d'activation du neurone
        if (pod.mode == Mode.ATTAQUE) return;
        if (pod.ordre.boost && Double.isNaN(pod.newThrust)) {
            return;
        }
        
        log("---- Strategie Defaut Thrust ----");
         
        pod.newThrust = 100;

        // Check pour voir s'il n'est pas préférable de couper le thrust
        int tempsAvecFullThrust = PAS_ATTEINT;
        
        int noThrust = 0;
        boolean continuer = true;
        
        // Simulation pour savoir si on arrive a atteindre le checkpoint suivant avec thrust
        // ou s'il faut le couper un peu
        
        while (noThrust < 5 && continuer) {
            Simulation simul = simulationCheckpointEtNext(
                    pod,
                    pod.nextCheckpoint /* checkpoint */,
                    pod.nextCheckpoint /* cible */,
                    noThrust /* noThrust */,
                    30 /* profMax */, 500 /* distance */);
            int t = simul.tempsCheckpoint;
            log("Temps:", t, "pour thrust:", noThrust, "simul", simul);
            
            if (noThrust == 0) {
                tempsAvecFullThrust = t;
            } else {
                if (t < tempsAvecFullThrust) {
                    log("!! Meilleur de couper le thrust", t, tempsAvecFullThrust);
                    pod.newThrust = 0;
                    continuer = false;
                }
            }
            noThrust++;
        }
        
    }

    private void ordreThrust(Pod pod, Pod podPrec) {
        
        // Validation des conditions d'activation du neurone
        if (pod.mode == Mode.ATTAQUE) return;
        if (pod.ordre.boost) {
            boostFait[pod.id] = true;
            return;
        }
          
        pod.ordre.thrust = Math.max(Math.min((int)pod.newThrust, 100), 0);
        if (Math.abs(pod.nextCheckpointAngle) > MAX_ANGLE) pod.ordre.thrust = 0;
        log("thrust brut:", pod.newThrust, " > ", pod.ordre.thrust);
        
    }

    private void shield(Pod pod, Pod podPrec) {
        
        // Validation des conditions d'activation du neurone
        if (pod.mode == Mode.COURSE) return;
        if (pod.ordre.shield) return;
                
        double dist1 = pod.v.doVtranslation(pod).distance(opponents.get(0).v.doVtranslation(opponents.get(0)));
        
        logDebug("dist1:", dist1);
        
        if (dist1 < 800) {
            log("---- SHIELD 1 ----", "dist1:", dist1);
            pod.ordre.cible = opponents.get(0);
            pod.ordre.shield = true;
            return;
        }

        double dist2 = pod.v.doVtranslation(pod).distance(opponents.get(1).v.doVtranslation(opponents.get(1)));
        
        logDebug("dist2:", dist2);
        
        if (dist2 < 800) {
            log("---- SHIELD 2 ----", "dist2:", dist2);
            pod.ordre.cible = opponents.get(1);
            pod.ordre.shield = true;
        }
 
    }
    
    private void modeAttaqueInterception(Pod pod, Pod podPrec) {
        
        // Validation des conditions d'activation du neurone
        if (pod.mode == Mode.COURSE) return;
        if (pod.ordre.shield) return;
        
        // Calculs
        int ordreThrust = -1;
        Coords cible = null;
        
        int idOpponent;
        if (opponents.get(0).rang == 1) {
            idOpponent = 0;
        } else {
            idOpponent = 1;
        }
        log("---- mode Attaque ----", idOpponent, opponents.get(idOpponent).nextCheckPointId);

        // Simulation des prochaines coords de l'adversaire
        Coords cibleOpponent = checkpoints.get(opponents.get(idOpponent).nextCheckPointId);
        log("cibleOpponent:", cibleOpponent);

        Simulation simul = simulationCheckpointEtNext(opponents.get(idOpponent),
                cibleOpponent, cibleOpponent,
                0 /* noThrust */, 20 /* profMax */, 500 /* distance */);
        

        log("Adv au checkpoint suiv:", simul);
        
        // Recherche d'une interception sur ces prochaines Coords
        
        for (int i=0; i<simul.ptPassage.size(); i++) {
            Coords p = simul.ptPassage.get(i);

            int thrust = thrustPourAtteindreCibleEnUnTemps(pod, p, i, 700);
            
            logDebug(" > interception:", p, "en:", i, "thrust", thrust);
            if (thrust > -1) {
                log(" +++ choix");
                cible = p;
                ordreThrust = thrust;
                break;
            }
            
        }
        
        if (cible == null) {
            // Peut-on arriver au checkpoint avant l'adversaire ?
            Simulation simulCheckpoint = simulationCheckpointEtNext(pod, cibleOpponent, cibleOpponent,
                    0 /* noThrust */, simul.tempsCheckpoint /* profMax */,
                    500 /* distance */);

            // Si oui, on garde cette cible sinon on prend la suivante
            
            int objectifTemps = 15; 
            if (simulCheckpoint.tempsCheckpoint < PAS_ATTEINT) {
                cible = checkpoints.get(opponents.get(idOpponent).nextCheckPointId);
                objectifTemps = simul.tempsCheckpoint;
            } else {
                cible = getCheckpointFuture(opponents.get(idOpponent).nextCheckPointId);
            }
            log("Aller sur checkpoint:", simulCheckpoint, "cible", cible);
            
            Coords checkpointPartenaire = checkpoints.get(pods.get(1-pod.id).nextCheckPointId);
            log("Checkpoint du partenaire:", checkpointPartenaire);
            if (cible.equals(checkpointPartenaire)) {
                cible = new Coords(8000, 4500);
                objectifTemps = 15;
            }
            
            // Peut-on arriver sur la cible sans thrust ?
            
            Simulation simulSansThrust = simulationCheckpointEtNext(pod, cible, cible,
                    objectifTemps /* noThrust */, objectifTemps /* profMax */,
                    500 /* distance */);
            log("On peut l'atteindre sans thrust ?", simulSansThrust);
            if (simulSansThrust.tempsCheckpoint < PAS_ATTEINT) {
                log("On peut l'atteindre sans thrust:", cible);
                ordreThrust = 0;
            } else {
                ordreThrust = 100;
            }
            
        }
        
        pod.ordre.thrust = ordreThrust;
        pod.ordre.cible = cible;
        
    }

    private Coords getCheckpointFuture(int id) {
        if (id < checkpoints.size() - 1) {
            return checkpoints.get(id+1);
        } else {
            return checkpoints.get(0);
        }
    }

    public Simulation simulationCheckpointEtNext(Unit unit,
            Coords checkpoint, Coords cible, int noThrust, int profMax, int distance) {
        
        Simulation ret = new Simulation();
        
        int cptNoThrust = noThrust;
        Coords position = unit;
        Coords workVecteurVit = unit.v;
        AngleEnDegres workAngle = new AngleEnDegres(unit.angle);
        
        int i = 0;
        while (i <= profMax) {
            
            int thrust = 100;
            if (cptNoThrust > 0) thrust = 0;
            cptNoThrust--;
                
            double angleRadian = workAngle.toRadian();
            
            double x = workVecteurVit.x * 0.85 + Math.cos(angleRadian) * thrust;
            double y = workVecteurVit.y * 0.85 + Math.sin(angleRadian) * thrust;

            workVecteurVit = new Coords(x, y);
            position = workVecteurVit.doVtranslation(position);
            
            logDebug("workVecteurVit: ", workVecteurVit);
            logDebug("---- round:", i, "thrust", thrust, "position: ", position);
            
            if (checkpoint.compareDistance(position, distance) < 0)
                ret.tempsCheckpoint = Math.min(ret.tempsCheckpoint, i);
            
            if (cible.compareDistance(position, distance) < 0)
                ret.tempsCible = Math.min(ret.tempsCible, i);

            // ---- Calcul nouvel angle du pod
            
            AngleEnDegres angleCible = position.createVecteurVers(cible).getVAngleDegres();
            AngleEnDegres ecart = workAngle.ecart(angleCible);

            logDebug("workAngle: ", workAngle, "angleCible: ", angleCible, "ecart: ", ecart);
            
            if (ecart.angle < -18) ecart = new AngleEnDegres(-18);
            if (ecart.angle > 18) ecart = new AngleEnDegres(18);
            
            workAngle = workAngle.retire(ecart);
            logDebug("workAngle: ", workAngle);
            
            Unit newUnit = new Unit();
            newUnit.x = position.x;
            newUnit.y = position.y;
            newUnit.v = workVecteurVit;
            newUnit.angle = workAngle.getIntAngle();
            ret.ptPassage.add(newUnit);

            i++;
        }
        ret.noThrust = noThrust;
        return ret;
    }
    
    int thrustPourAtteindreCibleEnUnTemps(Unit unit, Coords cible, int temps, int distance) {

        int noThrust = 0; // Test de zéro thrust successif 
        
        while (noThrust < temps) {
            
            Simulation simul = simulationCheckpointEtNext(unit, cible, cible, noThrust, temps, distance);
            logDebug("> simul pour thrust:", noThrust, "simul", simul);
            
            if (simul.tempsCheckpoint == temps) {
                return noThrust == 0 ? 100 : 0;
            }

            noThrust++;
        }
        return -1;
    }
    
    class Pod extends Unit {
        Coords nextCheckpoint = new Coords();
        double nextCheckpointDist; // distance to the next checkpoint
        int nextCheckpointAngle; // angle between your pod orientation and the direction of the next checkpoint

        Coords futureCheckpoint = null;
        AngleEnDegres angleFutureCheckpoint = null;
        
        double vitesse = Double.NaN;
        double vitesseRelative = Double.NaN;

        Unit opponent = new Unit();
        double distOpponentAuCheckpoint = Double.NaN; 
        double distOpponentAuPod; 

        double newThrust = Double.NaN;
        Ordre ordre = new Ordre();
        Mode mode;
    }

    class Unit extends Coords {
        int id;
        Coords v = new Coords(); // speed of your pod
        int angle; // angle of your pod
        int nextCheckPointId; // next check point id of your pod
        int rang = 0;
    }
    
    class Simulation {
        int tempsCible = Player.PAS_ATTEINT;
        int tempsCheckpoint = Player.PAS_ATTEINT;
        int noThrust = 0;
        LinkedList<Unit> ptPassage = new LinkedList<Unit>();

        @Override
        public String toString() {
            return "[tpsCible:" + tempsCible + ", tpsCP:" + tempsCheckpoint + "]";
        }
    }
    
    class Ordre {
        Coords cible = null;
        boolean boost = false;
        boolean shield = false;
        int thrust;
        
        String toCommand() {
            return ""+cible.getIntX()+" "+cible.getIntY()+" "+(shield ? "SHIELD" : (boost?"BOOST":thrust));
        }
    }
    /* Codingame common */
    
    static void log(Object... objects) {
        if (doLog) {
            for (Object o : objects) {
                System.err.print(o.toString() + " ");
            }
            System.err.println();
        }
    }
    
    static void activateLog() {
        doLog = true;
    }
    
    static void logDebug(Object... objects) {
        if (doLogDebug) {
            System.err.print("*");
            for (Object o : objects) {
                System.err.print(o.toString() + " ");
            }
            System.err.println();
        }
    }
    
    static void activateLogDebug() {
        doLogDebug = true;
    }
    
    public static void main(String args[]) {
        Player player = new Player();
        player.joue(System.in);
    }

}


class Coords {
    /* static */ int MAX_X = 17_630;
    /* static */ int MAX_Y = 9_000;

    double x;
    double y;

    public Coords() {}

    public Coords(double x, double y) {
        super();
        this.x = x;
        this.y = y;
    }

    double distance(Coords c2) {
        double dist = Math.hypot(x - c2.x, y - c2.y);
        return dist;
    }

    double distance2(Coords c2) {
        double distX = x - c2.x;
        double distY = y - c2.y;
        return distX * distX + distY * distY;
    }

    public int compareDistance(Coords cible, double val) {
        double dist2 = distance2(cible);
        double val2 = val * val;
        if (dist2 == val2) return 0;
        return dist2 < val2 ? -1 : 1;
    }
    
    public int compareDistance(Coords c1, Coords c2) {
        double dist1 = distance2(c1);
        double dist2 = distance2(c2);
        if (dist1 == dist2) return 0;
        return dist1 < dist2 ? -1 : 1;
    }

    boolean dansLaCarte() {
        boolean ret = x >= 0 && x <= MAX_X && y >= 0 && y <= MAX_Y;
        return ret;
    }
    
    /**
     * Retourne un point au dela du point2.
     * TODO : ajout d'un controle si on sort de la carte ou pas
     */

    Coords getAuDelaDe(Coords point2, double distance) {
        Coords vector2d = createVecteurVers(point2);
        return vector2d.doVtranslation(point2, distance);
    }

    /**
     * Retourne un point opposé au point2.
     */
    
    Coords getPointOppose(Coords point2, double distance) {
        return point2.getAuDelaDe(this, distance);
    }
    
    /**
     * Retourne un point vers le point2 à une certaine distance.
     */
    
    Coords getPointVers(Coords point2, double distance) {
        return this.doVtranslation(createVecteurVers(point2, distance));
    }
    
    /**
     * Calcul un point entre 2 points ou en dehors dans une direction ou une autre.
     * 
     * @param 2ème point du segment
     * @param p Si entre 0 et le point est entre les bornes du segment.
     *          si 0.5, alors le point est au millieu.
     *          si 2 alors le point est à une fois la longueur du segment dans la direction de c2 opposé à this..
     *          si -2 alors le point est à une fois la longueur du segment dans la direction de this opposé à c2.
     * @return
     */
     Coords pointSurSegment(Coords c2, double p) {
        return new Coords (this.x + (c2.x - this.x) * p, this.y + (c2.y - this.y) * p);
    }
     
    boolean equals(Coords c2) {
        return x == c2.x && y == c2.y; 
    }

    int getIntX() {
        return (int) x;
    }
    
    int getIntY() {
        return (int) y;
    }
    
    @Override
    public String toString() {
        return String.format("Coords [x=%.2f, y=%.2f]", x, y);
    }
    
    // ---- Fonctions vectorielles (on considère ici que l'objet Coords est un vecteur)

    static Coords createVFromFormeTrigono(double angle, double norme) {
        return new Coords((int) Math.floor(Math.cos(angle) * norme), (int) Math.floor(Math.sin(angle) * norme));
    }
    
    /**
     * Retourne le vecteur qui fait la translation vers le point2.
     */
            
    Coords createVecteurVers(Coords to) {
        return new Coords(to.x - x, to.y - y);
    }
    
    /**
     * Retourne le vecteur qui fait la translation vers le point2 à une certaine distance.
     */
    
    Coords createVecteurVers(Coords point2, double distance) {
        Coords vectorVersPoint2 = createVecteurVers(point2);
        return new Coords().createVFromFormeTrigono(vectorVersPoint2.getVAngle(), distance);
    }
    
    Coords createVecteurAPartirDe(Coords from) {
        return new Coords(x - from.x, y - from.y);
    }

    double getVNorme() {
        return Math.hypot(x, y);
    }

    double getVAngle() {
        return Math.atan2(y, x);
    }
    
    AngleEnDegres getVAngleDegres() {
        return new AngleEnDegres((360 * getVAngle()) / (2 * Math.PI));
    }
    
    Coords doVtranslation(Coords from) {
        return new Coords(from.x + x, from.y + y);
    }

    Coords doVtranslation(Coords from, double distance) {
        double angle = getVAngle();
        return new Coords(from.x + Math.cos(angle) * distance, from.y + Math.sin(angle) * distance);
    }
    
    Coords ajouteVecteur(Coords v2) {
        return new Coords(this.x + v2.x, this.y + v2.y);
    }
    
    Coords soustraitVecteur(Coords v2) {
        return new Coords(this.x - v2.x, this.y - v2.y);
    }
    
    /**
     * Calcule l'angle entre les vecteurs l'un vers A, l'autre vers B.<br>
     * Le résultat est positif si le segment vers B est dans le sens horaire vis-à-vis de celui
     * vers A.
     * 
     */
    
    AngleEnDegres angleVecteurs(Coords A, Coords B) {
        Coords vA = createVecteurVers(A);
        Coords vB = createVecteurVers(B);
        return new AngleEnDegres().ofRadian(vB.getVAngle()-vA.getVAngle()); 
    }
    
    Coords rotation(double angleAjout) {
        return new Coords().createVFromFormeTrigono(getVAngle()+angleAjout, getVNorme());
    }
   
    public String showVector() {
        return String.format("Coords [x=%.2f, y=%.2f, angle=%.2f(%s°), norme=%.2f]", x, y, getVAngle(), getVAngleDegres(), getVNorme());
    }
    
    /* static */ Comparator<Coords> duPlusProcheAuPlusLoin(Coords base) {
        return new DistanceToBaseComparator(base, 1);
    }

    /* static */ Comparator<Coords> duPlusLoinAuPlusProche(Coords base) {
        return new DistanceToBaseComparator(base, -1);
    }

} 

class AngleEnDegres {

    public double angle;

    public AngleEnDegres(double angle) {
        this.angle = angle;
        normalize();
    }

    public AngleEnDegres() {
        this.angle = 0;
    }

    /* Constructeur a partir de radian */
    /* static */ public AngleEnDegres ofRadian(double radian) {
        double temp = (radian / (2 * Math.PI)) * 360; 
        return new AngleEnDegres(temp);
    }
    
    public double toRadian() {
        return (angle / 360) * (2 * Math.PI); 
    }

    int getIntAngle() {
        return new Double(angle).intValue();
    }
    
    
    @Override
    public AngleEnDegres clone() {
        return new AngleEnDegres(angle);
    }
    
    private void normalize() {
        if (angle < -180) angle = 360 + angle;
        if (angle > 180) angle = angle - 360;
    }

    public AngleEnDegres ajoute(double angle2) {
        angle += angle2;
        normalize();
        return this;
    }

    public AngleEnDegres ajoute(AngleEnDegres angle2) {
        return ajoute(angle2.angle);
    }

    public AngleEnDegres retire(double angle2) {
        angle -= angle2;
        normalize();
        return this;
    }

    public AngleEnDegres retire(AngleEnDegres angle2) {
        return retire(angle2.angle);
    }

    public AngleEnDegres ecart(AngleEnDegres cible) {
        AngleEnDegres ret = new AngleEnDegres(angle - cible.angle);
        ret.normalize();
        return ret;
    }

    public boolean estProcheDe(double angle2, double delta) {
        AngleEnDegres temp = new AngleEnDegres(angle - angle2);
        return delta > temp.angle && temp.angle > -delta;
    }

    @Override
    public String toString() {
        return String.format("%.2f", angle);
    }
    
}

class DistanceToBaseComparator implements Comparator<Coords> {
    int ASC = 1;
    int DESC = -1;
    
    Coords base;
    int order;
       
    public DistanceToBaseComparator(Coords base, int order) {
        super();
        this.base = base;
        this.order = order;
    }

    @Override
    public int compare(Coords arg0, Coords arg1) {
        return arg0.distance(base) < arg1.distance(base) ? -1 * order : 1 * order;
    }

}