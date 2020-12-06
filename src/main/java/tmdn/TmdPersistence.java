package tmdn;

import persistence.ScrapeSlice;
import persistence.Slice;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static persistence.Common.createConnection;
import static persistence.Common.prepare;

public class TmdPersistence {

    private final Connection connection = createConnection("jdbc:mariadb://172.17.0.2:3306/tmdn?user=root&password=root", true);
    private final Slice slice = new Slice(connection);

    private static final String insertRowSQL =
            "insert into scrapeRow (" +
                    "indicationOfProduct," +
                    "st13," +
                    "designNumber," +
                    "ownerName," +
                    "representativeName," +
                    "designOffice," +
                    "designatedTerritory," +
                    "locarnoClassification," +
                    "status," +
                    "applicationDate," +
                    "registrationDate," +
                    "publicationDate," +
                    "expiryDate," +
                    "pageNumber," +
                    "sliceNumber," +
                    "urlOwnerDetails," +
                    "urlRepresentativeDetails) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final PreparedStatement insertRow = prepare(connection, insertRowSQL);

    private final PreparedStatement setAddressData = prepare(connection, "update scrapeRow set ownerCountryCode=?, ownerAddress=?, " +
            "representativeCountryCode=?, representativeAddress=? where id=?");

    public void removeDuplicates() {
        PreparedStatement del = prepare(connection, "delete from scrapeRow where id =?");
        String sel = "select id, st13 from scrapeRow order by id";

        HashMap<String, Integer> st13 = new HashMap<>();
        List<Integer> toDelete = new ArrayList<>();

        try {
            ResultSet r = connection.createStatement().executeQuery(sel);

            while (r.next()) {
                Integer id = r.getInt("id");
                String st = r.getString("st13");
                if (!st13.containsKey(st)) {
                    st13.put(st, id);
                } else {
                    toDelete.add(id);
                }
            }

            toDelete.forEach(i -> {
                try {
                    del.setInt(1, i);
                    del.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<TmdRow> getIncompleteRows() {
        String query = "select id, urlOwnerDetails, urlRepresentativeDetails from scrapeRow where ownerCountryCode is null order by id";

        List<TmdRow> result = new ArrayList<>();
        try {
            ResultSet r = connection.createStatement().executeQuery(query);
            while (r.next()) {
                result.add(new TmdRow()
                        .setId(r.getInt("id"))
                        .setUrlOwnerDetails(r.getString("urlOwnerDetails"))
                        .setUrlRepresentativeDetails(r.getString("urlRepresentativeDetails")));

            }
            return result;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateAddressData(TmdRow row) {
        try {
            setAddressData.setString(1, row.ownerCountryCode);
            setAddressData.setString(2, row.ownerAddress.replaceAll("[^\\p{ASCII}]", ""));
            setAddressData.setString(3, row.representativeCountryCode);
            setAddressData.setString(4, row.representativeAddress.replaceAll("[^\\p{ASCII}]", ""));
            setAddressData.setInt(5, row.id);
            setAddressData.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void persist(List<TmdRow> rows) {
        rows.forEach(r -> {
            try {
                insertRow.setString(1, r.indicationOfProduct);
                insertRow.setString(2, r.st13);
                insertRow.setString(3, r.designNumber);
                insertRow.setString(4, r.ownerName.replaceAll("[^\\p{ASCII}]", ""));
                insertRow.setString(5, r.representativeName.replaceAll("[^\\p{ASCII}]", ""));
                insertRow.setString(6, r.designOffice);
                insertRow.setString(7, r.designatedTerritory);
                insertRow.setString(8, r.locarnoClassification);
                insertRow.setString(9, r.status);
                insertRow.setString(10, r.applicationDate);
                insertRow.setString(11, r.registrationDate);
                insertRow.setString(12, r.publicationDate);
                insertRow.setString(13, r.expiryDate);
                insertRow.setInt(14, r.pageNumber);
                insertRow.setInt(15, r.sliceNumber);
                insertRow.setString(16, r.urlOwnerDetails);
                insertRow.setString(17, r.urlRepresentativeDetails);

                insertRow.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public List<ScrapeSlice> getSlicesToDo() {
        return slice.getSlicesToDo();
    }

    public void updateSlice(int id, int page, boolean sliceDone) {
        slice.updateSlice(id, page, sliceDone);
    }

    public void initializeTmdSlizes(LocalDate from, LocalDate to, int i) {
        slice.initializeTmdSlizes(from, to, i);
    }
}
