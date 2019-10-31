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
}
