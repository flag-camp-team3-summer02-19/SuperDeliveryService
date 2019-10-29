package SuperDelivery.service.idm.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.sql.Timestamp;

@JsonPropertyOrder({"deliveryType", "deliveryTime", "cost"})
public class DeliveryInfo {
    @JsonProperty(value = "deliveryType")
    private int deliveryType;
    @JsonProperty(value = "deliveryTime")
    private Timestamp deliveryTime;
    @JsonIgnore
    private int deliveryStatus;
    @JsonProperty(value = "cost")
    private float cost;

    private DeliveryInfo(DeliveryInfoBuilder builder) {
        this.deliveryType = builder.deliveryType;
        this.deliveryTime = builder.deliveryTime;
        this.deliveryStatus = builder.deliveryStatus;
        this.cost = builder.cost;
    }

    public int getDeliveryType() {
        return deliveryType;
    }

    public Timestamp getDeliveryTime() {
        return deliveryTime;
    }

    public int getDeliveryStatus() {
        return deliveryStatus;
    }

    public float getCost() {
        return cost;
    }

    public static class DeliveryInfoBuilder {
        private int deliveryType;
        private Timestamp deliveryTime;
        private int deliveryStatus;
        private float cost;

        public DeliveryInfoBuilder setDeliveryType(int deliveryType) {
            this.deliveryType = deliveryType;
            return this;
        }

        public DeliveryInfoBuilder setDeliveryTime(Timestamp deliveryTime) {
            this.deliveryTime = deliveryTime;
            return this;
        }

        public DeliveryInfoBuilder setDeliveryStatus(int deliveryStatus) {
            this.deliveryStatus = deliveryStatus;
            return this;
        }

        public DeliveryInfoBuilder setCost(float cost) {
            this.cost = cost;
            return this;
        }

        public DeliveryInfo build() {
            return new DeliveryInfo(this);
        }
    }
}
