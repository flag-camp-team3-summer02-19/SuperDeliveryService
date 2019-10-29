package SuperDelivery.service.idm.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"lat", "lon"})
public class LocationLatLon {
    @JsonProperty(value = "lat")
    private float lat;
    @JsonProperty(value = "lon")
    private float lon;

    private LocationLatLon(LocationLatLonBuilder builder) {
        this.lat = builder.lat;
        this.lon = builder.lon;
    }

    public float getLat() {
        return lat;
    }

    public float getLon() {
        return lon;
    }

    public static class LocationLatLonBuilder {
        private float lat;
        private float lon;

        public LocationLatLonBuilder setLat(float lat) {
            this.lat = lat;
            return this;
        }

        public LocationLatLonBuilder setLon(float lon) {
            this.lon = lon;
            return this;
        }

        public LocationLatLon build() {
            return new LocationLatLon(this);
        }
    }
}
