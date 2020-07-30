package huawei.iap;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.util.Log;
import android.widget.Toast;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapApiException;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseReq;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseResult;
import com.huawei.hms.iap.entity.InAppPurchaseData;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.entity.PurchaseIntentReq;
import com.huawei.hms.iap.entity.PurchaseIntentResult;
import com.huawei.hms.iap.entity.PurchaseResultInfo;
import com.huawei.hms.support.api.client.Status;

import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollObject;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiActivityResultHandler;
import org.json.JSONException;

import huawei.iap.common.CipherUtil;

import static huawei.iap.helper.Defaults.REQ_CODE_BUY;


public class PurchaseProducts implements TiActivityResultHandler {
    private KrollObject krollObject;
    private KrollFunction callback = null;

    /**
     * Create a PurchaseIntentReq instance
     * @param type In-app product type
     * @param productId ID of the in-app product to be paid, and set during in-app product configuration in AppGallery Connect
     * @return PurchaseIntentReq
     */
    private PurchaseIntentReq createPurchaseIntentReq(int type, String productId) {
        PurchaseIntentReq req = new PurchaseIntentReq();
        req.setProductId(productId);
        req.setPriceType(type);
        req.setDeveloperPayload("test");
        return req;
    }

    /**
     * create orders for in-app products in the PMS
     * @param productId ID list of products to be queried. Each product ID must exist and be unique in the current app
     * @param productType  In-app product type
     */
    private void gotoPay(String productId, int productType) {
        Log.i(HuaweiIapModule.TAG, "call createPurchaseIntent");
        IapClient mClient = Iap.getIapClient(TiApplication.getAppCurrentActivity());
        Task<PurchaseIntentResult> task = mClient.createPurchaseIntent(createPurchaseIntentReq(productType, productId));
        task.addOnSuccessListener(new OnSuccessListener<PurchaseIntentResult>() {
            @Override
            public void onSuccess(PurchaseIntentResult result) {
                Log.i(HuaweiIapModule.TAG, "createPurchaseIntent, onSuccess");
                if (result == null) {
                    Log.e(HuaweiIapModule.TAG, "result is null");
                    return;
                }
                Status status = result.getStatus();
                if (status == null) {
                    Log.e(HuaweiIapModule.TAG, "status is null");
                    return;
                }
                // you should pull up the page to complete the payment process.
                if (status.hasResolution()) {
                    try {
                        status.startResolutionForResult(TiApplication.getAppCurrentActivity(), REQ_CODE_BUY);
                    } catch (IntentSender.SendIntentException exp) {
                        Log.e(HuaweiIapModule.TAG, exp.toString());
                    }
                } else {
                    Log.e(HuaweiIapModule.TAG, "intent is null");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(HuaweiIapModule.TAG, e.toString());

                Toast.makeText(TiApplication.getAppCurrentActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();

                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException)e;
                    int returnCode = apiException.getStatusCode();
                    Log.e(HuaweiIapModule.TAG, "createPurchaseIntent, returnCode: " + returnCode);
                    // handle error scenarios
                } else {
                    // Other external errors
                }
            }
        });
    }


    @Override
    public void onResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED){
            // "Result Cancelled"

        } else if (REQ_CODE_BUY == requestCode && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                // "error"
                return;
            }

            PurchaseResultInfo purchaseResultInfo = Iap.getIapClient(TiApplication.getAppCurrentActivity()).parsePurchaseResultInfoFromIntent(data);

            switch(purchaseResultInfo.getReturnCode()) {
                case OrderStatusCode.ORDER_STATE_SUCCESS:
                    // verify signature of payment results.
                    boolean success = CipherUtil.doCheck(purchaseResultInfo.getInAppPurchaseData(), purchaseResultInfo.getInAppDataSignature());
                    if (success) {
                        // Call the consumeOwnedPurchase interface to consume it after successfully delivering the product to your user.
                        consumeOwnedPurchase(purchaseResultInfo.getInAppPurchaseData());
                    } else {
                        // "Pay successful,sign failed"
                    }
                    return;
                case OrderStatusCode.ORDER_STATE_CANCEL:
                    // The User cancels payment.
                    // "user cancel"
                    return;
                case OrderStatusCode.ORDER_PRODUCT_OWNED:
                    // The user has already owned the product.
                    // "you have owned the product"
                    // you can check if the user has purchased the product and decide whether to provide goods
                    // if the purchase is a consumable product, consuming the purchase and deliver product
                    return;

                default:
                    // "Pay failed"
                    break;
            }
            return;
        }
    }

    @Override
    public void onError(Activity activity, int requestCode, Exception exc) {

    }

    /**
     * Consume the unconsumed purchase with type 0 after successfully delivering the product, then the Huawei payment server will update the order status and the user can purchase the product again.
     * @param inAppPurchaseData JSON string that contains purchase order details.
     */
    private void consumeOwnedPurchase(String inAppPurchaseData) {
        Log.i(HuaweiIapModule.TAG, "call consumeOwnedPurchase");
        IapClient mClient = Iap.getIapClient(TiApplication.getAppCurrentActivity());
        Task<ConsumeOwnedPurchaseResult> task = mClient.consumeOwnedPurchase(createConsumeOwnedPurchaseReq(inAppPurchaseData));
        task.addOnSuccessListener(new OnSuccessListener<ConsumeOwnedPurchaseResult>() {
            @Override
            public void onSuccess(ConsumeOwnedPurchaseResult result) {
                // consumeOwnedPurchase success
                // "Pay success, and the product has been delivered"
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // failure
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException)e;
                    int returnCode = apiException.getStatusCode();
                    Log.e(HuaweiIapModule.TAG, "consumeOwnedPurchase fail,returnCode: " + returnCode);
                } else {
                    // Other external errors
                }
            }
        });
    }

    /**
     * Create a ConsumeOwnedPurchaseReq instance.
     * @param purchaseData JSON string that contains purchase order details.
     * @return ConsumeOwnedPurchaseReq
     */
    private ConsumeOwnedPurchaseReq createConsumeOwnedPurchaseReq(String purchaseData) {
        ConsumeOwnedPurchaseReq req = new ConsumeOwnedPurchaseReq();

        // Parse purchaseToken from InAppPurchaseData in JSON format.
        try {
            InAppPurchaseData inAppPurchaseData = new InAppPurchaseData(purchaseData);
            req.setPurchaseToken(inAppPurchaseData.getPurchaseToken());
        } catch (JSONException e) {
            Log.e(HuaweiIapModule.TAG, "createConsumeOwnedPurchaseReq JSONExeption");
        }
        return req;
    }
}
