package ro.ubb.map.retea.service;

import ro.ubb.map.retea.domain.Prietenie;
import ro.ubb.map.retea.domain.Tuple;
import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.repository.LoadWarnings;
import ro.ubb.map.retea.repository.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of {@link ReteaService}: builds the friendship graph (adjacency lists)
 * and runs DFS (communities) and backtracking (longest path) on it.
 */
public class ReteaServiceImpl implements ReteaService {
    private Repository<Long, Utilizator> repoUtilizatori;
    private Repository<Tuple<Long, Long>, Prietenie> repoPrietenii;
    private LoadWarnings warnings;

    /**
     * Creates the service.
     *
     * @param repoUtilizatori the users repository
     * @param repoPrietenii   the friendships repository
     * @param warnings        the collector that receives the warnings produced by the repositories when loading
     */
    public ReteaServiceImpl(Repository<Long, Utilizator> repoUtilizatori,
                            Repository<Tuple<Long, Long>, Prietenie> repoPrietenii,
                            LoadWarnings warnings) {
        this.repoUtilizatori = repoUtilizatori;
        this.repoPrietenii = repoPrietenii;
        this.warnings = warnings;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getAvertismenteIncarcare() {
        // the users are read first: the friendships validator looks them up while loading
        repoUtilizatori.findAll();
        repoPrietenii.findAll();
        return warnings.getAll();
    }

    /** {@inheritDoc} */
    @Override
    public int numarComunitati() {
        return comunitati().size();
    }

    /** {@inheritDoc} */
    @Override
    public List<List<Utilizator>> comunitati() {
        Map<Long, Utilizator> utilizatori = incarcaUtilizatori();
        Map<Long, List<Long>> adiacenta = construiesteGraf(utilizatori);
        Set<Long> vizitati = new HashSet<Long>();
        List<List<Utilizator>> rezultat = new ArrayList<List<Utilizator>>();
        for (Long id : utilizatori.keySet()) {
            if (!vizitati.contains(id)) {
                List<Utilizator> componenta = new ArrayList<Utilizator>();
                dfs(id, adiacenta, utilizatori, vizitati, componenta);
                rezultat.add(componenta);
            }
        }
        return rezultat;
    }

    /** {@inheritDoc} */
    @Override
    public ComunitateSociabila ceaMaiSociabilaComunitate() {
        Map<Long, Utilizator> utilizatori = incarcaUtilizatori();
        Map<Long, List<Long>> adiacenta = construiesteGraf(utilizatori);
        ComunitateSociabila cea = null;
        for (List<Utilizator> componenta : comunitati()) {
            List<Long> celMaiBun = new ArrayList<Long>();
            for (Utilizator start : componenta) {
                List<Long> curent = new ArrayList<Long>();
                Set<Long> pePath = new HashSet<Long>();
                curent.add(start.getId());
                pePath.add(start.getId());
                cautaDrum(start.getId(), adiacenta, pePath, curent, celMaiBun);
            }
            int lungime = celMaiBun.size() - 1;
            if (cea == null || lungime > cea.getLungimeDrum()) {
                List<Utilizator> drum = new ArrayList<Utilizator>();
                for (Long id : celMaiBun) {
                    drum.add(utilizatori.get(id));
                }
                cea = new ComunitateSociabila(componenta, lungime, drum);
            }
        }
        return cea;
    }

    private Map<Long, Utilizator> incarcaUtilizatori() {
        Map<Long, Utilizator> utilizatori = new LinkedHashMap<Long, Utilizator>();
        for (Utilizator u : repoUtilizatori.findAll()) {
            utilizatori.put(u.getId(), u);
        }
        return utilizatori;
    }

    /** Builds the adjacency lists; ignores friendships that refer to missing users. */
    private Map<Long, List<Long>> construiesteGraf(Map<Long, Utilizator> utilizatori) {
        Map<Long, List<Long>> adiacenta = new LinkedHashMap<Long, List<Long>>();
        for (Long id : utilizatori.keySet()) {
            adiacenta.put(id, new ArrayList<Long>());
        }
        for (Prietenie p : repoPrietenii.findAll()) {
            List<Long> vecini1 = adiacenta.get(p.getIdUtilizator1());
            List<Long> vecini2 = adiacenta.get(p.getIdUtilizator2());
            if (vecini1 != null && vecini2 != null) {
                vecini1.add(p.getIdUtilizator2());
                vecini2.add(p.getIdUtilizator1());
            }
        }
        return adiacenta;
    }

    private void dfs(Long nod, Map<Long, List<Long>> adiacenta, Map<Long, Utilizator> utilizatori,
                     Set<Long> vizitati, List<Utilizator> componenta) {
        vizitati.add(nod);
        componenta.add(utilizatori.get(nod));
        for (Long vecin : adiacenta.get(nod)) {
            if (!vizitati.contains(vecin)) {
                dfs(vecin, adiacenta, utilizatori, vizitati, componenta);
            }
        }
    }

    /** Backtracking: extends the current path with each neighbour not yet on it and keeps the longest path found. */
    private void cautaDrum(Long nod, Map<Long, List<Long>> adiacenta, Set<Long> pePath,
                           List<Long> curent, List<Long> celMaiBun) {
        if (curent.size() > celMaiBun.size()) {
            celMaiBun.clear();
            celMaiBun.addAll(curent);
        }
        for (Long vecin : adiacenta.get(nod)) {
            if (!pePath.contains(vecin)) {
                pePath.add(vecin);
                curent.add(vecin);
                cautaDrum(vecin, adiacenta, pePath, curent, celMaiBun);
                curent.remove(curent.size() - 1);
                pePath.remove(vecin);
            }
        }
    }
}
