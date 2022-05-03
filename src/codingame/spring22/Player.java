package codingame.spring22;

import java.util.*;
import java.util.stream.Collectors;
import java.io.*;

/**
 * Pour le futur :
 * - Faire un Système de création de chemin de ronde
 * - Faire des Prédicates procheDe et auDelaDe
 * - Pb algo du projeté orthogonal : (cas limitte) : vérifier
 *   que le monstre ne sort pas de la carte avant d'entrer dans le puits de
 *   gravité.
 * 
 * Idées : - Ne pas stocker des List de Pion mais des Stream directement comme
 * ça une liste des défenseurs c'est uniquement le Stream heros auquel on ajoute
 * une filter pour faire un nouveau Stream
 * 
 **/
class Player {
    static boolean doLog = false;
    static final int MAX_X = 17_630;
    static final int MAX_Y = 9_000;

    static final int STRATEGIE_DEF = 1;
    static final int STRATEGIE_ATT_1 = 10;
    static final int STRATEGIE_ATT_2 = 15;

    Coords base = new Coords();
    int heroesPerPlayer;
    Coords baseAdv = new Coords();
    int sens;

    int[] health = new int[2];
    int[] mana = new int[2];

    // Contexte stratégique

    Coords[] defPositions = null;
    Coords position1Buteur;
    int[] strategies;

    List<Coords> ronde = new ArrayList<>();

    int round = 0;

    int advEnvoieControls = 0;

    int forcedStrategie = 0;

    public void joue(InputStream inputStream) {

        Scanner in = new Scanner(inputStream);
        base.x = in.nextInt(); // The corner of the map representing your base
        base.y = in.nextInt();
        heroesPerPlayer = in.nextInt(); // Always 3

        init();
        System.err.format("%s %s %s%n", base.x, base.y, heroesPerPlayer);

        // game loop
        while (true) {
            for (int i = 0; i < 2; i++) {
                health[i] = in.nextInt(); // Each player's base health
                mana[i] = in.nextInt(); // Ignore in the first league; Spend ten mana to cast a spell
                System.err.format("%s %s%n", health[i], mana[i]);
            }
            List<Pion> heros = new ArrayList<>();
            List<Pion> advs = new ArrayList<>();
            Map<Integer, Pion> mstrs = new HashMap<>();

            int entityCount = in.nextInt(); // Amount of heros and monsters you can see
            System.err.format("%s%n", entityCount);

            for (int i = 0; i < entityCount; i++) {
                Pion pion = new Pion();
                pion.id = in.nextInt(); // Unique identifier
                pion.type = in.nextInt(); // 0=monster, 1=your hero, 2=opponent hero
                pion.x = in.nextInt(); // Position of this entity
                pion.y = in.nextInt();
                pion.shieldLife = in.nextInt(); // Ignore for this league; Count down until shield spell fades
                pion.isControlled = in.nextInt(); // Ignore for this league; Equals 1 when this entity is under a
                                                  // control spell
                pion.health = in.nextInt(); // Remaining health of this monster
                pion.vx = in.nextInt(); // Trajectory of this monster
                pion.vy = in.nextInt();
                pion.nearBase = in.nextInt(); // 0=monster with no target yet, 1=monster targeting a base
                pion.threatFor = in.nextInt(); // Given this monster's trajectory, is it a threat to 1=your base, 2=your
                                               // opponent's base, 0=neither

                if (pion.type == 1) {
                    heros.add(pion);
                } else if (pion.type == 2) {
                    advs.add(pion);
                } else {
                    mstrs.put(pion.id, pion);
                }
                System.err.println(pion.dump());
            }

            strategie(heros, advs, mstrs);

            heros.forEach(h -> {
                if (h.action == 1) {
                    if (h.x == h.targetx && h.y == h.targety) {
                        System.out.println("WAIT");
                    } else {
                        System.out.println("MOVE " + h.targetx + " " + h.targety + " " + h.comment);
                    }
                } else if (h.action == 2) {
                    System.out.println("SPELL WIND " + h.targetx + " " + h.targety + " " + h.comment);
                } else if (h.action == 3) {
                    System.out.println(
                            "SPELL CONTROL " + h.targetId + " " + h.targetx + " " + h.targety + " " + h.comment);
                } else if (h.action == 4) {
                    System.out.println("SPELL SHIELD " + h.targetId + " " + h.comment);
                }
            });
        }
    }

