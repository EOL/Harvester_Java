package org.bibalex.eol.harvester;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.bibalex.eol.parser.handlers.PropertiesHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.*;

public class StorageLayerClient {

    private static final Logger logger = LoggerFactory.getLogger(StorageLayerClient.class);

    public StorageLayerClient() throws IOException {
    }

    public String getPath(int resourceId){
        return PropertiesHandler.getProperty("storage.output.directory") + File.separator + resourceId + "_org";
    }

    public static void downloadResource(String resId, String isOrg) throws IOException {
        logger.debug("Downloading resource into harvester: " + resId);
        final String uri = PropertiesHandler.getProperty("storage.layer.api.url") +
                PropertiesHandler.getProperty("download.resource.url");

        RestTemplate restTemplate;
        if(PropertiesHandler.getProperty("proxyExists").equalsIgnoreCase("true")) {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();

            String proxyUrl = PropertiesHandler.getProperty("proxy");
            System.out.println(proxyUrl);

            int port = Integer.parseInt(PropertiesHandler.getProperty("port"));
            System.out.println(port);


            credsProvider.setCredentials(new AuthScope(proxyUrl, port),
                    new UsernamePasswordCredentials(PropertiesHandler.getProperty("proxyUserName"),
                            PropertiesHandler.getProperty("password")));

            HttpClientBuilder clientBuilder = HttpClientBuilder.create();
            clientBuilder.useSystemProperties();
            clientBuilder.setProxy(new HttpHost(proxyUrl, port));
            clientBuilder.setDefaultCredentialsProvider(credsProvider);
            clientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
            CloseableHttpClient client = clientBuilder.build();

            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
            factory.setHttpClient(client);
            restTemplate = new RestTemplate(factory);
        } else {
            restTemplate = new RestTemplate();
        }
        System.out.println("done proxy");
        restTemplate.getMessageConverters().add(
                new ByteArrayHttpMessageConverter());

        Map<String, String> params = new HashMap<String, String>();
        params.put(PropertiesHandler.getProperty("download.var1"), resId);
        params.put(PropertiesHandler.getProperty("download.var2"), isOrg);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));

        HttpEntity<String> entity = new HttpEntity<String>(headers);
        System.out.println("before send request");
        System.out.println(uri);
        System.out.println(params);
        ResponseEntity<byte[]> response = restTemplate.exchange(
                uri,
                HttpMethod.GET, entity, byte[].class, params);
        System.out.println("after request");
        HttpHeaders outHeaders = response.getHeaders();
        System.out.println(outHeaders.get("Content-disposition").size() + "_----------------------");
        String fileName = "";
        if(outHeaders.get("Content-disposition").size() > 0) {
            fileName = outHeaders.get("Content-disposition").get(0);
            fileName = fileName.substring(fileName.indexOf("=") + 1);
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            System.out.println("creating file");
            FileOutputStream fos = new FileOutputStream(PropertiesHandler.getProperty
                    ("storage.output.directory") + File.separator + resId + "_" +
                    (isOrg.equalsIgnoreCase("1")? "org" : "core"));
            fos.write(response.getBody());
            fos.close();
        } else {
            logger.error("org.bibalex.eol.harvester.client.StorageLayerClient.downloadResource: returned code(" + response.getStatusCode() + ")");
        }

    }

    public static void uploadDWCAResource(String resId, String fileName) throws IOException {
        logger.debug("Uploading DWCA resource (" + resId + ") into SL.");
        final String uri = PropertiesHandler.getProperty("storage.layer.api.url") +
                PropertiesHandler.getProperty("upload.resource.url");

        RestTemplate restTemplate;
        if(PropertiesHandler.getProperty("proxyExists").equalsIgnoreCase("true")) {

            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            String proxyUrl = PropertiesHandler.getProperty("proxy");
            int port = Integer.parseInt(PropertiesHandler.getProperty("port"));

            credsProvider.setCredentials(new AuthScope(proxyUrl, port),
                    new UsernamePasswordCredentials(PropertiesHandler.getProperty("proxyUserName"),
                            PropertiesHandler.getProperty("password")));

            HttpClientBuilder clientBuilder = HttpClientBuilder.create();
            clientBuilder.useSystemProperties();
            clientBuilder.setProxy(new HttpHost(proxyUrl, port));
            clientBuilder.setDefaultCredentialsProvider(credsProvider);
            clientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
            CloseableHttpClient client = clientBuilder.build();

            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
            factory.setHttpClient(client);
            restTemplate = new RestTemplate(factory);
        } else {
            restTemplate = new RestTemplate();
        }

//        restTemplate.getMessageConverters().add(
//                new ByteArrayHttpMessageConverter());

        Map<String, String> params = new HashMap<String, String>();
        System.out.println("before params");
        params.put(PropertiesHandler.getProperty("upload.var1"), resId);
        params.put(PropertiesHandler.getProperty("upload.var2"), "0");


        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file",new FileSystemResource(PropertiesHandler.getProperty("storage.dwca.directory") + File.separator + fileName));


//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data"); // we are sending a form
        headers.set("Accept", "text/plain"); // looks like you want a string back


        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
                new HttpEntity<LinkedMultiValueMap<String, Object>>(map, headers);

        System.out.println("before send request");
        ResponseEntity<String> response = restTemplate.exchange(
                uri,
                HttpMethod.POST, requestEntity, String.class, params);


        if (response.getStatusCode() == HttpStatus.OK) {
            logger.debug("Uplaoded DWCA resource (" + resId + ")");
        } else {
            logger.error("org.bibalex.eol.harvester.client.StorageLayerClient.uploadDWCAResource: returned code(" + response.getStatusCode() + ")");
        }

    }

    public void downloadMedia(String resId, ArrayList<String> mediaFiles) throws IOException {
        logger.debug("Request to storage layer to download media of resource (" + resId + ").");
        final String uri = PropertiesHandler.getProperty("storage.layer.api.url") + PropertiesHandler.getProperty("media.resource.url");


//        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
//        Proxy proxy= new Proxy(Proxy.Type.HTTP, new InetSocketAddress("my.host.com", 8080));
//        requestFactory.setProxy(proxy);


        RestTemplate restTemplate;
        if(PropertiesHandler.getProperty("proxyExists").equalsIgnoreCase("true")) {

            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            String proxyUrl = PropertiesHandler.getProperty("proxy");
            int port = Integer.parseInt(PropertiesHandler.getProperty("port"));

            credsProvider.setCredentials(new AuthScope(proxyUrl, port),
                    new UsernamePasswordCredentials(PropertiesHandler.getProperty("proxyUserName"),
                            PropertiesHandler.getProperty("password")));

            HttpClientBuilder clientBuilder = HttpClientBuilder.create();
            clientBuilder.useSystemProperties();
            clientBuilder.setProxy(new HttpHost(proxyUrl, port));
            clientBuilder.setDefaultCredentialsProvider(credsProvider);
            clientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
            CloseableHttpClient client = clientBuilder.build();

            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
            factory.setHttpClient(client);
            restTemplate = new RestTemplate(factory);
        } else {
            restTemplate = new RestTemplate();
        }

        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        Map<String, String> params = new HashMap<String, String>();
        params.put(PropertiesHandler.getProperty("media.var1"), resId);


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json"); // looks like you want a string back

        String mediaJson = getJson(mediaFiles);
        HttpEntity<String> entity = new HttpEntity<String>(mediaJson, headers);


        ResponseEntity<String> response = restTemplate.exchange(
                uri,
                HttpMethod.POST, entity, String.class, params);


        if (response.getStatusCode() == HttpStatus.OK) {
            logger.debug("Uplaoded DWCA resource (" + resId + ")");
            System.out.println(response.getBody());
        } else {
            logger.error("org.bibalex.eol.harvester.client.StorageLayerClient.uploadDWCAResource:" +
                    " returned code(" + response.getStatusCode() + ")");
        }
    }

    private String getJson(ArrayList<String> mediaFiles) {
        String out = "[";
        for(int i = 0; i < mediaFiles.size(); i++) {
            out += "\"" + mediaFiles.get(i) + "\"";
            if((i + 1) != mediaFiles.size()) {
                out += ",";
            }
        }
        return out + "]";
    }

    public static void main(String[] args) throws IOException {
        StorageLayerClient client = new StorageLayerClient();
        client.downloadResource("100", "1");
//        client.uploadDWCAResource("107", "3.tar.gz");
//        ArrayList<String> urls = new ArrayList<String>();
//        urls.add("https://download.quranicaudio.com/quran/abdullaah_3awwaad_al-juhaynee/033.mp3");
//        urls.add("https://download.quranicaudio.com/quran/abdullaah_3awwaad_al-juhaynee/031.mp3");
//        client.downloadMedia("105", urls);
    }

}