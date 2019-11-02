package SuperDelivery.service.idm.models;

public class TravelInfo {
    private int duration;  // unit: second
    private int distance;  // unit: meter

    private TravelInfo(TravelInfoBuilder builder) {
        this.duration = builder.duration;
        this.distance = builder.distance;
    }

    public int getDuration() {
        return duration;
    }

    public int getDistance() {
        return distance;
    }

    public static class TravelInfoBuilder {
        private int duration;
        private int distance;

        public TravelInfoBuilder setDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public TravelInfoBuilder setDistance(int distance) {
            this.distance = distance;
            return this;
        }

        public TravelInfo build() {
            return new TravelInfo(this);
        }
    }
}