    private void init() {
        if (base.x == 0) {
            sens = 1;
            baseAdv = new Coords(MAX_X, MAX_Y);
        } else {
            sens = -1;
            baseAdv = new Coords(0, 0);
        }

        calculateDefPositions();

        strategies = new int[heroesPerPlayer];
        for (int i = 0; i < strategies.length; i++)
            strategies[i] = STRATEGIE_DEF;

    }

    private void strategie(List<Pion> heros, List<Pion> advs, Map<Integer, Pion> mstrs) {
        evolutionStrategie(heros, advs, mstrs);
        List<Pion> defenseurs = heros.stream().filter(h -> h.tactiq == STRATEGIE_DEF).collect(Collectors.toList());

        tactiqueAutoShield(defenseurs, advs);
        tactiqueDefensive(defenseurs, mstrs);
        tactiqueAntiButeur(defenseurs, advs, mstrs);
        tactiquePreDefensive(defenseurs, mstrs);

        tactiqueButeur(heros, advs, mstrs);
        tactiqueRecolteWildMana(heros, advs, mstrs);
        tactiqueSurveillance(heros);
    }

    private void evolutionStrategie(List<Pion> heros, List<Pion> advs, Map<Integer, Pion> mstrs) {
        advEnvoieControls += heros.stream().filter(p -> p.isControlled == 1).count();

        round++;

        if ((mana[0] > 100 && strategies[0] == STRATEGIE_DEF) || forcedStrategie == STRATEGIE_ATT_1) {
            strategies[0] = STRATEGIE_ATT_1;
        }

        for (int i = 0; i < strategies.length; i++)
            heros.get(i).tactiq = strategies[i];

        log("advEnvoieControls:", advEnvoieControls, ", strategies:", Arrays.toString(strategies));
    }

    /**
     * Stratégie de défense de la base
     */
    private void tactiqueDefensive(List<Pion> heros, Map<Integer, Pion> mstrs) {
        log("---- tactiqueDefensive ----");

        double DISTANCE_SPELL_WIND = 5_000D;

        // Recherche attaques directes
        List<Pion> attackers = mstrs.values().stream().filter(m -> !m.affected)
                .filter(m -> m.nearBase == 1 && m.threatFor == 1)
                .sorted((p1, p2) -> p1.distance(base) < p2.distance(base) ? -1 : 1).collect(Collectors.toList());

        log(" > Base attackers: " + attackers.size());
        attackers.forEach(Player::log);

        if (attackers.size() == 0)
            return;

        boolean critique = attackers.stream().anyMatch(m -> m.distance(base) < DISTANCE_SPELL_WIND);
        log(" > critique: " + critique + ", mana: " + mana);

        if (critique && mana[0] >= 10) {
            if (spellWindDefensif(heros, attackers.get(0), mstrs)) {
                log("spellWindDefensif fait");
//    			return;
            }
        }

        // Pour chaque attaquant, on cherche le heros le plus proche
        affectHerosSurMonstres(heros, attackers, 2);
    }

    private boolean spellWindDefensif(List<Pion> heros, Pion mstr, Map<Integer, Pion> mstrs) {
        log("---- spellWindDefensif ---- " + mstr);
        if (mstr.shieldLife > 0) {
            log("No spell possible. shieldLife: " + mstr.shieldLife);
            return false;
        }
        // Recherche du heros le plus proche du monstre à au moins 1280

        Optional<Pion> hSelected = heros.stream().filter(h -> !h.affected && h.isControlled != 1)
                .reduce((h1, h2) -> h1.distance(mstr) < h2.distance(mstr) ? h1 : h2);

        // Si ok alors lancer le spell wind
        if (hSelected.isPresent()) {
            Pion h = hSelected.get();
            if (h.distance(mstr) <= 1_280D) {
                log(" > hero: " + h.id);
                h.affected = true;
                h.action = 2;

                Coords target = h.getPointOppose(base, 2_000D);
                h.targetx = target.x;
                h.targety = target.y;
                h.comment = "W " + mstr.id;
                mana[0] -= 10;

                // Marquer affected les monstres inclus dans le spell wind
                mstrs.values().stream().filter(m -> m.distance(h) < 1280).forEach(m -> {
                    m.affected = true;
                    log("Affected par spell wind:", m);
                });

                return true;
            } else {
                // Sinon se déplacer vers le monstre le plus proche
                affectHeroSurMonstre(h, mstr, null);
                return true;
            }
        }

        return false;
    }

