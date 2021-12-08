package com.demo.monitor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JOptionPane;
import java.awt.Toolkit;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws InterruptedException {


//        String url = "https://www.apple.com.cn/shop/fulfillment-messages?pl=true&mt=compact&parts.0=MLHG3CH/A&searchNearby=true&store=R577";
        String url = "https://www.apple.com.cn/shop/fulfillment-messages?pl=true&mt=compact&parts.0=MLDQ3CH/A&searchNearby=true&store=R639";
//
//        JSONObject data = new JSONObject();
//        JSONObject dataTemp = new JSONObject();
        String[] paramArray = url.split("\\?")[1].split("&");
        JSONObject parameters = new JSONObject();
        for (String parameter : paramArray) {
            String[] split = parameter.split("=");
            String key = split[0];
            String value = split[1];
            parameters.put(key, value);
        }
        String SKU = parameters.getString("parts.0");
//        dataTemp.put("MLHG3CH/A", "iPhone 13 Pro Max 远峰蓝色 512G");
//        data.put("city","广东广州");
//        data.put("model", dataTemp);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        boolean flag = true;
        while (true){
            String response = ApacheHttpUtil.doPost(url, "");
            JSONObject responseJSON = JSON.parseObject(response);
            JSONArray stores = responseJSON.getJSONObject("body").getJSONObject("content").getJSONObject("pickupMessage").getJSONArray("stores");

            for (Object store : stores) {
                JSONObject storeOne = JSON.parseObject(String.valueOf(store));
                String storeName = storeOne.getString("storeName");
                if (storeName.trim().equals("天环广场") || storeName.trim().equals("珠江新城")){
                    JSONObject storeStatus = storeOne.getJSONObject("partsAvailability").getJSONObject(SKU);
                    Boolean isHaving = storeStatus.getBoolean("storeSelectionEnabled");
                    if (isHaving){
                        //TODO:调用发送消息接口，如飞书，微信
                        logger.info("有货了有货了！！！！！！！！");
                        String pickupSearchQuote = storeStatus.getString("pickupSearchQuote");
                        String storePickupProductTitle = storeStatus.getString("storePickupProductTitle");
                        logger.info("店铺：[{}][{}]{}", storeName, storePickupProductTitle, pickupSearchQuote);
                        String message = MessageFormat.format("店铺：[{0}][{1}]{2}\n时间：{3}",
                                storeName,
                                storePickupProductTitle,
                                pickupSearchQuote,
                                format.format(new Date()));
                        //不带图标
                        Toolkit.getDefaultToolkit().beep();
                        new Thread(() -> JOptionPane.showMessageDialog(null, message, "有货了", JOptionPane.INFORMATION_MESSAGE)).start();
                    }else {
                        String pickupSearchQuote = storeStatus.getString("pickupSearchQuote");
                        String storePickupProductTitle = storeStatus.getString("storePickupProductTitle");
                        logger.info("店铺：[{}][{}]{}", storeName, storePickupProductTitle, pickupSearchQuote);
                    }
                }
            }

            Thread.sleep(Duration.ofSeconds(30).toMillis());
        }
    }
}
