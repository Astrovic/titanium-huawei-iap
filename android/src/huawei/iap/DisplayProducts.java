package huawei.iap;

import com.huawei.hmf.tasks.Task;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.ProductInfo;
import com.huawei.hms.iap.entity.ProductInfoReq;
import com.huawei.hms.iap.entity.ProductInfoResult;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollObject;
import org.appcelerator.titanium.TiApplication;

import java.util.ArrayList;
import java.util.List;

import huawei.iap.helper.Defaults;


public class DisplayProducts {
    private KrollFunction callback;
    private KrollObject krollObject;

    public DisplayProducts(Object callback, KrollObject krollObject) {
        this.callback = (KrollFunction) callback;
        this.krollObject = krollObject;
    }

    public void fetchProductList(int priceType, Object[] items) {
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

        task.addOnSuccessListener(result -> {
            if (result == null) {
                onFail("ProductInfoResult null");
                return;
            }

            if (result.getProductInfoList().isEmpty()) {
                onFail("ProductInfo list empty");
                return;
            }

            onResult(result);
        }).addOnFailureListener(e -> onFail(e.toString()));
    }

    private void onFail(String msg) {
        KrollDict result = new KrollDict();
        result.put(Defaults.PROPERTY_SUCCESS, false);
        result.put(Defaults.PROPERTY_MESSAGE, msg);
        result.put(Defaults.PROPERTY_ITEM_LIST, new Object[0]);
        callback.callAsync(krollObject, result);
    }

    private void onResult(ProductInfoResult productInfoResult) {
        List<ProductInfo> productInfoList = productInfoResult.getProductInfoList();
        ArrayList<KrollDict> items = new ArrayList<>();

        for (ProductInfo productInfo: productInfoList) {
            KrollDict itemDict = new KrollDict();

            itemDict.put(Defaults.ItemProperty.ProductId, productInfo.getProductId());
            itemDict.put(Defaults.ItemProperty.PriceType, productInfo.getPriceType());
            itemDict.put(Defaults.ItemProperty.Price, productInfo.getPrice());
            itemDict.put(Defaults.ItemProperty.MicrosPrice, productInfo.getMicrosPrice());
            itemDict.put(Defaults.ItemProperty.OriginalLocalPrice, productInfo.getOriginalLocalPrice());
            itemDict.put(Defaults.ItemProperty.OriginalMicroPrice, productInfo.getOriginalMicroPrice());
            itemDict.put(Defaults.ItemProperty.Currency, productInfo.getCurrency());
            itemDict.put(Defaults.ItemProperty.ProductName, productInfo.getProductName());
            itemDict.put(Defaults.ItemProperty.ProductDesc, productInfo.getProductDesc());
            itemDict.put(Defaults.ItemProperty.SubPeriod, productInfo.getSubPeriod());
            itemDict.put(Defaults.ItemProperty.SubSpecialPrice, productInfo.getSubSpecialPrice());
            itemDict.put(Defaults.ItemProperty.SubSpecialPriceMicros, productInfo.getSubSpecialPriceMicros());
            itemDict.put(Defaults.ItemProperty.SubSpecialPeriod, productInfo.getSubSpecialPeriod());
            itemDict.put(Defaults.ItemProperty.SubSpecialPeriodCycles, productInfo.getSubSpecialPeriodCycles());
            itemDict.put(Defaults.ItemProperty.SubFreeTrialPeriod, productInfo.getSubFreeTrialPeriod());
            itemDict.put(Defaults.ItemProperty.SubGroupId, productInfo.getSubGroupId());
            itemDict.put(Defaults.ItemProperty.SubGroupTitle, productInfo.getSubGroupTitle());
            itemDict.put(Defaults.ItemProperty.SubProductLevel, productInfo.getSubProductLevel());
            itemDict.put(Defaults.ItemProperty.Status, productInfo.getStatus());

            items.add(itemDict);
        }

        KrollDict result = new KrollDict();
        result.put(Defaults.PROPERTY_SUCCESS, true);
        result.put(Defaults.PROPERTY_MESSAGE, "");
        result.put(Defaults.PROPERTY_ITEM_LIST, items.toArray());
        callback.callAsync(krollObject, result);
    }
}
