package tmdn;


import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

public class ResultListExtractorTest {

    @Test
    public void getFrom() {
        try {
            String json = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("rawData/result.json"), StandardCharsets.UTF_8.name());
            List<TmdRow> row = ResultListExtractor.getFrom(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}