    private void tactiqueAntiButeur(List<Pion> heros, List<Pion> advs, Map<Integer, Pion> mstrs) {
        log("---- tactiqueAntiButeur ----");
        if (mana[0] < 20 || mana[1] < 10)
            return;

        double DISTANCE_TO_BASE = 7_000D;
        double MIN_DISTANCE_POUR_SPELL = 1_280D;

        // Recherche d'un buteur
        Optional<Pion> butteurAdv = advs.stream().filter(p -> p.shieldLife == 0)
                .filter(p -> p.distance(base) < DISTANCE_TO_BASE)
                .reduce((p1, p2) -> p1.distance(base) < p2.distance(base) ? p1 : p2);

        if (butteurAdv.isPresent()) {
            Pion adv = butteurAdv.get();

            List<Pion> mstrsProchesAdv = mstrs.values().stream().filter(m -> m.distance(adv) < 2_200D)
                    .collect(Collectors.toList());
            log("countMstrsProches ADV:", mstrsProchesAdv.size());
            mstrsProchesAdv.forEach(Player::log);

            if (mstrsProchesAdv.size() > 0) {
                // Recherche d'un hero proche et dispo
                heros.stream().filter(h -> !h.affected && h.isControlled != 1)
                        .filter(h -> !h.affected && h.isControlled != 1).filter(h -> {
                            log("Hero", h);
                            List<Pion> mstrsProchesHero = mstrs.values().stream().filter(m -> m.distance(h) < 1_280D)
                                    .collect(Collectors.toList());
                            log("countMstrsProches Hero:", mstrsProchesHero.size());
                            mstrsProchesHero.forEach(Player::log);
                            return mstrsProchesHero.size() > 0;
                        })

                        .reduce((h1, h2) -> h1.distance(adv) < h2.distance(adv) ? h1 : h2).ifPresent(h -> {
                            log("select h:", h, ", h.distance(adv): ", h.distance(adv));
                            if (h.distance(adv) < MIN_DISTANCE_POUR_SPELL && mana[0] >= 10) {
                                log(" > hero: " + h.id);
                                h.affected = true;
                                h.action = 2;
                                h.targetId = adv.id;
                                h.targetx = baseAdv.x;
                                h.targety = baseAdv.y;
                                h.comment = "Go home " + Math.floor(h.distance(adv));
                                mana[0] -= 10;
                            } else {
                                affectHeroSurMonstre(h, adv, null);
                            }
                        });
            }
        }
    }

    private void tactiqueAutoShield(List<Pion> heros, List<Pion> advs) {
        log("---- tactiqueAutoShield ----");
        if (mana[0] < 10 || advEnvoieControls == 0) {
            log("return. advEnvoieControls:", advEnvoieControls);
            return;
        }

        int DISTANCE_TO_BASE = 8_000;

        // Recherche d'un buteur
        Optional<Pion> buteurAdvPresent = advs.stream().filter(p -> p.distance(base) < DISTANCE_TO_BASE)
                .reduce((p1, p2) -> p1);

        if (buteurAdvPresent.isPresent()) {
            Pion buteurAdv = buteurAdvPresent.get();
            log("Buteur ADV:", buteurAdv);
            // Recherche d'un hero concerné
            heros.stream().filter(h -> !h.affected && h.isControlled != 1).filter(h -> h.shieldLife == 0).forEach(h -> {
                if (mana[0] >= 10) {
                    log(" > auto Shield: " + h.id);
                    h.affected = true;
                    h.action = 4;
                    h.targetId = h.id;
                    h.comment = "Shield";
                    mana[0] -= 10;
                }
            });
        } else
            log("no Buteur ADV");
    }

