package SuperDelivery.service.idm.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"pkgLength", "pkgWidth", "pkgHeight", "pkgWeight", "pkgFrom", "pkgTo", "pkgNotes"})
public class PackageInfo {
    @JsonProperty(value = "length")
    private float pkgLength;
    @JsonProperty(value = "width")
    private float pkgWidth;
    @JsonProperty(value = "height")
    private float pkgHeight;
    @JsonProperty(value = "weight")
    private float pkgWeight;
    @JsonProperty(value = "from")
    private String pkgFrom;
    @JsonProperty(value = "to")
    private String pkgTo;
    @JsonProperty(value = "notes")
    private String pkgNotes;

    @JsonCreator
    public PackageInfo(@JsonProperty(value = "length", required = true) float pkgLength,
                       @JsonProperty(value = "width", required = true) float pkgWidth,
                       @JsonProperty(value = "height", required = true) float pkgHeight,
                       @JsonProperty(value = "weight", required = true) float pkgWeight,
                       @JsonProperty(value = "from", required = true) String pkgFrom,
                       @JsonProperty(value = "to", required = true) String pkgTo,
                       @JsonProperty(value = "notes", required = true) String pkgNotes) {
        this.pkgLength = pkgLength;
        this.pkgWidth = pkgWidth;
        this.pkgHeight = pkgHeight;
        this.pkgWeight = pkgWeight;
        this.pkgFrom = pkgFrom;
        this.pkgTo = pkgTo;
        this.pkgNotes = pkgNotes;
    }

    private PackageInfo(PackageInfoBuilder builder) {
        this.pkgLength = builder.pkgLength;
        this.pkgWidth = builder.pkgWidth;
        this.pkgHeight = builder.pkgHeight;
        this.pkgWeight = builder.pkgWeight;
        this.pkgFrom = builder.pkgFrom;
        this.pkgTo = builder.pkgTo;
        this.pkgNotes = builder.pkgNotes;
    }

    public float getPkgLength() {
        return pkgLength;
    }

    public float getPkgWidth() {
        return pkgWidth;
    }

    public float getPkgHeight() {
        return pkgHeight;
    }

    public float getPkgWeight() {
        return pkgWeight;
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

    public static class PackageInfoBuilder {
        private float pkgLength;
        private float pkgWidth;
        private float pkgHeight;
        private float pkgWeight;
        private String pkgFrom;
        private String pkgTo;
        private String pkgNotes;

        public PackageInfoBuilder setPkgLength(float pkgLength) {
            this.pkgLength = pkgLength;
            return this;
        }

        public PackageInfoBuilder setPkgWidth(float pkgWidth) {
            this.pkgWidth = pkgWidth;
            return this;
        }

        public PackageInfoBuilder setPkgHeight(float pkgHeight) {
            this.pkgHeight = pkgHeight;
            return this;
        }

        public PackageInfoBuilder setPkgWeight(float pkgWeight) {
            this.pkgWeight = pkgWeight;
            return this;
        }

        public PackageInfoBuilder setPkgFrom(String pkgFrom) {
            this.pkgFrom = pkgFrom;
            return this;
        }

        public PackageInfoBuilder setPkgTo(String pkgTo) {
            this.pkgTo = pkgTo;
            return this;
        }

        public PackageInfoBuilder setPkgNotes(String pkgNotes) {
            this.pkgNotes = pkgNotes;
            return this;
        }

        public PackageInfo build() {
            return new PackageInfo(this);
        }
    }
}
