package tmdn;

import org.openqa.selenium.TimeoutException;
import persistence.ScrapeSlice;
import selenium.Driver;

import java.util.List;
import java.util.logging.Logger;

public class TmdnRestHarvester {

    private Logger log = Logger.getLogger(this.getClass().getSimpleName());
    private TmdPersistence tmdPersistence = new TmdPersistence();

    public void run() {
        Driver driver = new Driver("","");
        for (ScrapeSlice s : tmdPersistence.getSlicesToDo()) {
            while (true) {
                log.info("restart harvesting slice " + s.id);

                if (harvest(driver, s)) {
                    break;
                } else {
                    driver.close();
                    driver = new Driver("83.149.70.159", "13012");
                }

            }
        }

    }

    private boolean harvest(Driver driver, ScrapeSlice slice) {
        try {
            driver.get("https://www.tmdn.org/tmdsview-web/welcome");
            driver.waitFor("/html/body");

            int page = slice.pagesDone + 1;
            while (true) {
                if (page > 40)
                    throw new IllegalStateException("slice exceeds 40 pages: " + slice.id);

                List<TmdRow> rows = ResultListExtractor.getFrom(getJson(driver, page, slice.dtFrom, slice.dtTo));

                if (rows.size() == 0) {
                    tmdPersistence.updateSlice(slice.id, page, true);
                    log.info("processed slice " + slice.id + ", page " + page);
                    break;
                }

                for (TmdRow r : rows) {
                    r.setPageNumber(page);
                    r.setSliceNumber(slice.id);
                }

                tmdPersistence.persist(rows);
                tmdPersistence.updateSlice(slice.id, page, false);

                log.info("processed slice " + slice.id + ", page " + page);
                page++;
            }
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    private final static String urlQuery = "https://www.tmdn.org/tmdsview-web/search-dsv?_search=false&nd=1559854656536" +
            "&rows=40&page=%d&sidx=ipvalue&sord=asc&q=tp:IN+AND+RegistrationDate:%s..%s&fq=[]" +
            "&pageSize=40&eurolocarnoSearch=&interfacelanguage=en&&providerList=null&expandedOffices=null" +
            "&selectedRowRegNumber=null";

    private String getJson(Driver driver, int page, String dtFrom, String dtTo) {
        String url = String.format(urlQuery, page, dtFrom, dtTo);
        log.info("getting " + url);
        driver.get(url);

        while (!driver.getPageSource().startsWith("<html><head></head><body><pre")) {
        }
        String jsonResult = driver.getPageSource();

        int from = jsonResult.indexOf('{');
        int to = jsonResult.indexOf("</pre>");
        jsonResult = jsonResult.substring(from, to);
        return jsonResult;
    }

    public static void main(String[] args) {
        new TmdnRestHarvester().run();
    }

}
