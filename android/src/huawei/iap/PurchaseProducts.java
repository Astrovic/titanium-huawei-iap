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
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.entity.PurchaseIntentReq;
import com.huawei.hms.iap.entity.PurchaseIntentResult;
import com.huawei.hms.iap.entity.PurchaseResultInfo;
import com.huawei.hms.support.api.client.Status;

import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollObject;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiActivityResultHandler;


public class PurchaseProducts implements TiActivityResultHandler {
    private KrollObject krollObject;
    private KrollFunction callback = null;


    /**
     * create orders for in-app products in the PMS
     * @param productId ID list of products to be queried. Each product ID must exist and be unique in the current app.
     * @param type  In-app product type.
     */
    private void gotoPay(String productId, int type) {
        Log.i(HuaweiIapModule.TAG, "call createPurchaseIntent");
        IapClient mClient = Iap.getIapClient(TiApplication.getAppCurrentActivity());
        Task<PurchaseIntentResult> task = mClient.createPurchaseIntent(createPurchaseIntentReq(type, productId));
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
                        Log.e(HuaweiIapModule.TAG, exp.getMessage());
                    }
                } else {
                    Log.e(HuaweiIapModule.TAG, "intent is null");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(HuaweiIapModule.TAG, e.getMessage());

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

    /**
     * Create a PurchaseIntentReq instance.
     * @param type In-app product type.
     * @param productId ID of the in-app product to be paid.
     *              The in-app product ID is the product ID you set during in-app product configuration in AppGallery Connect.
     * @return PurchaseIntentReq
     */
    private PurchaseIntentReq createPurchaseIntentReq(int type, String productId) {
        PurchaseIntentReq req = new PurchaseIntentReq();
        req.setProductId(productId);
        req.setPriceType(type);
        req.setDeveloperPayload("test");
        return req;
    }

    @Override
    public void onResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_BUY) {
            if (data == null) {
                // "error"
                return;
            }
            
            PurchaseResultInfo purchaseResultInfo = Iap.getIapClient(TiApplication.getAppCurrentActivity()).parsePurchaseResultInfoFromIntent(data);

            switch(purchaseResultInfo.getReturnCode()) {
                case OrderStatusCode.ORDER_STATE_SUCCESS:
                    // verify signature of payment results.
                    boolean success = CipherUtil.doCheck(purchaseResultInfo.getInAppPurchaseData(), purchaseResultInfo.getInAppDataSignature(), Key.getPublicKey());
                    if (success) {
                        // Call the consumeOwnedPurchase interface to consume it after successfully delivering the product to your user.
                        consumeOwnedPurchase(this, purchaseResultInfo.getInAppPurchaseData());
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
}
