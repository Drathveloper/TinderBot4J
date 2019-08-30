package org.drathveloper.facades;


import org.apache.http.Header;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.drathveloper.exceptions.HttpGenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;

public class HttpClientFacade {

    private final Logger logger = LoggerFactory.getLogger(HttpClientFacade.class);

    private static HttpClientFacade instance;

    private CloseableHttpClient client;

    private HttpClientFacade(){
        client = HttpClients.createDefault();
    }

    public static HttpClientFacade getInstance(){
        if(instance == null){
            instance = new HttpClientFacade();
        }
        return instance;
    }

    public String executeGet(String url, Map<String, String> headers) throws HttpGenericException {
        RequestBuilder builder = RequestBuilder.get(url);
        this.addHeaders(builder, headers);
        HttpUriRequest request = builder.build();
        return this.execute(request);
    }

    public String executePost(String url, Map<String, String> headers, String body) throws HttpGenericException {
        body = (body != null ? body : "");
        String response;
        try {
            RequestBuilder builder = RequestBuilder.post(url).setEntity(new StringEntity(body));
            this.addHeaders(builder, headers);
            HttpUriRequest request = builder.build();
            response = this.execute(request);
        } catch (UnsupportedEncodingException ex){
            response = null;
        }
        return response;
    }

    private String execute(HttpUriRequest request) throws HttpGenericException {
        String parsedResponse = null;
        CloseableHttpResponse response = null;
        try {
            logger.debug("Started request");
            long start = new Date().getTime();
            response = client.execute(request);
            long end = new Date().getTime();
            logger.debug("Finished request");
            long ellapsedTime = end - start;
            logger.debug("Execution time: " + ellapsedTime + " ms");
            if(response.getStatusLine().getStatusCode()!=200){
                this.releaseResponse(response);
                throw new HttpGenericException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            }
            parsedResponse = this.processResponse(this.parseResponse(response));
        } catch (IOException e) {
            if (response != null) {
                this.printResponseDetails(response);
            }
            logger.debug("[" +   this.getClass().getSimpleName() + "] Exception while calling the endpoint: " + request.getURI() + "\n" +
                    "Message: " + e.getMessage());
        }
        return parsedResponse;
    }

    private void addHeaders(RequestBuilder builder, Map<String, String> headers){
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
    }

    private String parseResponse(CloseableHttpResponse response) throws NullPointerException, IOException {
        String parsedResponse;
        ResponseHandler<String> handler = new BasicResponseHandler();
        parsedResponse = handler.handleResponse(response);
        parsedResponse = parsedResponse != null ? parsedResponse : "";
        return parsedResponse;
    }

    private void releaseResponse(CloseableHttpResponse response) throws IOException{
        EntityUtils.consume(response.getEntity());
        response.close();
    }

    private void printResponseDetails(CloseableHttpResponse response){
        Header[] headers = response.getAllHeaders();
        logger.debug("Reason: " + response.getStatusLine().getReasonPhrase() + "\nCode: " + response.getStatusLine().getStatusCode());
        for(Header h : headers){
            logger.debug(h.getName() + ": " + h.getValue());
        }
    }

    private String processResponse(String response){
        if(response == null){
            return "";
        }
        return response;
    }

}
