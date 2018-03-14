package com.dcits.fpcy.commons.utils;

import java.io.File;  

import java.io.FileInputStream;  

import java.security.KeyStore;  

import javax.net.ssl.SSLContext;  

import org.apache.http.HttpEntity;  

import org.apache.http.client.methods.CloseableHttpResponse;  

import org.apache.http.client.methods.HttpGet;  

import org.apache.http.conn.ssl.SSLContexts;  

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;  

import org.apache.http.impl.client.CloseableHttpClient;  

import org.apache.http.impl.client.HttpClients;  

import org.apache.http.util.EntityUtils;  
public class ClientCustomSSL {
	
	public final static void main(String[] args) throws Exception {  
		  
        KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());  

        //加载证书文件  

        FileInputStream instream = new FileInputStream(new File("D:\\pdf\\jiemi\\1.keystore"));  

        try {  

            trustStore.load(instream, "512082".toCharArray());  

        } finally {  

            instream.close();  

        }  

        SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(trustStore).build();  

        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,  

                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);  

        CloseableHttpClient httpclient = HttpClients.custom()  

                .setSSLSocketFactory(sslsf)  

                .build();  

        try  

        {  
          
            //访问税局
            String url="https://fpcyweb.zjtax.gov.cn/WebQuery/yzmQuery?" +
            		"callback=jQuery110203825870054480329_"+String.valueOf(System.currentTimeMillis())
            		+"&fpdm=3300171130&" +
            		"r=0.7957691287205526&nowtime"+
            "=1512637399250&publickey=AB2AA9A5812E7AA00244A36B5FED2389&_=1512636945683";
            HttpGet httpget = new HttpGet(url);  
            System.out.println("executing request" + httpget.getRequestLine());  

            CloseableHttpResponse response = httpclient.execute(httpget);  

            try {  

                HttpEntity entity = response.getEntity();  

                System.out.println("----------------------------------------");  

                System.out.println(response.getStatusLine());  

                if (entity != null) {  

                    System.out.println(EntityUtils.toString(entity));  

                }  

            } finally {  

                response.close();  

            }  

        } finally {  

            httpclient.close();  

        }  

    }  

}
