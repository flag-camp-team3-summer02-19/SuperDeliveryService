package SuperDelivery.service.idm.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MakeNewDeliveryResponseModel {
    private int resultCode;
    private String message;
    private DeliveryMethods methods;

    public MakeNewDeliveryResponseModel() {

    }

    @JsonCreator
    public MakeNewDeliveryResponseModel(@JsonProperty(value = "resultCode", required = true) int resultCode,
                                        @JsonProperty(value = "message", required = true) String message,
                                        @JsonProperty(value = "methods", required = true) DeliveryMethods methods) {
        this.resultCode = resultCode;
        this.message = message;
        this.methods = methods;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DeliveryMethods getMethods() {
        return methods;
    }

    public void setMethods(DeliveryMethods methods) {
        this.methods = methods;
    }
}
