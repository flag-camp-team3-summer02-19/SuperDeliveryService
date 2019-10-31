package SuperDelivery.service.idm.models;

public enum Warehouse {
    WAREHOUSE1 ("1"),
    WAREHOUSE2 ("2"),
    WAREHOUSE3 ("3");

    private final String dbName;

    Warehouse(String dbName) {
        this.dbName = dbName;
    }

    public String getDbName() {
        return dbName;
    }
}
