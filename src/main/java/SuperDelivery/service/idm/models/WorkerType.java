package SuperDelivery.service.idm.models;

import static SuperDelivery.service.idm.constants.DeliveryServiceInfo.*;

public enum WorkerType {
    ROBOT ("ROBOT", ROBOT_TYPEID, false, ROBOTSPEED, ROBOTCOST_DIST),
    DRONE ("DRONE", DRONE_TYPEID, true, DRONESPEED, DRONECOST_DIST);

    private final String dbName;
    private final int typeID;
    private final boolean isGeodesic;
    private final double speed;
    private final double costDist;

    WorkerType(String dbName, int typeID, boolean isGeodesic, double speed, double costDist) {
        this.dbName = dbName;
        this.typeID = typeID;
        this.isGeodesic = isGeodesic;
        this.speed = speed;
        this.costDist = costDist;
    }

    public String getDbName() {
        return dbName;
    }

    public int getTypeID() {
        return typeID;
    }

    public boolean isGeodesic() {
        return isGeodesic;
    }

    public double getSpeed() {
        return speed;
    }

    public double getCostDist() {
        return costDist;
    }
}