    /**
     * Tactique d'attaque préventive des monstres
     */
    private void tactiquePreDefensive(List<Pion> heros, Map<Integer, Pion> mstrs) {
        log("---- tactiquePreDefensive ----");

        long freeHeros = heros.stream().filter(m -> !m.affected).count();
        if (freeHeros == 0) {
            log(" > no free heros");
            return;
        }

        // Recherche des monstres qui pourraient intercepter la base
        List<Pion> attackers = mstrs.values().stream().filter(m -> !m.affected).peek(Player::log)
                .filter(m -> m.willFallIntoBase(base))
                .sorted((p1, p2) -> p1.distance(base) < p2.distance(base) ? -1 : 1).collect(Collectors.toList());

        log(" > Distant Base attackers: " + attackers.size());
        attackers.forEach(m -> log(m.toString()));

        // Affectation heros sur la cilble la plus proche
        affectHerosSurMonstres(heros, attackers, 1);
    }

    private void tactiqueButeur(List<Pion> heros, List<Pion> advs, Map<Integer, Pion> mstrs) {
        log("---- tactiqueButeur ----");

        Optional<Pion> optButeur = heros.stream().filter(h -> h.tactiq == STRATEGIE_ATT_1).findAny();

        if (!optButeur.isPresent()) {
            log("Pas de buteur");
            return;
        }

        Pion buteur = optButeur.get();
        boolean DO_PROTECTION = true;
        int NB_MIN_MONSTRE_POUR_PROTECTION = 2;

        // Params WIND
        int NB_MIN_MONSTRE_POUR_WIND = 1;
        double DISTANCE_BASE_ADV_POUR_WIND = 7_000D;

        if (mana[0] > 30 && DO_PROTECTION) {
            // Recherche de monstres à protéger dans la base adv
            List<Pion> mProches = mstrs.values().stream().filter(m -> m.distance(buteur) < 2_200D)
                    .filter(m -> m.shieldLife == 0).filter(m -> m.distance(baseAdv) < 5_000D)
                    // .filter(m -> m.willFallIntoBase(baseAdv))
                    .filter(m -> m.health > 5).sorted((p1, p2) -> p1.health > p2.health ? -1 : 1)
                    .collect(Collectors.toList());
            log("Monstres a proteger:", mProches.size());
            mProches.forEach(Player::log);

            if (mProches.size() > NB_MIN_MONSTRE_POUR_PROTECTION) {
                Pion m = mProches.get(0);
                buteur.comment = "Shield";
                buteur.targetId = m.id;
                buteur.action = 4;
                buteur.affected = true;
                m.affected = true;
                return;
            }
        }

        // Envoi de monstres dans la base adv avec un spell wind
        if (mana[0] >= 10 && NB_MIN_MONSTRE_POUR_WIND > 0 && buteur.distance(baseAdv) < DISTANCE_BASE_ADV_POUR_WIND) {
            // Compter le nb de monstres que l'on peut envoyer par un spell wind
            List<Pion> mProches = mstrs.values().stream().filter(m -> m.distance(buteur) < 1_280D)
                    .filter(m -> m.shieldLife == 0) // Sinon les monstres ne sont pas impactés
                    // .filter(m -> m.willFallIntoBase(baseAdv))
                    // .filter(m -> m.health > 5)
                    .collect(Collectors.toList());
            log("Monstres proches pour wind:", mProches.size());
            mProches.forEach(Player::log);

            if (mProches.size() >= NB_MIN_MONSTRE_POUR_WIND) {
                Pion m = mProches.get(0);
                buteur.comment = "Take that";
                buteur.affected = true;
                buteur.action = 2;
                buteur.targetx = baseAdv.x;
                buteur.targety = baseAdv.y;
                buteur.affected = true;
                m.affected = true;
                return;
            }
        } else {
            log("Pas de wing possible :", buteur.distance(baseAdv));
        }

        if (mana[0] > 50) {
            // Recherche de monstres à envoyer sur la base adv
            List<Pion> mProches = mstrs.values().stream().filter(m -> !m.affected)
                    .filter(m -> mana[0] >= 200 || (m.id % 3) == 0).filter(m -> m.distance(buteur) < 2_200D)
                    .filter(m -> m.shieldLife == 0).filter(m -> m.health > 5).filter(m -> !m.willFallIntoBase(baseAdv))
                    .collect(Collectors.toList());
            log("Monstres a envoyer vers la base adv:", mProches.size());
            if (mProches.size() > 0) {
                Pion m = mProches.get(0);
                buteur.comment = "GO";
                buteur.targetId = m.id;
                buteur.targetx = baseAdv.x;
                buteur.targety = baseAdv.y;
                buteur.affected = true;
                buteur.action = 3;
                buteur.affected = true;
                m.affected = true;
                return;
            }
        }

    }

