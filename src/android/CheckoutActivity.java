package cordova.plugin.stripeuiplugin;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import java.util.HashMap;
import org.json.JSONObject;

public class CheckoutActivity extends AppCompatActivity {
    Intent resultIntent = new Intent();
    HashMap<String, String> resultMap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resultMap.clear();
        Intent receivedIntent = getIntent();
        String publishableKey = receivedIntent.getStringExtra("publishableKey");
        String companyName = receivedIntent.getStringExtra("companyName");
        String paymentIntent = receivedIntent.getStringExtra("paymentIntent");
        String customer = receivedIntent.getStringExtra("customer");
        String ephemeralKey = receivedIntent.getStringExtra("ephemeralKey");
        String appleMerchantCountryCode = receivedIntent.getStringExtra("appleMerchantCountryCode");
        String billingAddressStr = receivedIntent.getStringExtra("billingAddress");

        try {
            assert publishableKey != null;
            assert paymentIntent != null;
            assert companyName != null;
            assert customer != null;
            assert ephemeralKey != null;
            assert appleMerchantCountryCode != null;

            PaymentConfiguration.init(this, publishableKey);
            PaymentSheet paymentSheet = new PaymentSheet(this, result -> {
                onPaymentSheetResult(result);
            });


            JSONObject billingAddressDetails = new JSONObject(billingAddressStr);
            String billingEmail = billingAddressDetails.getString("email");
            String billingName = billingAddressDetails.getString("name");
            String billingPhone = billingAddressDetails.getString("phone");
            String billingLine1 = billingAddressDetails.getString("line1");
            String billingLine2 = null;
            String billingCity = billingAddressDetails.getString("city");
            String billingState = billingAddressDetails.getString("state");
            String billingPostalCode = billingAddressDetails.getString("postalCode");
            String billingCountry = billingAddressDetails.getString("country");

            PaymentSheet.Address billingAddress = new PaymentSheet.Address(billingCity, billingCountry, billingLine1, billingLine2, billingPostalCode, billingState);
            PaymentSheet.BillingDetails billingDetails = new PaymentSheet.BillingDetails(billingAddress,billingEmail,billingName,billingPhone);
            PaymentSheet.CustomerConfiguration customerConfig = new PaymentSheet.CustomerConfiguration(customer, ephemeralKey);
            PaymentSheet.GooglePayConfiguration googlePayConfig = new PaymentSheet.GooglePayConfiguration(PaymentSheet.GooglePayConfiguration.Environment.Production, appleMerchantCountryCode);

            PaymentSheet.Configuration configuration = new PaymentSheet.Configuration(companyName, customerConfig, googlePayConfig, null, billingDetails);

            paymentSheet.presentWithPaymentIntent(paymentIntent, configuration);

        } catch (Exception e) {
            resultMap.put("code", "2");
            resultMap.put("message", "PAYMENT_FAILED");
            resultMap.put("error", e.getMessage());
            resultIntent.putExtra("result", resultMap);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        resultMap.clear();
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            resultMap.put("code", "0");
            resultMap.put("message", "PAYMENT_COMPLETED");
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            resultMap.put("code", "1");
            resultMap.put("message", "PAYMENT_CANCELED");
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            resultMap.put("code", "2");
            resultMap.put("message", "PAYMENT_FAILED");
            resultMap.put("error", ((PaymentSheetResult.Failed) paymentSheetResult).getError().getMessage());
        }
        resultIntent.putExtra("result", resultMap);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
