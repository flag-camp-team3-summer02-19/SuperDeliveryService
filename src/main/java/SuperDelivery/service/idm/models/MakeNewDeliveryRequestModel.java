package SuperDelivery.service.idm.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MakeNewDeliveryRequestModel {
    private PackageInfo packageInfo;

    public MakeNewDeliveryRequestModel() {

    }

    @JsonCreator
    public MakeNewDeliveryRequestModel(@JsonProperty(value = "packageInfo", required = true) PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }
}
