package de.schalter.sermononline.parser;

import com.google.android.gms.ads.internal.gmsg.HttpClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import de.schalter.sermononline.fragments.SearchFragment;

/**
 * Created by martin on 30.11.17.
 */

public abstract class JsoupParser {

    protected String html;
    protected String url;

    /**
     * Connect to the given url and download the html
     *
     * @param urlString url as String
     * @throws IOException when the url is not an URL
     */
    public void connect(String urlString) throws IOException {
        this.url = urlString;

//        Document doc = Jsoup.connect(urlString).get();
//        html = doc.html();

        trustEveryone();
        URL url = new URL(urlString);
        HttpsURLConnection ucon = (HttpsURLConnection) url.openConnection();

        ucon.setRequestProperty("User-Agent", "Mozilla/5.0...");

        InputStream is = ucon.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is);

        /*
         * Read bytes to the Buffer until there is nothing more to read(-1).
         */
        byte[] contents = new byte[1024];

        int bytesRead;
        StringBuilder stringBuilder = new StringBuilder();
        while ((bytesRead = bis.read(contents)) != -1) {
            stringBuilder.append(new String(contents, 0, bytesRead));
        }

        html = stringBuilder.toString();
    }

    private void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }});
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }

    /**
     * parse the downloaded html file
     *
     * @throws NoDataFoundException when the downloaded html is empty (or not downloaded) or there are no results
     */
    public abstract void parse() throws NoDataFoundException;

}
