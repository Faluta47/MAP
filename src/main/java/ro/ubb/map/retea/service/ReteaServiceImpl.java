package ro.ubb.map.retea.service;

import ro.ubb.map.retea.domain.Entity;
import ro.ubb.map.retea.domain.Prietenie;
import ro.ubb.map.retea.domain.Tuple;
import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.repository.LoadWarnings;
import ro.ubb.map.retea.repository.Repository;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default implementation of {@link ReteaService}: builds the friendship graph (adjacency lists)
 * and runs DFS (communities) and a recursive search (longest path) on it, both written with streams.
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
        return comunitati(utilizatori, construiesteGraf(utilizatori));
    }

    /** {@inheritDoc} */
    @Override
    public Optional<ComunitateSociabila> ceaMaiSociabilaComunitate() {
        Map<Long, Utilizator> utilizatori = incarcaUtilizatori();
        Map<Long, List<Long>> adiacenta = construiesteGraf(utilizatori);
        // max keeps the first element among equals (OpenJDK BinaryOperator.maxBy), so on a tie
        // the first community found wins, as before the refactoring
        return comunitati(utilizatori, adiacenta).stream()
                .map(componenta -> comunitateSociabila(componenta, utilizatori, adiacenta))
                .max(Comparator.comparingInt(ComunitateSociabila::getLungimeDrum));
    }

    private Map<Long, Utilizator> incarcaUtilizatori() {
        return repoUtilizatori.stream()
                .collect(Collectors.toMap(Entity::getId, Function.identity(), (primul, al2lea) -> primul,
                        LinkedHashMap::new));
    }

    /**
     * Builds the adjacency lists; ignores friendships that refer to missing users. Every friendship
     * gives two directed edges, kept in the order of the friendships, so the neighbours of a user are
     * listed in the order the friendships were stored.
     */
    private Map<Long, List<Long>> construiesteGraf(Map<Long, Utilizator> utilizatori) {
        Map<Long, List<Long>> vecini = repoPrietenii.stream()
                .filter(p -> utilizatori.containsKey(p.getIdUtilizator1())
                        && utilizatori.containsKey(p.getIdUtilizator2()))
                .flatMap(p -> Stream.of(Map.entry(p.getIdUtilizator1(), p.getIdUtilizator2()),
                        Map.entry(p.getIdUtilizator2(), p.getIdUtilizator1())))
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue,
                        Collectors.toList())));
        return utilizatori.keySet().stream()
                .collect(Collectors.toMap(Function.identity(), id -> vecini.getOrDefault(id, Collections.emptyList()),
                        (primul, al2lea) -> primul, LinkedHashMap::new));
    }

    /**
     * The connected components, in the order of their first member (in repository order); each one lists its
     * members in DFS pre-order starting from that first member. A DFS from every user gives the set of
     * users reachable from it; the sets of users of the same community are equal, so distinct()
     * keeps only the one found first, that is the one started from the first member.
     */
    private List<List<Utilizator>> comunitati(Map<Long, Utilizator> utilizatori, Map<Long, List<Long>> adiacenta) {
        return utilizatori.keySet().stream()
                .map(id -> dfs(id, adiacenta, new LinkedHashSet<>()))
                .distinct()
                .map(componenta -> componenta.stream().map(utilizatori::get).collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    /**
     * DFS that fills {@code vizitati} (a set local to the caller, in visiting order) with the nodes
     * reachable from {@code nod}. The neighbours are processed through a stream: the filter is evaluated
     * element by element, so it already sees the nodes visited by the previous recursive calls.
     */
    private Set<Long> dfs(Long nod, Map<Long, List<Long>> adiacenta, Set<Long> vizitati) {
        vizitati.add(nod);
        adiacenta.get(nod).stream()
                .filter(vecin -> !vizitati.contains(vecin))
                .forEach(vecin -> dfs(vecin, adiacenta, vizitati));
        return vizitati;
    }

    private ComunitateSociabila comunitateSociabila(List<Utilizator> componenta, Map<Long, Utilizator> utilizatori,
                                                    Map<Long, List<Long>> adiacenta) {
        List<Long> celMaiBun = componenta.stream()
                .map(start -> celMaiLungDrum(Collections.singletonList(start.getId()), adiacenta))
                .max(Comparator.comparingInt(List::size))
                .orElseThrow(() -> new IllegalStateException("O comunitate are cel putin un membru."));
        List<Utilizator> drum = celMaiBun.stream().map(utilizatori::get).collect(Collectors.toList());
        return new ComunitateSociabila(componenta, celMaiBun.size() - 1, drum);
    }

    /**
     * Recursive search of the longest elementary path that starts with {@code drum}: the path itself or
     * the best extension through each neighbour of its last node that is not on it yet. On a tie the first
     * path found in this order is kept. This is the exhaustive search (backtracking) of the previous
     * version, with immutable paths instead of a shared list that is modified and restored.
     */
    private List<Long> celMaiLungDrum(List<Long> drum, Map<Long, List<Long>> adiacenta) {
        Long ultim = drum.get(drum.size() - 1);
        Stream<List<Long>> extinderi = adiacenta.get(ultim).stream()
                .filter(vecin -> !drum.contains(vecin))
                .map(vecin -> celMaiLungDrum(extinde(drum, vecin), adiacenta));
        return Stream.concat(Stream.of(drum), extinderi)
                .max(Comparator.comparingInt(List::size))
                .orElse(drum);
    }

    private static List<Long> extinde(List<Long> drum, Long nod) {
        return Stream.concat(drum.stream(), Stream.of(nod)).collect(Collectors.toList());
    }
}
