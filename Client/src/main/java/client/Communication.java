package client;

import client.localFileHandler.FileWrapper;
import client.security.EncryptedFileWrapper;
import client.security.SecurityHandler;
import client.security.Token;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Communication {
    private Token loginToken;
    private String serverUrl = "https://localhost:8080/operations";
    public void ping(){

    }

    public Token getLoginToken() {
        return loginToken;
    }

    public void setLoginToken(Token loginToken) {
        this.loginToken = loginToken;
    }

    public void register(User user){

        throw new UnsupportedOperationException();
    }

    public void login(User user) {
        System.out.println("Pretending to login...");

    }

    public List<FileWrapper> getFiles(User user){
        RestTemplate restTemplate = restTemplate();

        //make the object
        FileSystemMessage obj = new FileSystemMessage();


        //set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //set entity to send
        HttpEntity entity = new HttpEntity(obj,headers);

        // send
        ResponseEntity<FileSystemMessage> out = restTemplate.exchange("url", HttpMethod.POST, entity
                , FileSystemMessage.class);


        EncryptedFileWrapper[] files = out.getBody().getFiles();

        return SecurityHandler.decryptFileWrappers(Arrays.asList(files));
    }

    /**
     * Sends the staged files to the server
     * @param user the user sending the files
     * @param files the list of files to be sent
     */
    public void putFiles(User user, List<FileWrapper> files){
        RestTemplate restTemp = restTemplate();
        List<EncryptedFileWrapper> list = new ArrayList<>();
        EncryptedFileWrapper enc;
        for(FileWrapper f: files){
            //list.add(SecurityHandler.encryptFileWrapper(f, user.getPublicKey()));
            enc = new EncryptedFileWrapper();
            enc.setFileCreator(f.getFileCreator());
            enc.setFileName(f.getFileName());
            enc.setFileKey("Key".getBytes());
            list.add(enc);
        }

        //TODO : Replace with FileSystemMessage
        restTemp.postForObject(serverUrl+"/upload", list,  ResponseEntity.class);

    }

    public void shareFile(User user, FileWrapper file){
        throw new UnsupportedOperationException();
    }

    /**
     * Generates a rest template for https
     * @return https rest template
     */
    private RestTemplate restTemplate()
    {

        File trustStore = new File("./" + "\\src\\main\\resources\\clienttruststore.jks");

        SSLContextBuilder sslContextBuilder = new SSLContextBuilder();

        try {
            sslContextBuilder.loadTrustMaterial(trustStore, "password".toCharArray());
        } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException e) {
            e.printStackTrace();
        }

        SSLContext sslContext = null;
        try {
            sslContext = sslContextBuilder.build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }


        SSLConnectionSocketFactory socketFactory = null;
        if (sslContext != null) {
            socketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        }

        HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();


        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(factory);

    }
}
