package tmdn;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class TmdPersistenceTest {

    TmdPersistence p = new TmdPersistence();

    @Test
    public void removeDuplicates() {
    }

    @Test
    public void initializeTmdSlizes() {
        p.initializeTmdSlizes(LocalDate.of(2010, 1, 1), LocalDate.of(2010, 12, 31), 30);
    }
}