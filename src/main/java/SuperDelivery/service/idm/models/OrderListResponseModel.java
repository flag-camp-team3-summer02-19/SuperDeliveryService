package SuperDelivery.service.idm.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class OrderListResponseModel {
    private String status;
    private List<OrderSummary> ordersSummary;

    public OrderListResponseModel() {
        ordersSummary = new ArrayList<>();
    }

    @JsonCreator
    public OrderListResponseModel(@JsonProperty(value = "status", required = true) String status,
                                  @JsonProperty(value = "ordersSummary", required = true) List<OrderSummary> ordersSummary) {
        this.status = status;
        this.ordersSummary = ordersSummary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderSummary> getOrdersSummary() {
        return ordersSummary;
    }

    public void addOrdersSummary(OrderSummary order) {
        ordersSummary.add(order);
    }
}
