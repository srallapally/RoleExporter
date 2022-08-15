package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Iterator;

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
        try {
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
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

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