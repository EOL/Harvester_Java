package com.bibalex.taxonmatcher.handlers;

import com.bibalex.taxonmatcher.models.Node;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.bibalex.eol.handler.PropertiesHandler;
//import org.bibalex.eol.parser.models.HbaseResult;
//import org.bibalex.eol.parser.models.Node;
//import org.bibalex.eol.parser.models.NodeRecord;

/**
 * Created by Amr Morad
 */
public class RestClientHandler {
    public static Object doConnectionGet(String uri, Object object_1,Object param_1,Object object_2,Object param_2){
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity response = null;
        if(!uri.equalsIgnoreCase("")) {
            restTemplate.setMessageConverters(createJsonConverter());
            HttpHeaders headers = setHeaders();

            Map<String, String> params = new HashMap<String, String>();
            if(param_1!=null) {params.put(ResourceHandler.getPropertyValue(String.valueOf(param_1)), String.valueOf(object_1));}
            if (param_2!=null){params.put(ResourceHandler.getPropertyValue(String.valueOf(param_2)), String.valueOf(object_2));}

            HttpEntity<String> entity = new HttpEntity<String>(headers);
            response = restTemplate.exchange(uri,HttpMethod.GET, entity,Object.class, params);


                if (response.getStatusCode() == HttpStatus.OK) {
//                    System.out.println(response.getBody());
                    return response.getBody();
                } else {
                    System.out.println("returned code(" + response.getStatusCode() + ")");
                }


        }else{
            System.out.println("Empty uri");
        }
        return "";
    }

    public static Object doConnectionPost(String uri, Object object_1) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity response = null;
        if (!uri.equalsIgnoreCase("")) {

            restTemplate.setMessageConverters(createJsonConverter());
            HttpHeaders headers = setHeaders();
            HttpEntity<Object> entity = new HttpEntity<Object>(object_1,headers);
            response = restTemplate.exchange(uri, HttpMethod.POST, entity,Object.class);


            if (response.getStatusCode() == HttpStatus.OK) {
//                    System.out.println(response.getBody());
                return response.getBody();
            } else {
                System.out.println("returned code(" + response.getStatusCode() + ")");
            }

        } else {
            System.out.println("Empty uri");
        }
        return "";
    }

    public static List<HttpMessageConverter<?>> createJsonConverter()
    {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        List<HttpMessageConverter<?>> list = new ArrayList<HttpMessageConverter<?>>();
        list.add(converter);
        return list;
    }

    public static HttpHeaders setHeaders()
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");
        return headers ;
    }

}
