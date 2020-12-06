package google.scholar;

import java.sql.Connection;

public class ItemFactory {
    private final Connection connection;

    public ItemFactory(Connection connection) {
        this.connection = connection;
    }

    public ScholarItem createScholaryItem(){
        return new ScholarItem(connection);
    }
}
