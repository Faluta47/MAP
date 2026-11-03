package ro.ubb.map.retea.ui;

/**
 * User interface of the application. Implementations depend only on the service interfaces.
 */
public interface UI {

    /**
     * Runs the interface until the user exits or the input ends.
     * Application errors are shown to the user and do not stop the program.
     */
    void run();
}
