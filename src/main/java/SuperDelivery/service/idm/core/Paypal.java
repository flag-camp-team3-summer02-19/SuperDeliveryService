package SuperDelivery.service.idm.core;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import SuperDelivery.service.idm.IDMService;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

public class Paypal {

    private static String clientId = "Aa8UEQc8HY-ZMnDBMx5I6x3b_kI6cJOLecFBczZvaOjvqnYBPFXzav4llPBH9MMNy811jf6hqTAtJZji";
    private static String clientSecret = "EPrVUooJz7FNoE5u3fJ8kTuYUnaomzwpJs62Qy9dGhT7Yu2xD_Mo5IJGdkeNXilnZ_SpO2Hl7FErWfvI";

    public Paypal() {

    }

    public Map<String, Object> createPayment(String sum){
        Map<String, Object> response = new HashMap<String, Object>();
        Amount amount = new Amount();
        amount.setCurrency("USD");
        amount.setTotal(sum);
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        List<Transaction> transactions = new ArrayList<Transaction>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        String scheme = IDMService.getConfigs().getScheme();
        String hostName = IDMService.getConfigs().getHostName();
        int port = IDMService.getConfigs().getPort();
        String path = IDMService.getConfigs().getPath();
        URI uri = UriBuilder.fromUri(scheme + hostName + path).port(port).build();
        redirectUrls.setCancelUrl(uri.toString());
        redirectUrls.setReturnUrl(uri.toString() + "/orderComplete");
        payment.setRedirectUrls(redirectUrls);
        Payment createdPayment;
        try {
            String redirectUrl = "";
            APIContext context = new APIContext(clientId, clientSecret, "sandbox");
            createdPayment = payment.create(context);
            if(createdPayment!=null){
                List<Links> links = createdPayment.getLinks();
                for (Links link:links) {
                    if(link.getRel().equals("approval_url")){
                        redirectUrl = link.getHref();
                        break;
                    }
                }
                response.put("status", "success");
                response.put("redirect_url", redirectUrl);
            }
        } catch (PayPalRESTException e) {
            System.out.println("Error happened during payment creation!");
        }
        return response;
    }

    public static Map<String, Object> completePayment(String paymentId,String payerId){
        Map<String, Object> response = new HashMap();
        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);
        try {
            APIContext context = new APIContext(clientId, clientSecret, "sandbox");
            Payment createdPayment = payment.execute(context, paymentExecution);
            String transactionId = createdPayment.getTransactions().get(0).getRelatedResources().get(0).getSale().getId();
            if(createdPayment!=null){
                response.put("status", "success");
                response.put("payment", createdPayment);
                response.put("transactionId",transactionId);
            }
        } catch (PayPalRESTException e) {
            System.err.println(e.getDetails());
        }
        return response;
    }
}
