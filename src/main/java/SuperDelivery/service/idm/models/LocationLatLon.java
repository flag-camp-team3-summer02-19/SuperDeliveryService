package SuperDelivery.service.idm.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;

@JsonPropertyOrder({"lat", "lon"})
public class LocationLatLon {
    @JsonProperty(value = "lat")
    private BigDecimal lat;
    @JsonProperty(value = "lon")
    private BigDecimal lon;

    private LocationLatLon(LocationLatLonBuilder builder) {
        this.lat = builder.lat;
        this.lon = builder.lon;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public BigDecimal getLon() {
        return lon;
    }

    public static class LocationLatLonBuilder {
        private BigDecimal lat;
        private BigDecimal lon;

        public LocationLatLonBuilder setLat(BigDecimal lat) {
            this.lat = lat;
            return this;
        }

        public LocationLatLonBuilder setLon(BigDecimal lon) {
            this.lon = lon;
            return this;
        }

        public LocationLatLon build() {
            return new LocationLatLon(this);
        }
    }
}
