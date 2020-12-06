package tmdn;

import org.openqa.selenium.TimeoutException;
import selenium.Driver;

import java.util.List;
import java.util.logging.Logger;

public class TmdnRestDetailHarvester {

    private Logger log = Logger.getLogger(this.getClass().getSimpleName());
    private TmdPersistence tmdPersistence = new TmdPersistence();
    private int numberProcessed = 0;

    public void run() {

        while (true) {
            log.info("restarting harvesting");
            harvest();
        }

    }

    private void harvest() {
        try (Driver driver = new Driver("","")) {
            driver.get("https://www.tmdn.org/tmdsview-web/welcome");
            driver.waitFor("/html/body");

            List<TmdRow> incomplete = tmdPersistence.getIncompleteRows();
            for (TmdRow r : incomplete) {
                log.info(String.format("processing item %d, %s", numberProcessed + 1, r.st13));

                String xml = getXml(driver, r.urlOwnerDetails);
                ScrapedAddress owner = extract(xml, r);
                r.setOwnerCountryCode(owner.countryCode);
                r.setOwnerAddress(owner.address);

                xml = getXml(driver, r.urlRepresentativeDetails);
                ScrapedAddress rep = extract(xml, r);
                r.setRepresentativeCountryCode(rep.countryCode);
                r.setRepresentativeAddress(rep.address);

                tmdPersistence.updateAddressData(r);
                numberProcessed++;
            }

        } catch (TimeoutException e) {
        }
    }

    private ScrapedAddress extract(String source, TmdRow r) {
        int from = getEndIndexOpeningTag(source, "<FreeFormatAddressLine>");
        int to = source.indexOf("</FreeFormatAddressLine>");
        String address = "";
        if (to >= 0)
            address = source.substring(from, to);

        from = getEndIndexOpeningTag(source, "<AddressCountryCode>");
        to = source.indexOf("</AddressCountryCode>");
        String countryCode = "";
        if (to >= 0)
            countryCode = source.substring(from, to);
        return new ScrapedAddress(countryCode, address);
    }

    private int getEndIndexOpeningTag(String xml, String tag) {
        return xml.indexOf(tag) + tag.length();
    }

    private String getXml(Driver driver, String url) {

        driver.get(url);
        String xmlResult = "";

        while (true) {
            xmlResult = driver.getPageSource();
            if (!xmlResult.contains("<Transaction")) {
                continue;
            }
            break;

        }
        int from = xmlResult.indexOf("<Transaction");
        int to = xmlResult.indexOf("<!-- Source file");

        return xmlResult.substring(from, to);
    }


    public static void main(String[] args) {
        new TmdnRestDetailHarvester().run();
    }

}
