const HMS_IAP = require('huawei.iap');

// first set the api-key
HMS_IAP.apiKey = 'ertfghvbju8976rtdfgcvhjiou';


// constants from module
const PRICE_TYPE_CONSUMABLE = HMS_IAP.PRICE_TYPE_CONSUMABLE;
const PRICE_TYPE_NON_CONSUMABLE = HMS_IAP.PRICE_TYPE_NON_CONSUMABLE;
const PRICE_TYPE_SUBSCRIPTION = HMS_IAP.PRICE_TYPE_SUBSCRIPTION;
const CODE_PAYMENT_SUCCESS = HMS_IAP.CODE_PAYMENT_SUCCESS;
const CODE_PAYMENT_SUCCESS_SIGNATURE_FAILED = HMS_IAP.CODE_PAYMENT_SUCCESS_SIGNATURE_FAILED;
const CODE_OWNED_PRODUCT = HMS_IAP.CODE_OWNED_PRODUCT;


function fetchItemList() {
    // this method will be used to show the real-time details of the queried item-id
    HMS_IAP.getItemList({
        priceType: PRICE_TYPE_NON_CONSUMABLE,
        items: ['123', '234'],   // your product item-ids as string in this array
        callback: function (e) {
            // success: true/false
            // message: failure message when success is false
            // items: array of queried items details
            if (e.success) {
                const itemList = e.items;   // item details array

                // contains below key-value pairs
                const item1 = itemList[0];
                // item1.productId
                // item1.priceType
                // item1.price
                // item1.microsPrice
                // item1.originalLocalPrice
                // item1.originalMicroPrice
                // item1.currency
                // item1.productName
                // item1.productDesc
                // item1.subPeriod
                // item1.subSpecialPrice
                // item1.subSpecialPriceMicros
                // item1.subSpecialPeriod
                // item1.subSpecialPeriodCycles
                // item1.subFreeTrialPeriod
                // item1.subGroupId
                // item1.subGroupTitle
                // item1.subProductLevel
                // item1.status

            } else {
                alert(e.message);
            }
        }
    });
}

function buyItem() {
    // this method will be used to show the real-time details of the queried item-id
    HMS_IAP.purchaseItem({
        priceType: PRICE_TYPE_NON_CONSUMABLE,
        productId: '123', // your product item-id to buy
        developerPayload: 'optional: developer message for this purchase',
        callback: function (e) {
            // success: true/false
            // message: failure message when success is false
            // code: -1 if success is false / else below codes based on payment result
            // CODE_PAYMENT_SUCCESS
            // CODE_PAYMENT_SUCCESS_SIGNATURE_FAILED
            // CODE_OWNED_PRODUCT
            if (e.success) {
                if (e.code == CODE_PAYMENT_SUCCESS) {
                    alert('Well done!');
                } else if (e.code == CODE_PAYMENT_SUCCESS_SIGNATURE_FAILED) {
                    alert(e.message);
                } else if (e.code == CODE_OWNED_PRODUCT) {
                    alert(e.message);
                }

            } else {
                alert('Code: ' + e.code + ' :: Message: ' +e.message);
            }
        }
    });
}
