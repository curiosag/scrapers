package persistence;

import java.util.List;

public interface ActiveRecord<T extends ActiveRecord> {
    public List<T> findAll();
    public void write();
}
