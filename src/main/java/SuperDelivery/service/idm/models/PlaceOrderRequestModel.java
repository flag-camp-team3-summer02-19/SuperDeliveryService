package SuperDelivery.service.idm.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PlaceOrderRequestModel {
    @JsonProperty(value = "paymentResult")
    private String paymentResult;
    @JsonProperty(value = "order")
    private PlaceOrderInfo order;

    public PlaceOrderRequestModel() {

    }

    @JsonCreator
    public PlaceOrderRequestModel(@JsonProperty(value = "paymentResult", required = true) String paymentResult,
                                  @JsonProperty(value = "order", required = true) PlaceOrderInfo order) {
        this.paymentResult = paymentResult;
        this.order = order;
    }

    public String getPaymentResult() {
        return paymentResult;
    }

    public void setPaymentResult(String paymentResult) {
        this.paymentResult = paymentResult;
    }

    public PlaceOrderInfo getOrder() {
        return order;
    }

    public void setOrder(PlaceOrderInfo order) {
        this.order = order;
    }
}
