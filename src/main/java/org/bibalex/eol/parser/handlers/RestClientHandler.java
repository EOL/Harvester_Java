package org.bibalex.eol.parser.handlers;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.bibalex.eol.parser.handlers.PropertiesHandler;
import org.bibalex.eol.parser.models.HbaseResult;
import org.bibalex.eol.parser.models.Node;
import org.bibalex.eol.parser.models.NodeRecord;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import scala.Int;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Amr Morad
 */
public class RestClientHandler {

    public String doConnection(String uri, Object object) {
        if (!uri.equalsIgnoreCase("")) {

            RestTemplate restTemplate = handleRestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            // Pass the object and the needed headers
            ResponseEntity response = null;
            if (object instanceof NodeRecord) {
                System.out.println("iffffffffffffff");
                HttpEntity<NodeRecord> entity = new HttpEntity<NodeRecord>((NodeRecord) object, headers);
                // Send the request as POST
                response = restTemplate.exchange(uri, HttpMethod.POST, entity, HbaseResult.class);

                if (response.getStatusCode() == HttpStatus.OK) {
                    System.out.println(response.getBody());
                    return ((HbaseResult) response.getBody()).getStatus() + "";
                } else {
                    System.out.println("returned code(" + response.getStatusCode() + ")");
                }

            } else if (object instanceof Node) {
                System.out.println("else iffffffffffffff");
                HttpEntity<Node> entity = new HttpEntity<Node>((Node) object, headers);
                System.out.println("before send post request");
                // Send the request as POST
                try {
                    System.out.println(uri);
                    response = restTemplate.exchange(uri, HttpMethod.POST, entity, Integer.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("can't connect");
                }

                System.out.println(response);

                if (response.getStatusCode() == HttpStatus.OK) {
                    System.out.println(response.getBody());
                    return Integer.toString((Integer) response.getBody());
                } else {
                    System.out.println("returned code(" + response.getStatusCode() + ")");
                }

            }

        } else {
            System.out.println("Empty uri");
        }
        System.out.println("henaaaaaa");
        return "";
    }

    public String deleteTaxon(String uri, NodeRecord nodeRecord) {

        if (!uri.equalsIgnoreCase("")) {

            RestTemplate restTemplate= handleRestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            ResponseEntity response = null;
            Map<String, Integer> params = new HashMap<String, Integer>();
            params.put(PropertiesHandler.getProperty("resourceId"), nodeRecord.getResourceId());
            params.put(PropertiesHandler.getProperty("generatedNodeId"), Integer.valueOf(nodeRecord.getGeneratedNodeId()));

            response = restTemplate.exchange(uri, HttpMethod.GET, null, HbaseResult.class, params);
            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println(response.getBody());
                return ((HbaseResult) response.getBody()).getStatus() + "";
            } else {
                System.out.println("returned code(" + response.getStatusCode() + ")");
            }
        }
        return "";
    }

    public Boolean updateTaxonInNeo4jAncestoryFormat(String uri, ArrayList<Node> nodes) {

        if (!uri.equalsIgnoreCase("")) {

            RestTemplate restTemplate = handleRestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            System.out.println("after header");
            ResponseEntity response = null;
            HttpEntity<ArrayList<Node>> entity = new HttpEntity<ArrayList<Node>>(nodes, headers);
            System.out.println("before send post request");
            // Send the request as POST
            try {
                System.out.println(uri);
                response = restTemplate.exchange(uri, HttpMethod.POST, entity, Boolean.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    System.out.println(response.getBody());
                    return Boolean.valueOf((Boolean) response.getBody());
                } else {
                    System.out.println("returned code(" + response.getStatusCode() + ")");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("can't connect");
            }
        }
        return false;
    }

    public int getPageId(String uri, int generatedNodeId) {

        if (!uri.equalsIgnoreCase("")) {
            
            RestTemplate restTemplate = handleRestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            ResponseEntity response = null;
            Map<String, Integer> params = new HashMap<String, Integer>();
            params.put(PropertiesHandler.getProperty("generatedNodeId"), generatedNodeId);

            response = restTemplate.exchange(uri, HttpMethod.GET, null, Integer.class, params);
            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println(response.getBody());
                return Integer.valueOf((Integer) response.getBody());
            } else {
                System.out.println("returned code(" + response.getStatusCode() + ")");
            }

        }
        return 0;
    }

    private RestTemplate handleRestTemplate(){
        RestTemplate restTemplate;
        if (PropertiesHandler.getProperty("proxyExists").equalsIgnoreCase("true")) {
            System.out.println("gowa el proper");
            restTemplate = handleProxy(PropertiesHandler.getProperty("proxy"),
                    Integer.parseInt(PropertiesHandler.getProperty("port")),
                    PropertiesHandler.getProperty("proxyUserName"),
                    PropertiesHandler.getProperty("password"));
        } else {
            System.out.println("else of proper");
            restTemplate = new RestTemplate();
        }

        //create the json converter
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        List<HttpMessageConverter<?>> list = new ArrayList<HttpMessageConverter<?>>();
        list.add(converter);
        restTemplate.setMessageConverters(list);
        return restTemplate;
    }

    private RestTemplate handleProxy(String proxyUrl, int port, String username, String password) {

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(proxyUrl, port), new UsernamePasswordCredentials(username, password));
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        clientBuilder.useSystemProperties();
        clientBuilder.setProxy(new HttpHost(proxyUrl, port));
        clientBuilder.setDefaultCredentialsProvider(credsProvider);
        clientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
        CloseableHttpClient client = clientBuilder.build();
        //set the HTTP client
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(client);
        return new RestTemplate(factory);
    }
}
