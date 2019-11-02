package SuperDelivery.service.idm.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PlaceOrderInfo {
    @JsonProperty(value = "packageInfo")
    private PackageInfo packageInfo;
    @JsonProperty(value = "method")
    private DeliveryInfo deliveryInfo;

    public PlaceOrderInfo() {

    }

    @JsonCreator
    public PlaceOrderInfo(@JsonProperty(value = "packageInfo", required = true) PackageInfo packageInfo,
                          @JsonProperty(value = "method", required = true) DeliveryInfo deliveryInfo) {
        this.packageInfo = packageInfo;
        this.deliveryInfo = deliveryInfo;
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
}
