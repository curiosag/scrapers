package google.maps.extraction;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class DistrictLoaderTest {

    @Test
    public void testScanFor() {
        assertEquals(Optional.of("hi"), KlmXmlConverter.scanFor("<hi>", "lala<hi>hi<ho>"));
        assertEquals(Optional.empty(), KlmXmlConverter.scanFor("<hi>", null));
        assertEquals(Optional.empty(), KlmXmlConverter.scanFor("<hi>", ""));
        assertEquals(Optional.empty(), KlmXmlConverter.scanFor("<hi>", "lala<ho>hi<ho>"));
    }
}