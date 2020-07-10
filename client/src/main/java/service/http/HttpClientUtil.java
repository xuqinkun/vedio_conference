package service.http;

import common.bean.HttpResult;
import common.bean.User;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.JsonUtil;

import java.io.IOException;

public class HttpClientUtil {

    private static final HttpClientUtil INSTANCE = new HttpClientUtil();

    private static final Logger log = LoggerFactory.getLogger(HttpClientUtil.class);

    private static String COOKIE;

    private HttpClientUtil() {}

    public static HttpClientUtil getInstance() {
        return INSTANCE;
    }

    public HttpClient getClient() {
        return HttpClientBuilder.create().build();
    }

    public HttpResult doPost(String url, Object data) {
        HttpPost post = new HttpPost(url);
        post.setEntity(new StringEntity(JsonUtil.toJsonString(data), "UTF-8"));
        post.setHeader("Content-Type", "application/json;charset=utf8");
        if (COOKIE != null) {
            post.setHeader("cookie", COOKIE);
        }
        try {
            HttpResponse response = getClient().execute(post);
            Header[] headers = response.getHeaders("Set-Cookie");
            if (headers.length != 0) {
                refreshCookie(headers[0]);
            }
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                log.warn("StatusCode[{}]", statusCode);
            }
            String ret = EntityUtils.toString(response.getEntity());
            log.debug(ret);
            return JsonUtil.jsonToObject(ret, HttpResult.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void refreshCookie(Header header) {
        COOKIE = header.getValue();
        log.warn("Refresh cookie:{}", COOKIE);
    }

    public static void main(String[] args) {
        User user = new User("sb", "aa", "aa", "bb");
        HttpResult httpResult = getInstance().doPost(UrlMap.getLoginUrl(), JsonUtil.toJsonString(user));
        System.out.println(httpResult);
        httpResult = getInstance().doPost(UrlMap.getLoginUrl(), JsonUtil.toJsonString(user));
        System.out.println(httpResult);
    }
}
