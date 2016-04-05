package support;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ResourceFetcher {

    public static String resource(String spec) throws Exception {
        URL url = new URL( spec );
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        InputStream inputStream = connection.getInputStream();
        byte[] response = new byte[ inputStream.available() ];
        inputStream.read(response);

        return new String(response);
    }
}
