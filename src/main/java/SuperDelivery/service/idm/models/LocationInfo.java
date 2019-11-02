package SuperDelivery.service.idm.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"current", "destination", "worker"})
public class LocationInfo {
    @JsonProperty(value = "current")
    private LocationLatLon current;
    @JsonProperty(value = "to")
    private LocationLatLon destination;
    @JsonProperty(value = "worker")
    private LocationLatLon worker;

    private LocationInfo(LocationInfoBuilder builder) {
        this.current = builder.current;
        this.destination = builder.destination;
        this.worker = builder.worker;
    }

    public LocationLatLon getCurrent() {
        return current;
    }

    public LocationLatLon getDestination() {
        return destination;
    }

    public LocationLatLon getWorker() {
        return worker;
    }

    public static class LocationInfoBuilder {
        private LocationLatLon current;
        private LocationLatLon destination;
        private  LocationLatLon worker;

        public LocationInfoBuilder setCurrent(LocationLatLon current) {
            this.current = current;
            return this;
        }

        public LocationInfoBuilder setDestination(LocationLatLon destination) {
            this.destination = destination;
            return this;
        }

        public LocationInfoBuilder setWorker(LocationLatLon worker) {
            this.worker = worker;
            return this;
        }

        public LocationInfo build() {
            return new LocationInfo(this);
        }
    }
}