    /**
     * Stratégie d'attaque des monstres non menaçant
     */
    private void tactiqueRecolteWildMana(List<Pion> heros, List<Pion> advs, Map<Integer, Pion> mstrs) {
        log("---- strategieRecolteWildMana ----");

        double PC_MAX_DISTANCE = 0.5D;
        double MAX_DISTANCE_DU_MONSTRE = 2_000D;

        // Recherche des adversaires trop proches
        double maxDistance = 99_999D; // diagonale *PC_MAX_DISTANCE;
        /*
         * Optional<Pion> adv = advs.stream() .reduce((p1, p2) -> p1.distance(base) <
         * p2.distance(base) ? p1 : p2); if (adv.isPresent()) maxDistance =
         * adv.get().distance(base) + 3_000D;
         * 
         * log("maxDistance: "+ maxDistance); Double max = new Double(maxDistance);
         */
        List<Pion> freeHeros = heros.stream().filter(m -> !m.affected)
                .filter(m -> m.tactiq == STRATEGIE_DEF || mana[0] < 30)
                // .filter(m -> m.distance(base) < maxDistance)
                .collect(Collectors.toList());
        for (Pion h : freeHeros) {
            log("h: " + h);

            // Recherche des monstres prochent
            Optional<Pion> mProches = mstrs.values().stream().peek(Player::log).filter(m -> !m.affected)
                    .filter(m -> !m.willFallIntoBase(baseAdv)).filter(m -> m.distance(h) < MAX_DISTANCE_DU_MONSTRE)
                    .filter(m -> m.distance(base) < maxDistance).reduce((m1, m2) -> {
                        if (h.getInterception(m1).round < h.getInterception(m2).round)
                            return m1;
                        else
                            return (m1.distance(base) < m2.distance(base) ? m1 : m2);
                    });

            // Affectation heros sur la cilble la plus proche
            if (mProches.isPresent()) {
                affectHeroSurMonstre(h, mProches.get(), mstrs.values());
            }
        }

    }

    private void affectHerosSurMonstres(List<Pion> heros, List<Pion> attackers, int nbDef) {

        for (int n = 0; n < nbDef; n++) {
            attackers.stream().filter(m -> !m.affected).forEach(m -> {
                log(" >> Attacker: " + m);

                Optional<Pion> heroSelected = heros.stream().peek(Player::log).filter(h -> !h.affected)
                        .filter(h -> h.isControlled != 1)
                        .reduce((h1, h2) -> h1.getInterception(m).round < h2.getInterception(m).round ? h1 : h2);

                if (heroSelected.isPresent()) {
                    affectHeroSurMonstre(heroSelected.get(), m, attackers);
                    if (m.shieldLife > 0)
                        m.affected = false;
                }
            });
        }
    }

