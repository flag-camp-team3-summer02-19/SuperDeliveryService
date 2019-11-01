package SuperDelivery.service.idm.models;

import SuperDelivery.service.idm.constants.DeliveryServiceInfo;

public enum Warehouse {
    WAREHOUSE1 ("1", DeliveryServiceInfo.WAREHOUSE1),
    WAREHOUSE2 ("2", DeliveryServiceInfo.WAREHOUSE2),
    WAREHOUSE3 ("3", DeliveryServiceInfo.WAREHOUSE3);

    private final String dbName;
    private final LocationLatLon location;

    Warehouse(String dbName, LocationLatLon location) {
        this.dbName = dbName;
        this.location = location;
    }

    public String getDbName() {
        return dbName;
    }

    public LocationLatLon getLocation() {
        return location;
    }

    public static Warehouse getInstance(String dbName) {
        switch (dbName) {
            case "1":
                return WAREHOUSE1;
            case "2":
                return WAREHOUSE2;
            case "3":
                return WAREHOUSE3;
            default:
                return null;
        }
    }
}
