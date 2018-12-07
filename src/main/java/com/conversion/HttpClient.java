package com.conversion;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class HttpClient {


    public String getXmlPayload(String url) throws IOException, IllegalArgumentException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response1 = httpclient.execute(httpGet);
        try {
            System.out.println(response1.getStatusLine());
            HttpEntity entity1 = response1.getEntity();
            if (entity1.getContent() == null) throw new IOException("Cannot locate content stream for URL " + url);
            StringWriter writer = new StringWriter();
            IOUtils.copy(entity1.getContent(), writer, StandardCharsets.UTF_8);
            String result = writer.toString();
            EntityUtils.consume(entity1);
            return result;
        } finally {
            response1.close();
        }
    }

}
