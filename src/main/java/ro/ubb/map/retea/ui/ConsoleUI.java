package ro.ubb.map.retea.ui;

import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.exceptions.EntityNotFoundException;
import ro.ubb.map.retea.exceptions.RepositoryException;
import ro.ubb.map.retea.exceptions.ServiceException;
import ro.ubb.map.retea.exceptions.ValidationException;
import ro.ubb.map.retea.service.ComunitateSociabila;
import ro.ubb.map.retea.service.PrietenieService;
import ro.ubb.map.retea.service.ReteaService;
import ro.ubb.map.retea.service.UtilizatorService;

import java.util.List;
import java.util.Scanner;

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
     * caught here and shown as messages, without a stack trace.
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
                for (String avertisment : avertismente) {
                    System.out.println("  ! " + avertisment);
                }
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
            case "0":
                return false;
            default:
                System.out.println("Optiune invalida.");
        }
        return true;
    }

    private void afiseazaUtilizatori() {
        for (Utilizator x : utilizatorService.getUtilizatori()) {
            System.out.println(x + " | prieteni: " + x.getPrieteni().size());
            for (Utilizator prieten : x.getPrieteni()) {
                System.out.println("    - " + prieten);
            }
        }
    }

    private void afiseazaCeaMaiSociabila() {
        ComunitateSociabila c = reteaService.ceaMaiSociabilaComunitate();
        if (c == null) {
            System.out.println("Nu exista utilizatori.");
            return;
        }
        System.out.println("Membri:");
        for (Utilizator m : c.getMembri()) {
            System.out.println("  " + m);
        }
        System.out.println("Lungime drum (muchii): " + c.getLungimeDrum());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < c.getDrum().size(); i++) {
            Utilizator m = c.getDrum().get(i);
            if (i > 0) {
                sb.append(" => ");
            }
            sb.append(m.getId()).append(" (").append(m.getPrenume()).append(' ').append(m.getNume()).append(')');
        }
        System.out.println("Drum: " + sb);
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
