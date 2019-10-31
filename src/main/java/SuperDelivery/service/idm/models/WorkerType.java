package SuperDelivery.service.idm.models;

public enum WorkerType {
    ROBOT ("ROBOT", false),
    DRONE ("DRONE", true);

    private final String dbName;
    private final boolean isGeodesic;

    WorkerType(String dbName, boolean isGeodesic) {
        this.dbName = dbName;
        this.isGeodesic = isGeodesic;
    }

    public String getDbName() {
        return dbName;
    }

    public boolean isGeodesic() {
        return isGeodesic;
    }
}
