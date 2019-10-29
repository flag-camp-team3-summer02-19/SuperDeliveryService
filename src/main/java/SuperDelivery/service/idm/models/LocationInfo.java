package SuperDelivery.service.idm.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"current", "destination"})
public class LocationInfo {
    @JsonProperty(value = "current")
    private LocationLatLon current;
    @JsonProperty(value = "to")
    private LocationLatLon destination;

    private LocationInfo(LocationInfoBuilder builder) {
        this.current = builder.current;
        this.destination = builder.destination;
    }

    public LocationLatLon getCurrent() {
        return current;
    }

    public LocationLatLon getDestination() {
        return destination;
    }

    public static class LocationInfoBuilder {
        private LocationLatLon current;
        private LocationLatLon destination;

        public LocationInfoBuilder setCurrent(LocationLatLon current) {
            this.current = current;
            return this;
        }

        public LocationInfoBuilder setDestination(LocationLatLon destination) {
            this.destination = destination;
            return this;
        }

        public LocationInfo build() {
            return new LocationInfo(this);
        }
    }
}
