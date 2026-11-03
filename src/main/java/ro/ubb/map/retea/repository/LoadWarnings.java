package ro.ubb.map.retea.repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects the warnings produced while loading persistent data (for example corrupt file lines).
 * The repositories only add messages; whoever wants to show them to the user reads them later,
 * so nothing is printed from the lower layers.
 */
public class LoadWarnings {
    private List<String> messages = new ArrayList<String>();

    /**
     * Adds a warning.
     *
     * @param message the text of the warning
     */
    public void add(String message) {
        messages.add(message);
    }

    /**
     * Returns the warnings collected so far.
     *
     * @return a copy of the list of warnings, in the order they were added
     */
    public List<String> getAll() {
        return new ArrayList<String>(messages);
    }
}