    private void affectHeroSurMonstre(Pion h, Pion m, Collection<Pion> mstrs) {
        log(" >>>> affected: " + h.id + " to " + m.id);

        boolean INTERCEPTION = true;

        h.comment = "Kill " + m.id;

        boolean doAttaqueDirecte = true;
        if (INTERCEPTION) {
            Interception intercept = new Interception(h, m);
            log(" >>>> Interception: ", intercept);
            if (intercept.round > 0 && intercept.round < 99) {
                h.targetx = intercept.x;
                h.targety = intercept.y;
                h.comment += " (" + intercept.round + ")";
                doAttaqueDirecte = false;
            }
        }
        if (doAttaqueDirecte) {
            Coords decall = base;
            if (mstrs != null) {
                // Recherche d'un autre monstre proche pour se décaller vers lui
                List<Pion> mProches = mstrs
                        .stream().filter(p -> p.id != m.id).sorted((p1, p2) -> p1.nextCoords()
                                .distance(m.nextCoords()) < p2.nextCoords().distance(m.nextCoords()) ? -1 : 1)
                        .collect(Collectors.toList());
                if (mProches.size() > 0) {
                    log("mProches:", mProches.size());
                    mProches.forEach(Player::log);
                    decall = mProches.get(0).nextCoords();
                }
            }
            log("decall:", decall);
            Coords target = m.nextCoords().getPointVers(decall, 390);
            h.targetx = target.x;
            h.targety = target.y;
        }
        h.affected = true;
        h.action = 1;
        m.affected = true;
    }

    /**
     * Repositionnement à la position par défaut si non affecté
     */
    private void tactiqueSurveillance(List<Pion> heros) {
        int i = 0;
        for (Pion h : heros) {
            if (!h.affected) {

                switch (h.tactiq) {
                case STRATEGIE_ATT_1: {
                    h.affected = true;
                    h.action = 1;
                    int k = (h.id + round) % ronde.size();
                    Coords point = ronde.get(k);
                    h.targetx = position1Buteur.x + -sens * point.x;
                    h.targety = position1Buteur.y + -sens * point.y;
                    h.comment = "Banzai";
                }
                    break;

                case STRATEGIE_DEF:
                default: {
                    h.affected = true;
                    h.action = 1;
                    int k = (h.id + round) % ronde.size();
                    Coords point = ronde.get(k);
                    h.targetx = defPositions[i].x + sens * point.x;
                    h.targety = defPositions[i].y + sens * point.y;
                    h.comment = "Let's see";
                    break;
                }
                }
            }
            i++;
        }
    }

    /**
     * Détermination des positions par défaut
     */
    private void calculateDefPositions() {

        int DISTANCE1 = 6_500;
        int DISTANCE2 = 8_000;
        int DISTANCE_BUTEUR = 3_000;

        int sens = (base.x == 0 ? 1 : -1);

        defPositions = new Coords[3];

        int x = (int) Math.round(DISTANCE1 * Math.cos(Math.PI / 9));
        int y = (int) Math.round(DISTANCE1 * Math.sin(Math.PI / 9));

        defPositions[2] = new Coords();
        defPositions[2].x = base.x + sens * x;
        defPositions[2].y = base.y + sens * y;

        defPositions[0] = new Coords();
        defPositions[0].x = base.x + sens * y;
        defPositions[0].y = base.y + sens * x;

        x = (int) Math.round(DISTANCE2 * Math.cos(Math.PI / 4));
        y = (int) Math.round(DISTANCE2 * Math.sin(Math.PI / 4));

        defPositions[1] = new Coords();
        defPositions[1].x = base.x + sens * y;
        defPositions[1].y = base.y + sens * x;

        // ----Coords du buteur
        x = (int) Math.round(DISTANCE_BUTEUR * Math.cos(Math.PI / 4));
        y = (int) Math.round(DISTANCE_BUTEUR * Math.sin(Math.PI / 4));
        position1Buteur = new Coords(baseAdv.x - sens * x, baseAdv.y - sens * y);
        log("coordButeur: ", position1Buteur);

        ronde = createCheminDeRonde(10);
    }

