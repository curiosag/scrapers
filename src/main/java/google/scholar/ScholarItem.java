package google.scholar;

import persistence.ActiveRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static persistence.Common.prepare;

public class ScholarItem implements ActiveRecord<ScholarItem> {

    public int id;

    public String cid;
    public String title;
    public String description;
    public int year;
    public String authors;
    public int numberCitations;
    public String idCitations;
    public int depth;
    public int pageNumber;

    public List<ScholarItem> citing = new ArrayList<>();

    private static final String insertSQL =
            "insert into googleScholarItem (" +
                    "pageNumber," +
                    "depth," +
                    "cid," +
                    "title," +
                    "decription," +
                    "year," +
                    "authors," +
                    "numberCitations," +
                    "urlCitations) values(?, ?, ?, ?, ?, ?, ?, ?,?)";

    private static final String selectSQL =
            "select id," +
                    "pageNumber," +
                    "depth," +
                    "cid," +
                    "title," +
                    "decription," +
                    "year," +
                    "authors," +
                    "numberCitations," +
                    "urlCitations from googleScholarItem order by id";

    private static Connection connection;

    private static PreparedStatement insert;
    private static PreparedStatement select;

    public ScholarItem() {
    }

    ScholarItem(Connection connection) {
        if (ScholarItem.connection == null) {
            ScholarItem.connection = connection;
            ScholarItem.insert = prepare(connection, insertSQL);
            ScholarItem.select = prepare(connection, selectSQL);
        }
    }

    public void setCiting(List<ScholarItem> citing) {
        this.citing.addAll(citing);
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public ScholarItem setId(int id) {
        this.id = id;
        return this;
    }

    public ScholarItem setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
        return this;
    }

    public ScholarItem setNumberCitations(int numberCitations) {
        this.numberCitations = numberCitations;
        return this;
    }

    public ScholarItem setTitle(String title) {
        this.title = title;
        return this;
    }

    public ScholarItem setCid(String cid) {
        this.cid = cid;
        return this;
    }

    public ScholarItem setYear(int year) {
        this.year = year;
        return this;
    }

    public ScholarItem setAuthors(String authors) {
        this.authors = authors;
        return this;
    }

    public ScholarItem setIdCitations(String idCitations) {
        this.idCitations = idCitations;
        return this;
    }

    public ScholarItem setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        return "ScholarItem{" +
                "id=" + id +
                ", cid='" + cid + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", year=" + year +
                ", authors='" + authors + '\'' +
                ", numberCitations=" + numberCitations +
                ", idCitations='" + idCitations + '\'' +
                ", depth=" + depth +
                ", pageNumber=" + pageNumber +
                ", citing=" + citing +
                '}';
    }

    @Override
    public List<ScholarItem> findAll() {
        final List<ScholarItem> result = new ArrayList<>();

        try {
            ResultSet items = select.executeQuery();
            while (items.next()) {
                ScholarItem i = new ScholarItem(connection);
                i.setId(items.getInt("id"));
                i.setPageNumber(items.getInt("pageNumber"));
                i.setDepth(items.getInt("depth"));
                i.setNumberCitations(items.getInt("numberCitations"));

                i.setCid(items.getString("cid"));
                i.setTitle(items.getString("title"));
                i.setDescription(items.getString("decription"));
                i.setYear(items.getInt("year"));
                i.setAuthors(items.getString("authors"));
                i.setIdCitations(items.getString("urlCitations"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public void write() {
        try {
            insert.setInt(1, pageNumber);
            insert.setInt(2, depth);
            insert.setString(3, cid);
            insert.setString(4, title);
            insert.setString(5, description);
            insert.setInt(6, year);
            insert.setString(7, authors);
            insert.setInt(8, numberCitations);
            insert.setString(9, idCitations);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
