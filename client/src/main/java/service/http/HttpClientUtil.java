package service.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import service.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HttpClientUtil {

    public HttpClient getClient() {
        return HttpClientBuilder.create().build();
    }

    public void doPost(String url, JSONObject data) {
        HttpPost post = new HttpPost(url);
        post.setEntity(new StringEntity(data.toString(), "UTF-8"));
        post.setHeader("Content-Type", "application/json;charset=utf8");
        try {
            HttpResponse response = getClient().execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost postMethod = new HttpPost("http://localhost:8080/login");

        JSONObject json = new JSONObject(new User("sb", "aa", "aa", "bb"));

        List<NameValuePair> params = new ArrayList<>();
        postMethod.setEntity(new StringEntity(json.toString(), "UTF-8"));
        postMethod.setHeader("Content-Type", "application/json;charset=utf8");
        HttpResponse response = client.execute(postMethod);
        HttpEntity entity = response.getEntity();
        System.out.println(entity);
        System.out.println(response.getStatusLine().getStatusCode());
    }
}
