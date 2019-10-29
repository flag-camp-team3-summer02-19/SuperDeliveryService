package SuperDelivery.service.idm.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDetailResponseModel {
    @JsonProperty(value = "status")
    private String status;
    @JsonProperty(value = "packageInfo")
    private PackageInfo packageInfo;
    @JsonProperty(value = "method")
    private DeliveryInfo deliveryInfo;
    @JsonProperty(value = "locationsLatLon")
    private LocationInfo locationInfo;

    public OrderDetailResponseModel() {

    }

    @JsonCreator
    public OrderDetailResponseModel(@JsonProperty(value = "status", required = true) String status,
                                    @JsonProperty(value = "packageInfo", required = true) PackageInfo packageInfo,
                                    @JsonProperty(value = "method", required = true) DeliveryInfo deliveryInfo,
                                    @JsonProperty(value = "locationsLatLon", required = true) LocationInfo locationInfo) {
        this.status = status;
        this.packageInfo = packageInfo;
        this.deliveryInfo = deliveryInfo;
        this.locationInfo = locationInfo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    public DeliveryInfo getDeliveryInfo() {
        return deliveryInfo;
    }

    public void setDeliveryInfo(DeliveryInfo deliveryInfo) {
        this.deliveryInfo = deliveryInfo;
    }

    public LocationInfo getLocationInfo() {
        return locationInfo;
    }

    public void setLocationInfo(LocationInfo locationInfo) {
        this.locationInfo = locationInfo;
    }
}
