package example;

import httpfailover.FailoverHttpClient;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpFailoverExample {

    public static void main(String[] main) throws IOException {

        // create anf configure FailoverHttpClient just as you would a DefaultHttpClient
        PoolingClientConnectionManager cm = new PoolingClientConnectionManager();
        cm.setDefaultMaxPerRoute(20);
        cm.setMaxTotal(100);
        FailoverHttpClient failoverHttpClient = new FailoverHttpClient(cm);

        final String dockerIp = "192.168.159.139";

        // have ready a list of hosts to try the call
        List<HttpHost> hosts = Arrays.asList(
            new HttpHost(dockerIp, 9090),
            new HttpHost(dockerIp, 9091)
        );

        // create the request
        HttpGet request = new HttpGet(URI.create("/file.txt"));

        // invoke the request on dockerIp:9090 first and dockerIp:9091 if that fails.

        try {
            HttpResponse response = failoverHttpClient.execute(hosts, request);
            String res = EntityUtils.toString(response.getEntity());
            Pattern pattern = Pattern.compile(".*hostname is (\\w*).*", Pattern.DOTALL);
            Matcher m = pattern.matcher(res);
            if (m.matches()) {
            	System.out.println("Responding host was: "  + m.group(1));
            }
        }
        catch(IOException ex) {
            System.err.println("both hosts failed. The last exception is " + ex);
        }
    }

}
