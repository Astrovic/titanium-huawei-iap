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
import java.util.Arrays;
import java.util.List;


public class DisplayProducts {
    public void fetchProductList(int priceType, Object[] items, KrollFunction callback) {
        ProductInfoReq productInfoReq = new ProductInfoReq();
        productInfoReq.setPriceType(priceType);

        ArrayList<String> productIds = new ArrayList<>();
        for (Object item: items) {
            productIds.add(String.valueOf(item));
        }

        productInfoReq.setProductIds(productIds);

        // obtain in-app product details configured in AppGallery Connect, and then show the products
        IapClient iapClient = Iap.getIapClient(TiApplication.getAppRootOrCurrentActivity());
        Task<ProductInfoResult> task = iapClient.obtainProductInfo(productInfoReq);

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
