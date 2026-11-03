package ro.ubb.map.retea;

import org.junit.jupiter.api.Test;
import ro.ubb.map.retea.domain.Prietenie;
import ro.ubb.map.retea.domain.Tuple;
import ro.ubb.map.retea.domain.Utilizator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainTest {

    @Test
    void tupleEqualsCuNullSiAltTipDaFalse() {
        Tuple<Long, Long> t = new Tuple<Long, Long>(1L, 2L);
        assertFalse(t.equals(null));
        assertFalse(t.equals("(1, 2)"));
        assertFalse(t.equals(new Object()));
    }

    @Test
    void tupleEqualsSiHashCodeCoerente() {
        Tuple<Long, Long> a = new Tuple<Long, Long>(1L, 2L);
        Tuple<Long, Long> b = new Tuple<Long, Long>(1L, 2L);
        assertTrue(a.equals(a));
        assertTrue(a.equals(b));
        assertEquals(a.hashCode(), b.hashCode());
        assertFalse(a.equals(new Tuple<Long, Long>(2L, 1L)));
    }

    @Test
    void tupleAreSetteri() {
        Tuple<Long, Long> t = new Tuple<Long, Long>(1L, 2L);
        t.setFirst(5L);
        t.setSecond(6L);
        assertEquals(5L, t.getFirst().longValue());
        assertEquals(6L, t.getSecond().longValue());
    }

    @Test
    void entityEqualsCuNullSiAltTipDaFalse() {
        Utilizator u = new Utilizator("Ion", "Pop");
        u.setId(1L);
        assertFalse(u.equals(null));
        assertFalse(u.equals("1"));
        assertFalse(u.equals(new Prietenie(1L, 2L)));
    }

    @Test
    void entityEqualsDupaIdSiHashCodeCoerent() {
        Utilizator a = new Utilizator("Ion", "Pop");
        a.setId(1L);
        Utilizator b = new Utilizator("Alt", "Nume");
        b.setId(1L);
        assertTrue(a.equals(b));
        assertEquals(a.hashCode(), b.hashCode());
        b.setId(2L);
        assertFalse(a.equals(b));
    }

    @Test
    void prietenia12EsteNormalizataLa12() {
        Prietenie p = new Prietenie(2L, 1L);
        assertEquals(1L, p.getIdUtilizator1().longValue());
        assertEquals(2L, p.getIdUtilizator2().longValue());
        assertEquals(new Prietenie(1L, 2L).getId(), p.getId());
    }
}
