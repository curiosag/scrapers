package tmdn;

import org.junit.Ignore;
import org.junit.Test;
import persistence.ScrapeSlice;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotEquals;

public class PersistenceTest {

    TmdPersistence p = new TmdPersistence();

    @Test
    @Ignore
    public void persist() {
        TmdRow row = new TmdRow();
        row.setIndicationOfProduct("prod")
                .setSt13("R")
                .setDesignNumber("abc")
                .setOwnerName("owner")
                .setRepresentativeName("rep")
                .setDesignOffice("IN")
                .setDesignatedTerritory("IN")
                .setLocarnoClassification("loc")
                .setStatus("status")
                .setExpiryDate("01-01-1900")
                .setPublicationDate("02-01-1900")
                .setRegistrationDate("03-01-1900")
                .setApplicationDate("04-01-1900")
                .setPageNumber(1)
                .setUrlOwnerDetails("radr")
                .setUrlRepresentativeDetails("ll");

        p.persist(Collections.singletonList(row));
    }

    @Test

    public void initializeSlizes(){
        p.initializeTmdSlizes(LocalDate.parse("2014-01-26"), LocalDate.parse("2018-03-31"), 30);
    }

    @Test
    @Ignore
    public void updateSlice() {
        p.updateSlice(1, 0, true);
    }

    @Test
    @Ignore
    public void getSlices() {
        List<ScrapeSlice> slices = p.getSlicesToDo();
        assertNotEquals(0, slices.size());
    }

    @Test
    public void removeDuplicates() {
        p.removeDuplicates();
    }
}