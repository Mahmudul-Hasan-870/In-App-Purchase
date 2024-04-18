package com.sarkartech.uniquenasheedacademy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.ImmutableList;
import com.sarkartech.uniquenasheedacademy.databinding.ActivitySubscriptionBinding;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubscriptionActivity extends AppCompatActivity {

    private BillingClient billingClient;
    String SubName, purchase, Description, dur;
    boolean isSuccess = false;
    ActivitySubscriptionBinding binding;

    String ProductID= "3_months";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySubscriptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        billingClient = BillingClient.newBuilder(this).setListener(purchasesUpdatedListener).enablePendingPurchases().build();
        GetPrice();
        if (ConnectionActivity.premium) {
            binding.tvSubstatus.setText("Status: Already Subscribed");
            binding.subscriptionBtn.setVisibility(View.GONE);
        } else {
            binding.tvSubstatus.setText("Status: Not Subscribed");
        }
    }

    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
            // To be implemented in a later section.
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (Purchase purchase : purchases) {
                    handlePurchase(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                binding.tvSubstatus.setText("Already Subscribed");
                isSuccess = true;
                ConnectionActivity.premium = true;
                ConnectionActivity.locked = false;
                binding.subscriptionBtn.setVisibility(View.GONE);
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED) {
                binding.tvSubstatus.setText("FEATURE NOT SUPPORTED");

            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
                binding.tvSubstatus.setText("BILLING_UNAVAILABLE");

            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                binding.tvSubstatus.setText("USER_CANCELED");

            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.DEVELOPER_ERROR) {
                binding.tvSubstatus.setText("DEVELOPER_ERROR");

            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE) {
                binding.tvSubstatus.setText("ITEM_UNAVAILABLE");

            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.NETWORK_ERROR) {
                binding.tvSubstatus.setText("NETWORK_ERROR");

            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
                binding.tvSubstatus.setText("SERVICE_DISCONNECTED");

            } else {
                Toast.makeText(SubscriptionActivity.this, "Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    void handlePurchase(final Purchase purchase) {
        ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
        ConsumeResponseListener consumeResponseListener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {


                }
            }
        };
        billingClient.consumeAsync(consumeParams, consumeResponseListener);
        if (purchase.getPurchaseState() == com.android.billingclient.api.Purchase.PurchaseState.PURCHASED) {
            if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                Toast.makeText(this, "Error : invalid Purchase", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
                binding.tvSubstatus.setText("Subscribed");
                isSuccess = true;
            } else {
                binding.tvSubstatus.setText("Already Subscribed");
            }
            ConnectionActivity.premium = true;
            ConnectionActivity.locked = false;
            binding.subscriptionBtn.setVisibility(View.GONE);
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            binding.tvSubstatus.setText("PENDING");

        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE) {
            binding.tvSubstatus.setText("UNSPECIFIED_STATE");

        }

    }

    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
            binding.tvSubstatus.setText("Subscribed");
            isSuccess = true;
            ConnectionActivity.premium = true;
            ConnectionActivity.locked = false;
        }
    };

    private boolean verifyValidSignature(String signedData, String signature) {
        try {
            String base64Key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjBTfu5KEZsLC0inE+uBTQcDlKGduKNBtWAOJasduBYvrNUd5PC3WsjIXyMS9xlJVBJNT1c5G8MHsct1fzTdjVFIL8/JksRwL0dbsOnimC7PJmowz6vs5h8wSSl7pGIM/9ccQSA5sWmyaiVRZEgwymGKfh6wiHrFbBJlxYlEg7pl+iGSszTANBCmDk3ANyOyat8VDl96DF0jes/D2bUAraKrN2PKItSnGKRBvTtlSEhDH5Zu5ti5ibmoUs0KFsDOjrMOzhc1x0IfDIEY5cKAtSdOg2Upag/am6bPogiUWLB4h4c8/ZJRApMzudMUjMNqL5FPIue6czzVF+Yqk9+zdwQIDAQAB";
            return Security.verifyPurchase(base64Key, signedData, signature);
        } catch (IOException e) {
            return false;
        }
    }

    private void GetPrice() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(ImmutableList.of(QueryProductDetailsParams.Product.newBuilder().setProductId(ProductID).setProductType(BillingClient.ProductType.SUBS).build())).build();

                            billingClient.queryProductDetailsAsync(queryProductDetailsParams, new ProductDetailsResponseListener() {
                                public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> productDetailsList) {
                                    // check billingResult
                                    // process returned productDetailsList

                                    for (ProductDetails productDetails : productDetailsList) {
                                        String offerToken = productDetails.getSubscriptionOfferDetails().get(0).getOfferToken();
                                        ImmutableList productDetailsParamsList = ImmutableList.of(BillingFlowParams.ProductDetailsParams.newBuilder()
                                                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                                .setProductDetails(productDetails)
                                                // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                                // for a list of offers that are available to the user
                                                .setOfferToken(offerToken).build());
                                        SubName = productDetails.getName();
                                        Description = productDetails.getDescription();
                                        String formattedPrice = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();
                                        String billingperiod = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getBillingPeriod();
                                        int recurrenceMode = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getRecurrenceMode();

                                        String n, duration, bp;
                                        bp = billingperiod;
                                        n = billingperiod.substring(1, 2);
                                        duration = billingperiod.substring(2, 3);
                                        if (recurrenceMode == 2) {
                                            if (duration.equals("M")) {
                                                dur = " For " + n + " Month";
                                            } else if (duration.equals("Y")) {
                                                dur = " For " + n + "Year";
                                            } else if (duration.equals("W")) {
                                                dur = " For " + n + " Week";
                                            } else if (duration.equals("D")) {
                                                dur = " For " + n + " Days";
                                            } else {
                                                if (bp.equals("P1M")) {
                                                    dur = "/Monthly";
                                                } else if (bp.equals("P6M")) {
                                                    dur = "/Every 6 Month";
                                                } else if (bp.equals("P1Y")) {
                                                    dur = "/Yearly";
                                                } else if (bp.equals("P1W")) {
                                                    dur = "/Weekly";
                                                } else if (bp.equals("P3W")) {
                                                    dur = "/Every 3 Week";
                                                }
                                            }
                                            purchase = formattedPrice + "" + dur;
                                            for (int i = 0; i <= (productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().size()); i++) {
                                                if (i > 0) {
                                                    String period = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(i).getBillingPeriod();
                                                    String price = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(i).getFormattedPrice();

                                                    if (period.equals("P1M")) {
                                                        dur = "/Monthly";
                                                    } else if (bp.equals("P6M")) {
                                                        dur = "/Every 6 Month";
                                                    } else if (bp.equals("P1Y")) {
                                                        dur = "/Yearly";
                                                    } else if (bp.equals("P1W")) {
                                                        dur = "/Weekly";
                                                    } else if (bp.equals("P3W")) {
                                                        dur = "/Every 3 Week";
                                                    }
                                                    purchase += "\n" + price + dur;
                                                }
                                            }
                                        }
                                    }
                                }
                            });

                        }
                    });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            binding.tvSubmonthly.setText(SubName);
                            binding.tvSubprice.setText("Price: " + purchase);
                            binding.tvSubbenifit.setText(Description);

                        }
                    });
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }

    public void Subscription(View view) {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {

            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(ImmutableList.of(QueryProductDetailsParams.Product.newBuilder().setProductId(ProductID).setProductType(BillingClient.ProductType.SUBS).build())).build();
                billingClient.queryProductDetailsAsync(queryProductDetailsParams, new ProductDetailsResponseListener() {
                    public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> productDetailsList) {
                        // check billingResult
                        // process returned productDetailsList

                        for (ProductDetails productDetails : productDetailsList) {
                            String offerToken = productDetails.getSubscriptionOfferDetails().get(0).getOfferToken();
                            ImmutableList productDetailsParamsList = ImmutableList.of(BillingFlowParams.ProductDetailsParams.newBuilder()
                                    // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                    .setProductDetails(productDetails)
                                    // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                    // for a list of offers that are available to the user
                                    .setOfferToken(offerToken).build());
                            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList).build();
                            billingClient.launchBillingFlow(SubscriptionActivity.this, billingFlowParams);
                        }
                    }
                });

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingClient != null) {
            billingClient.endConnection();
        }
    }
}