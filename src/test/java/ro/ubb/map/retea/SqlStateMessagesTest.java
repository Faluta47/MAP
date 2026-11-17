package ro.ubb.map.retea;

import org.junit.jupiter.api.Test;
import ro.ubb.map.retea.repository.db.SqlStateMessages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** SQLState to user message mapping; no database needed. */
class SqlStateMessagesTest {

    @Test
    void stariDeConexiuneDauMesajulDeBazaIndisponibila() {
        String m = SqlStateMessages.toMessage("08001", "Connection refused");
        assertTrue(m.contains("Baza de date nu este disponibila"));
        assertTrue(m.contains("db.url"));
        assertEquals(m, SqlStateMessages.toMessage("08006", "x"));
    }

    @Test
    void autentificareaEsuataSpuneUserSauParolaFaraSaAfisezeParola() {
        String m = SqlStateMessages.toMessage("28P01", "password authentication failed for user \"u\"");
        assertTrue(m.contains("user sau parola gresita"));
        assertFalse(m.contains("password authentication"));
        assertEquals(m, SqlStateMessages.toMessage("28000", null));
    }

    @Test
    void cheieStrainaDaUtilizatorInexistent() {
        assertTrue(SqlStateMessages.toMessage("23503", "x").contains("Utilizator inexistent"));
    }

    @Test
    void cheieDuplicataDaPrietenieExistenta() {
        assertTrue(SqlStateMessages.toMessage("23505", "x").contains("Prietenia exista deja"));
    }

    @Test
    void checkIncalcatSpuneIdMaiMic() {
        assertTrue(SqlStateMessages.toMessage("23514", "x").contains("id1 < id2"));
    }

    @Test
    void stariNecunoscuteDauMesajGenericCuMotivul() {
        String m = SqlStateMessages.toMessage("42P01", "relation \"users\" does not exist");
        assertTrue(m.contains("42P01"));
        assertTrue(m.contains("relation \"users\" does not exist"));
        assertNotEquals(SqlStateMessages.toMessage("23505", "x"), m);
    }

    @Test
    void stareNullNuAruncaExceptie() {
        assertTrue(SqlStateMessages.toMessage(null, null).startsWith("Eroare la accesarea bazei de date"));
    }
}
