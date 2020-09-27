package httpasyncclient;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.nio.entity.NByteArrayEntity;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class HttpAsyncClientTest {
    //https://www.baeldung.com/httpclient-post-http-request
    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
        test01();
    }

    public static void test00() throws ExecutionException, InterruptedException, IOException {
        String str= "delivery_time=0&total=0.02&taxpayer_id=&wm_poi_name=t_pdi3qCPMyv&utime=1599615949&detail=%255B%257B%2522app_food_code%2522%253A%2522A001%2522%252C%2522box_num%2522%253A1.0%252C%2522box_price%2522%253A0.0%252C%2522food_discount%2522%253A1.0%252C%2522food_name%2522%253A%2522A001%2522%252C%2522price%2522%253A0.01%252C%2522quantity%2522%253A1%252C%2522sku_id%2522%253A%2522A001%2522%252C%2522unit%2522%253A%2522%25E4%25BB%25BD%2522%252C%2522upc%2522%253A%2522%2522%252C%2522weight%2522%253A1000%252C%2522weight_for_unit%2522%253A%25221000.00%2522%252C%2522weight_unit%2522%253A%2522%25E5%2585%258B%2528g%2529%2522%257D%255D&caution=%2B%25E3%2580%2590%25E5%25A6%2582%25E9%2581%2587%25E7%25BC%25BA%25E8%25B4%25A7%25E3%2580%2591%25EF%25BC%259A%2B%25E7%25BC%25BA%25E8%25B4%25A7%25E6%2597%25B6%25E7%2594%25B5%25E8%25AF%259D%25E4%25B8%258E%25E6%2588%2591%25E6%25B2%259F%25E9%2580%259A%2B%25E9%25A1%25BE%25E5%25AE%25A2%25E6%259C%25AA%25E5%25AF%25B9%25E9%25A4%2590%25E5%2585%25B7%25E6%2595%25B0%25E9%2587%258F%25E5%2581%259A%25E9%2580%2589%25E6%258B%25A9&original_price=0.02&recipient_name=%25E8%25B5%25B5%25E6%2599%2593%25E4%25BA%2589%2528%25E5%2585%2588%25E7%2594%259F%2529&order_id=27057681346841638&wm_poi_phone=4009208801&timestamp=1599615962&city_id=999999&pay_type=2&backup_recipient_phone=%255B%255D&wm_poi_id=2705768&longitude=95.369232&invMakeType=0&avg_send_time=2712.0&logistics_code=&invoice_title=&status=2&app_poi_code=5308_2705768&shipper_phone=&is_third_shipping=0&shipping_fee=0.01&ctime=1599615949&has_invoiced=0&extras=%255B%255D&incmp_modules=%255B%255D&recipient_phone=17665335287&wm_poi_address=%25E5%258D%2597%25E6%259E%2581%25E6%25B4%25B204%25E5%258F%25B7%25E7%25AB%2599&wm_order_id_view=27057681346841638&incmp_code=0&app_id=5308&latitude=29.774482&recipient_address=%25E6%2598%258C%25E9%2583%25BD%25E5%25A4%25A7%25E9%25B9%2585%25E8%258C%25B6%25E9%25A4%2590%25E5%258E%2585%2B%25281101%2529&sig=db04d1d4e1506b5bd312b3dec1de5c04";
        String str2 = "caution=%2B%25E3%2580%2590%25E5%25A6%2582%25E9%2581%2587%25E7%25BC%25BA%25E8%25B4%25A7%25E3%2580%2591%25EF%25BC%259A%2B%25E7%25BC%25BA%25E8%25B4%25A7%25E6%2597%25B6%25E7%2594%25B5%25E8%25AF%259D%25E4%25B8%258E%25E6%2588%2591%25E6%25B2%259F%25E9%2580%259A%2B%25E9%25A1%25BE%25E5%25AE%25A2%25E6%259C%25AA%25E5%25AF%25B9%25E9%25A4%2590%25E5%2585%25B7%25E6%2595%25B0%25E9%2587%258F%25E5%2581%259A%25E9%2580%2589%25E6%258B%25A9";
        byte[] bytes = str2.getBytes(StandardCharsets.ISO_8859_1);
        CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
        client.start();
        String url = "http://test-varian.quanqiuwa.com/api/third-open-api/app/mt/push/receiveOrderUserSubmitPush";
        String url2 = "http://localhost:8900/app/mt/push/receiveOrderUserSubmitPush";
        HttpPost request = new HttpPost(url2);
        request.setEntity(new NByteArrayEntity(bytes, ContentType.create("application/x-www-form-urlencoded", StandardCharsets.UTF_8)));
        Future<HttpResponse> future = client.execute(request, null);
        HttpResponse response = future.get();
        System.out.println(IOUtils.toString(response.getEntity().getContent()));
        client.close();

    }

    public static void test01() throws IOException, ExecutionException, InterruptedException {
        CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
        client.start();
        String url2 = "http://localhost:8900/app/mt/push/receiveOrderUserSubmitPush";
        String url = "http://test-varian.quanqiuwa.com/api/third-open-api/app/mt/push/receiveOrderUserSubmitPush";
        HttpPost request = new HttpPost(url2);
        //request.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("caution","【如遇缺货】： 缺货时电话与我沟通 顾客未对餐具数量做选择"));
        params.add(new BasicNameValuePair("recipient_name","xxx(先生)"));
        request.setEntity(new UrlEncodedFormEntity(params, Charset.forName("GBK")));
        Future<HttpResponse> future = client.execute(request, null);
        HttpResponse response = future.get();
        System.out.println(IOUtils.toString(response.getEntity().getContent()));
        client.close();
    }
}
