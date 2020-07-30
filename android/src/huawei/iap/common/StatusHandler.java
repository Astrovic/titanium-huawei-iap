package huawei.iap.common;

import com.huawei.hms.iap.IapApiException;
import com.huawei.hms.iap.entity.OrderStatusCode;


public class StatusHandler {
    /**
     * Handles the exception returned from the iap api.
     * @param e The exception returned from the iap api.
     * @return String
     */
    public static String getStatusMessage(Exception e) {

        if (e instanceof IapApiException) {
            IapApiException iapApiException = (IapApiException) e;

            switch (iapApiException.getStatusCode()) {
                case OrderStatusCode.ORDER_STATE_CANCEL:
                    return "Order has been canceled!";

                case OrderStatusCode.ORDER_STATE_PARAM_ERROR:
                    return "Order state param error!";

                case OrderStatusCode.ORDER_STATE_NET_ERROR:
                    return "Order state net error!";

                case OrderStatusCode.ORDER_VR_UNINSTALL_ERROR:
                    return "Order VR uninstall error!";

                case OrderStatusCode.ORDER_HWID_NOT_LOGIN:
                    return "Login again";

                case OrderStatusCode.ORDER_PRODUCT_OWNED:
                    return "Product already owned error!";

                case OrderStatusCode.ORDER_PRODUCT_NOT_OWNED:
                    return "Product not owned error!";

                case OrderStatusCode.ORDER_PRODUCT_CONSUMED:
                    return "Product consumed error!";

                case OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED:
                    return "Order account area not supported error!";

                case OrderStatusCode.ORDER_NOT_ACCEPT_AGREEMENT:
                    return "User does not agree the agreement";

                default:
                    return "Order unknown error!";
            }
        } else {
            return "external error: " + e.getMessage();
        }
    }
}