package SuperDelivery.service.idm.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.sql.Timestamp;

@JsonPropertyOrder({"deliveryType", "deliveryTime", "deliveryStatus", "cost"})
public class DeliveryInfo {
    @JsonProperty(value = "deliveryType")
    private int deliveryType;
    @JsonProperty(value = "deliveryTime")
    private Timestamp deliveryTime;
    @JsonProperty(value = "deliveryStatus")
    private int deliveryStatus;
    @JsonProperty(value = "cost")
    private float cost;
    @JsonIgnore
    private int workerID;

    @JsonCreator
    public DeliveryInfo(@JsonProperty(value = "deliveryType", required = true) int deliveryType,
                        @JsonProperty(value = "deliveryTime", required = true) Timestamp deliveryTime,
                        @JsonProperty(value = "cost", required = true) float cost) {
        this.deliveryType = deliveryType;
        this.deliveryTime =deliveryTime;
        this.cost =cost;
    }

    private DeliveryInfo(DeliveryInfoBuilder builder) {
        this.deliveryType = builder.deliveryType;
        this.deliveryTime = builder.deliveryTime;
        this.deliveryStatus = builder.deliveryStatus;
        this.cost = builder.cost;
        this.workerID = builder.workerID;
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

    public int getWorkerID() {
        return workerID;
    }

    public static class DeliveryInfoBuilder {
        private int deliveryType;
        private Timestamp deliveryTime;
        private int deliveryStatus;
        private float cost;
        private int workerID;

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

        public DeliveryInfoBuilder setWorkerID(int workerID) {
            this.workerID = workerID;
            return this;
        }

        public DeliveryInfo build() {
            return new DeliveryInfo(this);
        }
    }
}
