package google.scholar;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import selenium.Driver;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class ScholarScraper {
    private final int maxDepth = 1;
    private int currId = 0;
    private Logger log = Logger.getLogger(this.getClass().getSimpleName());

    private Map<String, ScholarItem> scrapedItems = new HashMap<>(); // cid -> ScholarItem

    private final static String searchQuery = "https://scholar.google.de/scholar?start=%d&hl=en&as_sdt=1Ä2C5&as_vis=1&q=Ä2Bsoftware+Ä2Bagile&btnG=";
    private final static String citationQuery = "https://scholar.google.de/scholar?start=%d&hl=en&cites=%s";

    private Driver driver;

    private void run(String... titles) {
        driver = new Driver("83.149.70.159", "13012");
        try {
            List<String> consider = Arrays.asList(titles);
            scrape(title -> consider.indexOf(title) >= 0);
            writeCitations("/home/ssm/Documents/citations.csv");
        } finally {
            driver.close();
        }
    }

    private void writeCitations(String path) {
        File file = new File(path);
        final StringBuffer sb = new StringBuffer();
        sb.append("id;cid;title;year;author;description;idCitations;citations\n");
        scrapedItems.forEach((k, v) -> {
            String citations = v.citing.stream().map(c -> String.valueOf(c.id)).collect(joining(","));
            sb.append(String.format("%d;%s;\"%s\";%s;%s;\"%s\";%s;%s\n", v.id, v.cid, v.title.replaceAll("\"", "'"), v.year, v.authors, v.description.replaceAll("\"", "'"), v.idCitations, citations));
        });
        try {
            FileUtils.writeStringToFile(file, sb.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void scrape(Function<String, Boolean> canProcess) {
        Document doc = getJsoupDoc(String.format(searchQuery, 1).replace('Ä', '%'));

        selectItems(doc).forEach(i -> {
            Optional<ScholarItem> extracted = extract(i, 0);
            extracted.ifPresent(e -> {
                if (canProcess.apply(e.title)) {
                    scrapedItems.put(e.cid, e);
                    log.info(String.format("adding item %s", e.cid));
                }
            });
        });

        scrapeCitations();
    }

    private void scrapeCitations() {
        for (int i = 0; i < maxDepth; i++) {
            final int level = i;
            List<ScholarItem> todo = scrapedItems.values().stream()
                    .filter(c -> c.citing.size() == 0)
                    .collect(toList());
            todo.forEach(c -> scrapeCitations(c, level));
        }
    }

    private static final int ITEMS_PER_CITEPAGE = 10;

    private void scrapeCitations(ScholarItem c, int scrapeLevel) {

        int page = 0;
        boolean hasNextPage = true;
        while (hasNextPage && page < 10) {
            String url = String.format(citationQuery, page * ITEMS_PER_CITEPAGE, c.idCitations);
            Document doc = getJsoupDoc(url);

            final int currentPage = page + 1;
            selectItems(doc).forEach(i -> {
                extract(i, scrapeLevel).ifPresent(e -> {
                    if (scrapedItems.get(e.cid) == null) {
                        currId++;
                        e.id = currId;
                        scrapedItems.put(e.cid, e);
                        log.info(String.format("adding item %s from page %d", e.cid, currentPage));
                    }
                    ScholarItem citingItem = scrapedItems.get(e.cid);
                    c.citing.add(citingItem);
                    log.info(String.format("%s: adding citation %s (%d of %d) from page %d", c.cid, e.cid, c.citing.size(), c.numberCitations, currentPage));
                });

            });
            sleep(10, 10);
            page++;
            hasNextPage = hasNextPage(doc);
        }
    }

    private final Random rand = new Random();

    private void sleep(int mandatory, int random) {
        try {
            Thread.sleep(mandatory * 1000 + rand.nextInt(random)* 1000);
        } catch (
                InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Elements selectItems(Document doc) {
        return doc.select("div .gs_r.gs_or");
    }

    private boolean hasNextPage(Document doc) {
        return doc.getElementsByClass("gs_ico_nav_next").size() > 0;
    }

    private static Pattern yearPattern = Pattern.compile("(.*)-\\s(\\d{4})\\s-.*");

    private Optional<ScholarItem> extract(Element i, int scrapeLevel) {
        if (i.select("gs_ct1").text().equals("[CITATION]")) {
            return Optional.empty();
        }

        ScholarItem result = new ScholarItem();

        result.depth = scrapeLevel;
        result.cid = i.attr("data-cid");
        result.description = i.select(".gs_rs").text();
        result.title = i.select(".gs_rt a").text();

        String author_year_source = i.select(".gs_a").text();
        Matcher matcher = yearPattern.matcher(author_year_source);
        if (matcher.find()) {
            result.authors = matcher.group(1);
            result.year = Integer.valueOf(matcher.group(2));
        }

        List<Element> cites = i.select(".gs_fl a").stream().filter(n -> n.text().contains("Cited by")).collect(toList());
        if (cites.size() != 1) {
            log.info("no citations for item " + result.cid);
        } else {
            Element c = cites.get(0);
            String href = c.attr("href");
            int idxFrom = "/scholar?cites=".length();
            int idxTo = href.indexOf("&");
            result.idCitations = href.substring(idxFrom, idxTo);
            result.numberCitations = Integer.valueOf(c.text().replace("Cited by", "").trim());
        }

        return Optional.of(result);
    }

    public static void main(String[] params) {
        new ScholarScraper().run(
                "Agile software development: principles, patterns, and practices",
                "Manifesto for agile software development",
                "Agile software development: the cooperative game");
    }

    private Document getJsoupDoc(String url) {
        driver.get(url);
        driver.waitFor("/html/body/div/div[11]/div[2]/div[2]/div[2]/div[1]");
        return Jsoup.parse(driver.getPageSource(), url);
    }
}
