package SuperDelivery.service.idm.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.sql.Timestamp;
import java.util.Map;

@JsonPropertyOrder({"orderId", "pkgFrom", "pkgTo", "pkgNotes", "deliveryType", "deliveryStatus", "orderedTime", "currentLocLatLon"})
public class OrderSummary {
    @JsonProperty(value = "orderId")
    private int orderID;
    @JsonProperty(value = "from")
    private String pkgFrom;
    @JsonProperty(value = "to")
    private String pkgTo;
    @JsonProperty(value = "notes")
    private String pkgNotes;
    @JsonProperty(value = "deliveryType")
    private int deliveryType;
    @JsonProperty(value = "deliveryStatus")
    private int deliveryStatus;
    @JsonProperty(value = "orderedTime")
    private Timestamp orderedTime;
    @JsonProperty(value = "currentLocLatLon")
    private LocationLatLon currentLocLatLon;

    private OrderSummary(OrderSummaryBuilder builder) {
        this.orderID = builder.orderID;
        this.pkgFrom = builder.pkgFrom;
        this.pkgTo = builder.pkgTo;
        this.pkgNotes = builder.pkgNotes;
        this.deliveryType = builder.deliveryType;
        this.deliveryStatus = builder.deliveryStatus;
        this.orderedTime = builder.orderedTime;
        this.currentLocLatLon = builder.currentLocLatLon;
    }

    public int getOrderID() {
        return orderID;
    }

    public String getPkgFrom() {
        return pkgFrom;
    }

    public String getPkgTo() {
        return pkgTo;
    }

    public String getPkgNotes() {
        return pkgNotes;
    }

    public int getDeliveryType() {
        return deliveryType;
    }

    public int getDeliveryStatus() {
        return deliveryStatus;
    }

    public Timestamp getOrderedTime() {
        return orderedTime;
    }

    public LocationLatLon getCurrentLocLatLon() {
        return currentLocLatLon;
    }

    public static class OrderSummaryBuilder {
        private int orderID;
        private String pkgFrom;
        private String pkgTo;
        private String pkgNotes;
        private int deliveryType;
        private int deliveryStatus;
        private Timestamp orderedTime;
        private LocationLatLon currentLocLatLon;

        public OrderSummaryBuilder setOrderID(int orderID) {
            this.orderID = orderID;
            return this;
        }

        public OrderSummaryBuilder setPkgFrom(String pkgFrom) {
            this.pkgFrom = pkgFrom;
            return this;
        }

        public OrderSummaryBuilder setPkgTo(String pkgTo) {
            this.pkgTo = pkgTo;
            return this;
        }

        public OrderSummaryBuilder setPkgNotes(String pkgNotes) {
            this.pkgNotes = pkgNotes;
            return this;
        }

        public OrderSummaryBuilder setDeliveryType(int deliveryType) {
            this.deliveryType = deliveryType;
            return this;
        }

        public OrderSummaryBuilder setDeliveryStatus(int deliveryStatus) {
            this.deliveryStatus = deliveryStatus;
            return this;
        }

        public OrderSummaryBuilder setOrderedTime(Timestamp orderedTime) {
            this.orderedTime = orderedTime;
            return this;
        }

        public OrderSummaryBuilder setCurrentLocLatLon(LocationLatLon currentLocLatLon) {
            this.currentLocLatLon = currentLocLatLon;
            return this;
        }

        public OrderSummary build() {
            return new OrderSummary(this);
        }
    }
}
