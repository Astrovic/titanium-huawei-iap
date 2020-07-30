package huawei.iap;

import android.util.Log;
import android.widget.Toast;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.ProductInfo;
import com.huawei.hms.iap.entity.ProductInfoReq;
import com.huawei.hms.iap.entity.ProductInfoResult;

import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.titanium.TiApplication;

import java.util.ArrayList;
import java.util.List;


public class DisplayProducts {
    private ProductInfoReq createProductInfoReq() {
        ProductInfoReq req = new ProductInfoReq();

        // In-app product type contains:
        // 0: consumable
        // 1: non-consumable
        // 2: auto-renewable subscription
        req.setPriceType(IapClient.PriceType.IN_APP_NONCONSUMABLE);

        ArrayList<String> productIds = new ArrayList<>();

        // Pass in the item_productId list of products to be queried.
        // The product ID is the same as that set by a developer when configuring product information in AppGallery Connect.
        productIds.add("CProduct1");

        req.setProductIds(productIds);

        return req;
    }

    private void loadProduct(KrollFunction callback) {
        // obtain in-app product details configured in AppGallery Connect, and then show the products
        IapClient iapClient = Iap.getIapClient(TiApplication.getAppRootOrCurrentActivity());

        Task<ProductInfoResult> task = iapClient.obtainProductInfo(createProductInfoReq());

        task.addOnSuccessListener(new OnSuccessListener<ProductInfoResult>() {
            @Override
            public void onSuccess(ProductInfoResult result) {
                if (result != null && !result.getProductInfoList().isEmpty()) {
                    List<ProductInfo> productInfo = result.getProductInfoList();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(HuaweiIapModule.TAG, e.getMessage());
                Toast.makeText(TiApplication.getAppRootOrCurrentActivity(), "error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
