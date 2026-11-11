package ro.ubb.map.retea.ui;

import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.exceptions.EntityNotFoundException;
import ro.ubb.map.retea.exceptions.RepositoryException;
import ro.ubb.map.retea.exceptions.ServiceException;
import ro.ubb.map.retea.exceptions.ValidationException;
import ro.ubb.map.retea.repository.CriteriiUtilizator;
import ro.ubb.map.retea.service.ComunitateSociabila;
import ro.ubb.map.retea.service.PrietenieService;
import ro.ubb.map.retea.service.ReteaService;
import ro.ubb.map.retea.service.UtilizatorService;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Console interface: numbered menu, input reading and checking, results display.
 */
public class ConsoleUI implements UI {
    private UtilizatorService utilizatorService;
    private PrietenieService prietenieService;
    private ReteaService reteaService;
    private Scanner scanner;

    /**
     * Creates the interface.
     *
     * @param utilizatorService the users service
     * @param prietenieService  the friendships service
     * @param reteaService      the network analysis service
     * @param scanner           the input source
     */
    public ConsoleUI(UtilizatorService utilizatorService, PrietenieService prietenieService,
                     ReteaService reteaService, Scanner scanner) {
        this.utilizatorService = utilizatorService;
        this.prietenieService = prietenieService;
        this.reteaService = reteaService;
        this.scanner = scanner;
    }

    /**
     * {@inheritDoc}
     * Shows the loading warnings first, then runs the menu loop. The application exceptions are
     * caught here and shown as messages, without a stack trace. The menu loop is the only loop left
     * in the application: it repeats until the user exits and does not traverse a collection.
     */
    @Override
    public void run() {
        afiseazaAvertismente();
        boolean mergeMai = true;
        while (mergeMai) {
            afiseazaMeniu();
            if (!scanner.hasNextLine()) {
                break;
            }
            String optiune = scanner.nextLine().trim();
            try {
                mergeMai = executa(optiune);
            } catch (ValidationException | ServiceException | EntityNotFoundException | RepositoryException e) {
                System.out.println("Eroare: " + e.getMessage());
            } catch (UnsupportedOperationException e) {
                System.out.println("Operatie indisponibila: " + e.getMessage());
            }
        }
    }

    private void afiseazaAvertismente() {
        try {
            List<String> avertismente = reteaService.getAvertismenteIncarcare();
            if (!avertismente.isEmpty()) {
                System.out.println("Avertismente la incarcarea datelor (" + avertismente.size() + "):");
                avertismente.stream().map(avertisment -> "  ! " + avertisment).forEach(System.out::println);
            }
        } catch (RepositoryException e) {
            System.out.println("Eroare la incarcarea datelor: " + e.getMessage());
        } catch (UnsupportedOperationException e) {
            System.out.println("Operatie indisponibila: " + e.getMessage());
        }
    }

    private void afiseazaMeniu() {
        System.out.println();
        System.out.println("1. Adauga utilizator");
        System.out.println("2. Sterge utilizator");
        System.out.println("3. Adauga prietenie");
        System.out.println("4. Sterge prietenie");
        System.out.println("5. Afiseaza utilizatorii (cu prietenii lor)");
        System.out.println("6. Numar de comunitati");
        System.out.println("7. Cea mai sociabila comunitate");
        System.out.println("8. Cauta utilizatori");
        System.out.println("0. Iesire");
        System.out.print("> ");
    }

    private boolean executa(String optiune) {
        switch (optiune) {
            case "1":
                Utilizator u = utilizatorService.adaugaUtilizator(citeste("Prenume"), citeste("Nume"));
                System.out.println("Adaugat: " + u);
                break;
            case "2":
                System.out.println("Sters: " + utilizatorService.stergeUtilizator(citesteId("Id")));
                break;
            case "3":
                System.out.println("Adaugata: " + prietenieService.adaugaPrietenie(citesteId("Id 1"), citesteId("Id 2")));
                break;
            case "4":
                System.out.println("Stearsa: " + prietenieService.stergePrietenie(citesteId("Id 1"), citesteId("Id 2")));
                break;
            case "5":
                afiseazaUtilizatori();
                break;
            case "6":
                System.out.println("Numar comunitati: " + reteaService.numarComunitati());
                break;
            case "7":
                afiseazaCeaMaiSociabila();
                break;
            case "8":
                cautaUtilizatori();
                break;
            case "0":
                return false;
            default:
                System.out.println("Optiune invalida.");
        }
        return true;
    }

