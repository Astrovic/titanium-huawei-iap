package huawei.iap;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.util.Log;

import com.huawei.hmf.tasks.Task;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.entity.PurchaseIntentReq;
import com.huawei.hms.iap.entity.PurchaseIntentResult;
import com.huawei.hms.iap.entity.PurchaseResultInfo;
import com.huawei.hms.support.api.client.Status;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollObject;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiActivityResultHandler;
import org.appcelerator.titanium.util.TiActivitySupport;

import java.lang.reflect.Field;

import huawei.iap.common.CipherUtil;
import huawei.iap.common.StatusHandler;
import huawei.iap.hacker.Hack;
import huawei.iap.helper.Defaults;
import ti.modules.titanium.ui.ScrollableViewProxy;

import static huawei.iap.helper.Defaults.REQ_CODE_BUY;


public class PurchaseProduct implements TiActivityResultHandler {
    private static final String TAG = "PurchaseProduct";
    private KrollObject krollObject;
    private KrollFunction callback;

    public PurchaseProduct(Object callback, KrollObject krollObject) {
        this.krollObject = krollObject;
        this.callback = (KrollFunction) callback;
    }

    private void onFail(String msg) {
        KrollDict result = new KrollDict();
        result.put(Defaults.PROPERTY_CODE, -1);
        result.put(Defaults.PROPERTY_SUCCESS, false);
        result.put(Defaults.PROPERTY_MESSAGE, msg);
        callback.callAsync(krollObject, result);
    }

    private void onSuccess(String msg, int code) {
        KrollDict result = new KrollDict();
        result.put(Defaults.PROPERTY_CODE, code);
        result.put(Defaults.PROPERTY_SUCCESS, true);
        result.put(Defaults.PROPERTY_MESSAGE, msg);
        callback.callAsync(krollObject, result);
    }

    /**
     * Create a PurchaseIntentReq instance
     * @param productId ID of the in-app product to be paid, and set during in-app product configuration in AppGallery Connect
     * @param priceType In-app product type
     * @return PurchaseIntentReq
     */
    private PurchaseIntentReq createPurchaseIntentReq(String productId, int priceType, String developerPayload) {
        PurchaseIntentReq purchaseIntentReq = new PurchaseIntentReq();
        purchaseIntentReq.setProductId(productId);
        purchaseIntentReq.setPriceType(priceType);
        purchaseIntentReq.setDeveloperPayload(developerPayload);
        return purchaseIntentReq;
    }

    /**
     * create orders for in-app products in the PMS
     * @param productId ID list of products to be queried. Each product ID must exist and be unique in the current app
     * @param priceType  In-app product type
     * @param developerPayload  custom developer payload
     */
    public void initiatePurchase(String productId, int priceType, String developerPayload) {
        PurchaseIntentReq purchaseIntentReq = createPurchaseIntentReq(productId, priceType, developerPayload);

        IapClient mClient = Iap.getIapClient(TiApplication.getAppCurrentActivity());

        Task<PurchaseIntentResult> task = mClient.createPurchaseIntent(purchaseIntentReq);

        task.addOnSuccessListener(result -> {
            // result: PurchaseIntentResult
            if (result == null) {
                onFail("PurchaseIntentResult: null");
                return;
            }

            Status status = result.getStatus();

            if (status == null) {
                onFail("PurchaseIntentResult: status null");
                return;
            }

            // you should pull up the page to complete the payment process.
            if (status.hasResolution()) {
                try {
                    PendingIntent pendingIntent = Hack.hackPendingIntent(status);
                    Intent intent = Hack.hackIntent(status);
                    TiActivitySupport tiActivitySupport = ((TiActivitySupport) TiApplication.getAppCurrentActivity());

                    if (pendingIntent != null) {
                        Log.i(TAG, "<<< launching from pendingIntent >>>");
                        tiActivitySupport.launchIntentSenderForResult(pendingIntent.getIntentSender(), REQ_CODE_BUY, (Intent)null, 0, 0, 0, null, this);
                        Log.i(TAG, "<<< launching from pendingIntent done >>>");
                    } else if (intent != null) {
                        Log.i(TAG, "<<< launching from intent >>>");
                        tiActivitySupport.launchActivityForResult(intent, REQ_CODE_BUY, this);
                        Log.i(TAG, "<<< launching from intent done >>>");
                    } else {
                        Log.i(TAG, "launching normally from IAP SDK");
                        status.startResolutionForResult(TiApplication.getAppCurrentActivity(), REQ_CODE_BUY);
                    }

                } catch (IntentSender.SendIntentException exp) {
                    onFail("PurchaseIntentResult: " + exp.toString());
                }
            } else {
                onFail("PurchaseIntentResult: status has no resolution");
            }
        }).addOnFailureListener(e -> onFail("PurchaseIntent: OnFailure: " + StatusHandler.getStatusMessage(e)));
    }


    @Override
    public void onResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED){
            Log.i(TAG, "onResult: result cancelled");
            onFail("onResult: result cancelled");
            return;
        }

        if (REQ_CODE_BUY == requestCode && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Log.i(TAG, "onResult: data null");
                onFail("onResult: data null");
                return;
            }

            PurchaseResultInfo purchaseResultInfo = Iap.getIapClient(TiApplication.getAppCurrentActivity()).parsePurchaseResultInfoFromIntent(data);

            switch(purchaseResultInfo.getReturnCode()) {
                case OrderStatusCode.ORDER_STATE_SUCCESS:
                    String inAppPurchaseData = purchaseResultInfo.getInAppPurchaseData();
                    String inAppPurchaseDataSignature = purchaseResultInfo.getInAppDataSignature();

                    // verify signature of payment results.
                    boolean success = CipherUtil.doCheck(inAppPurchaseData, inAppPurchaseDataSignature);

                    if (success) {
                        Log.i(TAG, "Payment successful and product delivered");
                        onSuccess("Payment successful and product delivered", HuaweiIapModule.CODE_PAYMENT_SUCCESS);
                    } else {
                        Log.i(TAG, "Payment successful but signature failed");
                        onSuccess("Payment successful but signature failed", HuaweiIapModule.CODE_PAYMENT_SUCCESS_SIGNATURE_FAILED);
                    }

                    break;

                case OrderStatusCode.ORDER_STATE_CANCEL:
                    Log.i(TAG, "onResult: user cancelled the payment");
                    onFail("onResult: user cancelled the payment");
                    break;

                case OrderStatusCode.ORDER_PRODUCT_OWNED:
                    Log.i(TAG, "You already owned the product");
                    onSuccess("You already owned the product", HuaweiIapModule.CODE_OWNED_PRODUCT);
                    break;

                default:
                    Log.i(TAG, "onResult: payment failed");
                    onFail("onResult: payment failed");
                    break;
            }
        } else {
            Log.i(TAG, "onResult: requestCode or resultCode null");
            onFail("onResult: requestCode or resultCode null");
        }
    }

    @Override
    public void onError(Activity activity, int requestCode, Exception exc) {
        if (REQ_CODE_BUY == requestCode) {
            Log.i(TAG, "onError: unknown error");
            onFail("onError: unknown error");
        }
    }
}
