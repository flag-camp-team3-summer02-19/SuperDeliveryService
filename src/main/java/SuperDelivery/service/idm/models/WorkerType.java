package SuperDelivery.service.idm.models;

public enum WorkerType {
    ROBOT ("ROBOT"),
    DRONE ("DRONE");

    private final String dbName;

    WorkerType(String dbName) {
        this.dbName = dbName;
    }

    public String getDbName() {
        return dbName;
    }
}