    private List<Coords> createCheminDeRonde(int nbPoints) {

        List<Coords> ret = new ArrayList<>();

        if (nbPoints == 8) {
            ret.add(new Coords(0, 0));
            ret.add(new Coords(565 * 1, -565 * 1));
            ret.add(new Coords(565 * 2, -565 * 2));
            ret.add(new Coords(565 * 3, -565 * 1));
            ret.add(new Coords(565 * 4, 0));
            ret.add(new Coords(565 * 3, 565 * 1));
            ret.add(new Coords(565 * 2, 565 * 2));
            ret.add(new Coords(565 * 1, 565 * 1));
        }

        if (nbPoints == 10) {
            ret.add(new Coords(0, 0));
            ret.add(new Coords(565 * 1, -565 * 1));
            ret.add(new Coords(565 * 2, -565 * 2));
            ret.add(new Coords(565 * 2 + 800, -565 * 2));
            ret.add(new Coords(565 * 3 + 800, -565 * 1));
            ret.add(new Coords(565 * 4 + 800, 0));
            ret.add(new Coords(565 * 3 + 800, 565 * 1));
            ret.add(new Coords(565 * 2 + 800, 565 * 2));
            ret.add(new Coords(565 * 2, 565 * 2));
            ret.add(new Coords(565 * 1, 565 * 1));
        }
        return ret;
    }

    // ----------------------------------------------------------------------------

    public class Coords {
        int x;
        int y;

        public Coords() {
        }

        public Coords(int x, int y) {
            super();
            this.x = x;
            this.y = y;
        }

        double distance(Coords c2) {
            double distX = x - c2.x;
            double distY = y - c2.y;
            double dist = Math.sqrt(distX * distX + distY * distY);
            if (this instanceof Pion) {
                ((Pion) this).distanceToBase = dist;
            }
            return dist;
        }

        boolean dansLaCarte() {
            boolean ret = (x >= 0 && x <= MAX_X) || (y >= 0 && y <= MAX_Y);
            return ret;
        }

        Interception getInterception(Pion m) {
            return new Interception(this, m);
        }

        @Override
        public String toString() {
            return "Coords [x=" + x + ", y=" + y + "]";
        }

        /**
         * Pour cette Coords, retourne un point opposé aux Coords base à une certaine
         * distance
         * 
         * @param base     Point opposé à celui qu'on veut calculer
         * @param distance Distance du point que l'on veut à partir d'ici
         */
        Coords getPointOppose(Coords base, double distance) {
            Coords vector2d = createVFromPoints(base, this);
            return vector2d.doVtranslation(this, distance);
        }

        Coords getPointVers(Coords base, double distance) {
            return getPointOppose(base, -distance);
        }

        // ---- Fonctions vectorielles

        double getVNorme() {
            return Math.sqrt(x * x + y * y);
        }

        double getVAngle() {
            return Math.atan2(y, x);
        }

        Coords doVtranslation(Coords from) {
            return new Coords(from.x + x, from.y + y);
        }

        Coords doVtranslation(Coords from, double distance) {
            double angle = getVAngle();
            return new Coords(from.x + (int) Math.floor(Math.cos(angle) * distance),
                    from.y + (int) Math.floor(Math.sin(angle) * distance));
        }

        /* static */ Coords createVFromPoints(Coords from, Coords to) {
            return new Coords(to.x - from.x, to.y - from.y);
        }

        /* static */ Coords createVFromFormeTrigono(double angle, double norme) {
            return new Coords((int) Math.floor(Math.cos(angle) * norme), (int) Math.floor(Math.sin(angle) * norme));
        }

    }

    // ----------------------------------------------------------------------------

    class Interception extends Coords {
        int round;

        Interception(Coords h, Pion m) {
            // log("Interception: "+h);
            // log(" vers "+m);
            x = m.x + m.vx;
            y = m.y + m.vy;
            int maxDistanceVsMonstre = 800;
            round = 0;
            // log("I: x: "+x+", y: "+y+", maxDistanceVsMonstre: "+maxDistanceVsMonstre+",
            // dist: "+h.distance(this));

            while (h.distance(this) > maxDistanceVsMonstre && dansLaCarte() && round < 99) {
                x += m.vx;
                y += m.vy;
                maxDistanceVsMonstre += 800;
                round++;
                // log("I: x: "+x+", y: "+y+", maxDistanceVsMonstre: "+maxDistanceVsMonstre+",
                // dist: "+h.distance(this));
            }
            if (!dansLaCarte())
                round = 99;
        }
    }

    // ----------------------------------------------------------------------------

    public class Pion extends Coords {
        int id;
        int type;
        int shieldLife;
        int isControlled;
        int health;
        int vx;
        int vy;
        int nearBase;
        int threatFor;