    private void afiseazaUtilizatori() {
        utilizatorService.getUtilizatori().forEach(this::afiseazaUtilizator);
    }

    private void afiseazaUtilizator(Utilizator x) {
        System.out.println(x + " | prieteni: " + x.getPrieteni().size());
        x.getPrieteni().stream().map(prieten -> "    - " + prieten).forEach(System.out::println);
    }

    private void afiseazaCeaMaiSociabila() {
        reteaService.ceaMaiSociabilaComunitate().ifPresentOrElse(this::afiseazaComunitate,
                () -> System.out.println("Nu exista utilizatori."));
    }

    private void afiseazaComunitate(ComunitateSociabila c) {
        System.out.println("Membri:");
        c.getMembri().stream().map(m -> "  " + m).forEach(System.out::println);
        System.out.println("Lungime drum (muchii): " + c.getLungimeDrum());
        String drum = c.getDrum().stream()
                .map(m -> m.getId() + " (" + m.getPrenume() + " " + m.getNume() + ")")
                .collect(Collectors.joining(" => "));
        System.out.println("Drum: " + drum);
    }

    /**
     * Search: each filled-in answer adds a criterion; the criteria are combined with {@code and}.
     * Several prefixes of the last name, separated by comma, are combined with {@code or}; "only users with
     * friends" is the {@code negate} of "no friends". The results are sorted by last name, then first name
     * (optionally reversed).
     */
    private void cautaUtilizatori() {
        Predicate<Utilizator> numeCriteriu = citesteOptional("Nume incepe cu (prefixe separate prin virgula)")
                .map(CriteriiUtilizator::numeIncepeCuUnulDin)
                .orElse(utilizator -> true);
        Predicate<Utilizator> prenumeCriteriu = citesteOptional("Prenume contine")
                .map(CriteriiUtilizator::prenumeContine)
                .orElse(utilizator -> true);
        Predicate<Utilizator> minimCriteriu = citesteOptional("Numar minim de prieteni")
                .map(this::parseazaNumar)
                .map(CriteriiUtilizator::areMinimPrieteni)
                .orElse(utilizator -> true);
        Predicate<Utilizator> criteriu = numeCriteriu.and(prenumeCriteriu).and(minimCriteriu);
        if (citesteOptional("Doar utilizatori fara prieteni? (da/nu)").filter("da"::equalsIgnoreCase).isPresent()) {
            criteriu = criteriu.and(CriteriiUtilizator.faraPrieteni());
        } else if (citesteOptional("Doar utilizatori cu prieteni? (da/nu)").filter("da"::equalsIgnoreCase).isPresent()) {
            criteriu = criteriu.and(CriteriiUtilizator.faraPrieteni().negate());
        }
        Comparator<Utilizator> ordine = Comparator.comparing(Utilizator::getNume)
                .thenComparing(Utilizator::getPrenume);
        if (citesteOptional("Ordine descrescatoare dupa nume? (da/nu)").filter("da"::equalsIgnoreCase).isPresent()) {
            ordine = ordine.reversed();
        }
        List<Utilizator> gasiti = utilizatorService.cautaUtilizatori(criteriu).stream()
                .sorted(ordine)
                .collect(Collectors.toList());
        System.out.println("Utilizatori gasiti: " + gasiti.size());
        gasiti.stream().map(u -> "  " + u + " | prieteni: " + u.getPrieteni().size()).forEach(System.out::println);
    }

    /** Reads a line; a blank answer means "no criterion" and gives an empty Optional. */
    private Optional<String> citesteOptional(String eticheta) {
        return Optional.of(citeste(eticheta)).filter(Predicate.not(String::isEmpty));
    }

    private int parseazaNumar(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new ValidationException("'" + text + "' nu este un numar valid.");
        }
    }

    private String citeste(String eticheta) {
        System.out.print(eticheta + ": ");
        return scanner.hasNextLine() ? scanner.nextLine().trim() : "";
    }

    private Long citesteId(String eticheta) {
        String text = citeste(eticheta);
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            throw new ValidationException("'" + text + "' nu este un id numeric valid.");
        }
    }
}
