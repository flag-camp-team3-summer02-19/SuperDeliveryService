package SuperDelivery.service.idm.models;

public class AmountModel {
    private String total;
    private String currency;

    public AmountModel() {

    }

    public AmountModel(String total, String currency) {
        this.total = total;
        this.currency = currency;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "AmountModel{" +
                "total='" + total + '\'' +
                ", currency='" + currency + '\'' +
                '}';
    }
}