        // Common fields
        boolean affected = false;

        // Heroes fields
        int tactiq = STRATEGIE_DEF;
        int action; // 1 = MOVE, 2 = SPELL WIND
        int targetId;
        int targetx;
        int targety;
        String comment = "";

        // Monsters fields
        double distanceToBase;

        public String dump() {
            return id + " " + type + " " + x + " " + y + " " + shieldLife + " " + isControlled + " " + health + " " + vx
                    + " " + vy + " " + nearBase + " " + threatFor;
        }

        boolean seRapprocheDe(Coords cible) {
            boolean ret = (Math.abs(x + vx - cible.x) <= Math.abs(x - cible.x)
                    && Math.abs(y + vy - cible.y) <= Math.abs(y - cible.y));
            log(" >>> se raproche de " + cible + ": " + ret);
            return ret;
        }

        Coords getProjeteOrthogonal(Coords base) {
            // Pb (cas limitte) :
            // vérifier que le monstre ne sort pas de la carte avant d'entrer dans le puits
            // de gravité
            Line thisLine = getLine();
            Line orthogonalLine = thisLine.getLineOrthogonale(base);
            Coords inters = thisLine.getIntersection(orthogonalLine);
            log(" >>> inters: " + inters);
            return inters;
        }

        boolean willFallIntoBase(Coords base) {
            try {
                Coords inters = getProjeteOrthogonal(base);
                double distance = base.distance(inters);
                boolean ret = (distance < 5_050D && seRapprocheDe(inters));
                log(" >>> distance: " + distance + ", will fall: " + ret);
                return ret;
            } catch (Exception e) {
                log("Exception: " + e);
                return true;
            }
        }

        Line getLine() {
            int a = vy;
            int b = -vx;
            int c = -(a * x + b * y);
            return new Line(a, b, c);

        }

        Coords nextCoords() {
            return new Coords(x + vx, y + vy);
        }

        @Override
        public String toString() {
            return "Pion [id=" + id + ", type=" + type + ", x=" + x + ", y=" + y + ", shieldLife=" + shieldLife
                    + ", isControlled=" + isControlled + ", health=" + health + ", vx=" + vx + ", vy=" + vy
                    + ", nearBase=" + nearBase + ", threatFor=" + threatFor + ", distanceToBase=" + distanceToBase
                    + ", affected=" + affected + "]";
        }

    }

    // ----------------------------------------------------------------------------

    public class Line {
        // Sous forme d'équation cartésienne : ax + y + c = 0
        int a;
        int b;
        int c;

        public Line(int a, int b, int c) {
            super();
            this.a = a;
            this.b = b;
            this.c = c;
        }

        Line getLineOrthogonale(Coords point) {
            return new Line(b, -a, -(b * point.x + -a * point.y));
        }

        Coords getIntersection(Line l2) {
            Coords inters = new Coords();
            log("Intersection: " + toString() + " vs " + l2);
            inters.y = (int) Math.round(((double) -l2.c + (l2.a * c) / a) / (((-l2.a * b) / a) + l2.b));
            inters.x = (int) Math.round(((double) -c - (b * inters.y)) / a);
            return inters;
        }

        @Override
        public String toString() {
            return "Line [a=" + a + ", b=" + b + ", c=" + c + "]";
        }

    }

    // ---- Pour le test ----

    public Coords getNewCoords() {
        return new Coords();
    }

    public Pion getNewPion() {
        return new Pion();
    }

    Interception getNewInterception(Coords h, Pion m) {
        return new Interception(h, m);
    }

    static void log(Object... objects) {
        if (doLog) {
            for (Object o : objects) {
                System.err.print(o.toString() + " ");
            }
            System.err.println();
        }
    }

    public void setAdvEnvoieControls(int advEnvoieControls) {
        this.advEnvoieControls = advEnvoieControls;
    }

    public void forceStrategie(int strategie) {
        forcedStrategie = strategie;
    }

    static void activateLog() {
        doLog = true;
    }

    public static void main(String args[]) {
        new Player().joue(System.in);
    }

}