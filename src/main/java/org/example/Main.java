package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;
import org.json.simple.JSONArray;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main {
    public static class justification {
        private String attr;
        private String label;
        private String value;

        public justification(){}
        public justification(String attr, String label, String value) {
            this.attr = attr;
            this.label = label;
            this.value = value;
        }

        public String getAttr() {
            return attr;
        }

        public String getLabel() {
            return label;
        }

        public String getValue() {
            return value;
        }
    }
    public static void main(String[] args) {
        // create Gson instance
        Gson gson = new Gson();
        AutoIDRole[] autoIDRoles = null;
        // create a reader
        //Reader reader = null;
        try {
/*
            // Trust standard CA and those trusted by our custom strategy
            final SSLContext sslcontext = SSLContexts.custom()
                    .loadTrustMaterial((chain, authType) -> {
                        final X509Certificate cert = chain[0];
                        return "CN=httpbin.org".equalsIgnoreCase(cert.getSubjectDN().getName());
                    })
                    .build();
            final SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                    .setSslContext(sslcontext)
                    .build();
            // Allow TLSv1.3 protocol only
            final HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(sslSocketFactory)
                    .setDefaultTlsConfig(TlsConfig.custom()
                            .setHandshakeTimeout(Timeout.ofSeconds(30))
                            .setSupportedProtocols(TLS.V_1_3)
                            .build())
                    .build();

 */
           // CloseableHttpClient client = HttpClients.custom()
           //         .setConnectionManager(cm)
           //         .build();
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost("https://autoidco50.frdpcloud.com/api/authentication/login");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Accept", "application/json");
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            node.put("username", "bob.rodgers@forgerock.com");
            node.put("password", "AutoIDdemo50!");
            String s = node.toString();
            httpPost.setEntity(new StringEntity(s, ContentType.APPLICATION_JSON));
            CloseableHttpResponse response =  client.execute(httpPost);
            InputStream source = response.getEntity().getContent(); //Get the data in the entity
            Reader reader = new InputStreamReader(source);
            AutoIDUser user = new Gson().fromJson(reader,AutoIDUser.class);
            //System.out.println(user.getToken());

            httpPost = new HttpPost("https://autoidco50.frdpcloud.com/api/roles/export");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Authorization","Bearer "+user.getToken());
            mapper = new ObjectMapper();
            node = mapper.createObjectNode();
            node.put("status", "active");
            s = node.toString();
            httpPost.setEntity(new StringEntity(s, ContentType.APPLICATION_JSON));
            response =  client.execute(httpPost);
            String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
            //System.out.println(responseString);
            JsonObject jo = new Gson().fromJson(responseString, JsonObject.class);
            //System.out.println(jo.);
            autoIDRoles = gson.fromJson(jo.getAsJsonArray("roles"),AutoIDRole[].class);
            client.close();
        } catch (IOException ioe ){
            ioe.printStackTrace();
        } /*catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }*/ catch (ParseException e) {
            throw new RuntimeException(e);
        }
        //InputStream is = Main.class.getClassLoader().getResourceAsStream("test.json");
        //reader = Files.newBufferedReader(Paths.get("test.json"));
        //Reader reader = new InputStreamReader(is);
        //AutoIDRole[] autoIDRoles = gson.fromJson(reader,AutoIDRole[].class);
        for (AutoIDRole autoIDRole: autoIDRoles){
            //System.out.println(gson.toJson(autoIDRole));
            ArrayList<justification> justificationArrayList = decode(autoIDRole.getJustifications());
        }
        //reader.close();

    }
    private static ArrayList<justification> decode(ArrayList<String> justifs){
        Iterator<String> iter = justifs.iterator();
        String memRule = null;
        while(iter.hasNext()){
            String justStr = iter.next();
            //System.out.println((justStr));
            String j = decodeJustification(justStr);
            if (memRule == null) {
                memRule = j;
            } else {
                memRule = memRule + " OR " + j;
            }

        }
        System.out.println(memRule);
        return new ArrayList<justification>();
    }
    private static String decodeJustification (String justif){
        String justCondition = null;
        String attrCondition = null;
        String[] justificationArr = justif.split(" ");
        for(int i = 0; i< justificationArr.length; i++){
            String j = justificationArr[i];
            String lenStr = null;
            lenStr = j.substring(0, 2);
            if(!lenStr.startsWith("0")){
                lenStr = "0x"+lenStr;
            } else {
              lenStr = lenStr.replace("0","0x");
            }
            int pos = Integer.decode(lenStr);
            String attr = j.substring(3,3+pos);
            attrCondition = attr + "="+ j.substring(pos+4,j.length());
            //System.out.println("justCondition is "+justCondition + " attrCondition is "+attrCondition);
            if(justCondition == null) {
                justCondition = "("+ attrCondition;
            } else {
                justCondition = justCondition  + " AND " + attrCondition;
            }
        }
        justCondition = justCondition + ")";
        return justCondition;
    }
    private static SSLContext getSSLContext() throws KeyStoreException,
            NoSuchAlgorithmException, CertificateException, IOException, KeyManagementException {
        KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream instream = new FileInputStream(new File("my.keystore"));
        try {
            trustStore.load(instream, "nopassword".toCharArray());
        } finally {
            instream.close();
        }
        return SSLContexts.custom()
                .loadTrustMaterial((Path) trustStore)
                .build();
    }
}