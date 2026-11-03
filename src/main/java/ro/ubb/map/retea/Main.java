package ro.ubb.map.retea;

import ro.ubb.map.retea.domain.Prietenie;
import ro.ubb.map.retea.domain.Tuple;
import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.exceptions.ConfigException;
import ro.ubb.map.retea.exceptions.ValidationException;
import ro.ubb.map.retea.repository.LoadWarnings;
import ro.ubb.map.retea.repository.Repository;
import ro.ubb.map.retea.repository.factory.RepositoryFactory;
import ro.ubb.map.retea.service.PrietenieService;
import ro.ubb.map.retea.service.PrietenieServiceImpl;
import ro.ubb.map.retea.service.ReteaService;
import ro.ubb.map.retea.service.ReteaServiceImpl;
import ro.ubb.map.retea.service.UtilizatorService;
import ro.ubb.map.retea.service.UtilizatorServiceImpl;
import ro.ubb.map.retea.ui.ConsoleUI;
import ro.ubb.map.retea.ui.UI;
import ro.ubb.map.retea.validators.PrietenieValidator;
import ro.ubb.map.retea.validators.UtilizatorValidator;
import ro.ubb.map.retea.validators.Validator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

/**
 * Entry point: loads the configuration, builds the objects (constructor Dependency Injection)
 * and starts the interface. It works only with the interface types.
 */
public class Main {
    private static final int MAX_LUNGIME_NUME = 30;

    /**
     * Starts the application. A configuration error is shown as a message and ends the program.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        UI ui;
        try {
            ui = construiesteUI(incarcaConfig());
        } catch (ConfigException e) {
            System.out.println("Eroare de configurare: " + e.getMessage());
            return;
        }
        ui.run();
    }

    private static UI construiesteUI(Properties config) {
        LoadWarnings warnings = new LoadWarnings();
        RepositoryFactory factory = new RepositoryFactory(config, warnings);

        // extra rule written as a lambda and chained to the existing validator (no new class)
        Validator<Utilizator> utilizatorValidator = new UtilizatorValidator().andThen(u -> {
            if (u.getNume().trim().length() > MAX_LUNGIME_NUME) {
                throw new ValidationException("Numele poate avea cel mult " + MAX_LUNGIME_NUME + " caractere.");
            }
        });
        Repository<Long, Utilizator> repoUtilizatori = factory.createUtilizatorRepository(utilizatorValidator);
        // the friendship validator checks that the users exist, so it receives the users repository
        Validator<Prietenie> prietenieValidator = new PrietenieValidator(repoUtilizatori);
        Repository<Tuple<Long, Long>, Prietenie> repoPrietenii = factory.createPrietenieRepository(prietenieValidator);

        UtilizatorService utilizatorService = new UtilizatorServiceImpl(repoUtilizatori, repoPrietenii);
        PrietenieService prietenieService = new PrietenieServiceImpl(repoUtilizatori, repoPrietenii);
        ReteaService reteaService = new ReteaServiceImpl(repoUtilizatori, repoPrietenii, warnings);
        return new ConsoleUI(utilizatorService, prietenieService, reteaService, new Scanner(System.in));
    }

    private static Properties incarcaConfig() {
        Properties config = new Properties();
        try (InputStream in = Main.class.getResourceAsStream("/config.properties")) {
            if (in == null) {
                throw new ConfigException("config.properties nu exista in classpath.");
            }
            config.load(in);
        } catch (IOException e) {
            throw new ConfigException("Nu se poate citi config.properties.", e);
        }
        return config;
    }
}
