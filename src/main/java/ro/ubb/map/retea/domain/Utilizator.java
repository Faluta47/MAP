package ro.ubb.map.retea.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * A user of the network. The friends list is filled by the service layer and is not saved in the file.
 */
public class Utilizator extends Entity<Long> {
    private String prenume;
    private String nume;
    private String username;
    private List<Utilizator> prieteni = new ArrayList<Utilizator>();

    /**
     * Creates a user.
     *
     * @param prenume the first name
     * @param nume    the last name
     */
    public Utilizator(String prenume, String nume) {
        this.prenume = prenume;
        this.nume = nume;
    }

    /**
     * Returns the first name.
     *
     * @return the first name
     */
    public String getPrenume() {
        return prenume;
    }

    /**
     * Sets the first name.
     *
     * @param prenume the new first name
     */
    public void setPrenume(String prenume) {
        this.prenume = prenume;
    }

    /**
     * Returns the last name.
     *
     * @return the last name
     */
    public String getNume() {
        return nume;
    }

    /**
     * Sets the last name.
     *
     * @param nume the new last name
     */
    public void setNume(String nume) {
        this.nume = nume;
    }

    /**
     * Returns the username.
     *
     * @return the username, or null if the user has none
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username the new username (null means no username)
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the friends list.
     *
     * @return the (modifiable) list of friends
     */
    public List<Utilizator> getPrieteni() {
        return prieteni;
    }

    /**
     * Replaces the friends list.
     *
     * @param prieteni the new list of friends
     */
    public void setPrieteni(List<Utilizator> prieteni) {
        this.prieteni = prieteni;
    }

    /**
     * Returns a readable form of the user.
     *
     * @return a readable form of the user
     */
    @Override
    public String toString() {
        return "#" + getId() + " " + prenume + " " + nume;
    }
}
