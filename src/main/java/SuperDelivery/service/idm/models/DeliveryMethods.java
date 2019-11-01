package SuperDelivery.service.idm.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"fastest", "fastest"})
public class DeliveryMethods {
    @JsonProperty(value = "fastest")
    private DeliveryInfo fastest;
    @JsonProperty(value = "cheapest")
    private DeliveryInfo cheapest;

    private DeliveryMethods(DeliveryMethodsBuilder builder) {
        this.fastest = builder.fastest;
        this.cheapest = builder.cheapest;
    }

    public DeliveryInfo getFastest() {
        return fastest;
    }

    public DeliveryInfo getCheapest() {
        return cheapest;
    }

    public static class DeliveryMethodsBuilder {
        private DeliveryInfo fastest;
        private DeliveryInfo cheapest;

        public DeliveryMethodsBuilder setFastest(DeliveryInfo fastest) {
            this.fastest = fastest;
            return this;
        }

        public DeliveryMethodsBuilder setCheapest(DeliveryInfo cheapest) {
            this.cheapest = cheapest;
            return this;
        }

        public DeliveryMethods build() {
            return new DeliveryMethods(this);
        }
    }
}
