package SuperDelivery.service.idm.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MakeNewDeliveryRequestModel {
    private String email;
    private PackageInfo packageInfo;

    public MakeNewDeliveryRequestModel() {

    }

    @JsonCreator
    public MakeNewDeliveryRequestModel(@JsonProperty(value = "email", required = true) String email,
                                       @JsonProperty(value = "packageInfo", required = true) PackageInfo packageInfo) {
        this.email = email;
        this.packageInfo = packageInfo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }
}
