package ru.antebeotcli.antebeotcli_.AnteBeotWeb;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpProxy;
import org.eclipse.jetty.client.ProxyConfiguration;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.dynamic.HttpClientTransportDynamic;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.json.Cookie;
import org.json.JSONObject;
import ru.antebeotcli.antebeotcli_.AppRun;

import java.io.*;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class JsonGetter {
    static final String defServHost = "antebeot.ru";
    static final String defServPort= "8081";
    static final String defServProt = "http";
    static final String defEncoding = "UTF-8";
    static final String defAdditionalPage = "";
    static final String defHTTPProxyHost = null;
    static final int defHTTPProxyPort = 4444;
    static final Boolean defHTTPProxyUse = false;

    static public String fixEncoding(String res) throws UnsupportedEncodingException {
        var fixed = new String(res.getBytes(), defEncoding);
        return fixed;
    }
    private static String ServerAddress = null;
    private static String ServerHost = null;
    private static String ServerPort = null;
    private static String ServerProt = null;
    private static String AdditionalPage = "";

    private static String httpProxyHost = null;
    private static int httpProxyPort = 4444;
    private static boolean httpproxyEnabled = false;
    public static String getServerAddress()
    {
        if (ServerAddress == null) return defServProt+"://"+defServHost+":"+defServPort;
        return ServerAddress;
    }
    public void setServerAddress(String adr)
    {
        ServerAddress = "" + adr; // new string. not by link. for garbage searcher.
    }
    private Properties m_propetries;
    private HttpClient m_client;
    public CookieStore getCookieStore()
    {
        return m_client.getCookieStore();
    }
    public List<HttpCookie> getCookies()
    {
        List<HttpCookie> cookies = getCookieStore().get(URI.create(ServerAddress));
        return cookies;
    }
    public void saveCookies(String path) throws IOException {
        var f = new File(path);
        if (f.exists()) f.delete();
        f.createNewFile();
        var stream = new FileOutputStream(f);
        for(var cookie : getCookies())
        {
            var raw = cookie.toString();
            stream.write(raw.getBytes());
            stream.write('\n');
        }
    }
    public void loadCookies(String path) throws IOException {
        var f = new File(path);
        var stream = new FileInputStream(f);
        var raw = new String(stream.readAllBytes());// not good idea. if big file.
        for(var line : raw.split("\n")){
            var c = line.split("=");
            AddCookie(c[0], c[1]);
        }
    }
    public boolean updateSession() throws ExecutionException, InterruptedException, TimeoutException {
        var r = getUriJSON("/user?w=updateSession");
        return r.getBoolean("result");
    }
    public void AddCookie(String name, String val)
    {
        var uri = URI.create(ServerAddress);
        HttpCookie cookie = new HttpCookie(name, val);
        cookie.setDomain(ServerHost);
        cookie.setPath("/");
        cookie.setMaxAge(TimeUnit.DAYS.toSeconds(1));
        m_client.getCookieStore().add(uri, cookie);
    }
    private void loadSettings()
    {
        this.m_propetries = new Properties();
        //app_settings.properties
        var a = AppRun.class.getResource("app_settings.properties");
        try {
            this.m_propetries.load(a.openStream());

            if (ServerAddress == null) {
                String host;
                String port;
                String prot;
                host = this.m_propetries.getOrDefault("server_host",defServHost).toString();
                port = this.m_propetries.getOrDefault("server_port",defServHost).toString();
                prot = this.m_propetries.getOrDefault("server_prot",defServHost).toString();
                AdditionalPage = this.m_propetries.getOrDefault("server_additional_page", defAdditionalPage).toString();
                httpProxyHost = m_propetries.getOrDefault("httpproxy_host", defHTTPProxyHost).toString();
                httpProxyPort = Integer.valueOf(m_propetries.getOrDefault("httpproxy_port", defHTTPProxyPort).toString());
                httpproxyEnabled = Boolean.valueOf(m_propetries.getOrDefault("usehttpproxy", defHTTPProxyUse).toString());
                ServerAddress = prot+"://"+host+":"+port+AdditionalPage;
                ServerHost = host;
                ServerProt = prot;
                ServerPort = port;
            }

        } catch(IOException e)
        {
            ServerAddress = defServProt+"://"+defServHost+":"+defServPort;
        }

    }
    public byte[] getRaw(String page) throws ExecutionException, InterruptedException, TimeoutException
    {
        var url = getServerAddress() + page;
        // System.out.println(url);
        Request request = m_client.newRequest(url);
        request.method(HttpMethod.GET);
        request.agent("AntebeotCliJavaFXOfficialClient");
        ContentResponse response = request.send();
        var content =  response.getContent();
        return content;
    }
    public String getUriRaw(String page) throws ExecutionException, InterruptedException, TimeoutException {
        var res = new String(getRaw(page));
        //System.out.println("res is: " + res);
        return res;
        //JSONObject(res);
    }
    public JSONObject getUriJSON(String page) throws ExecutionException, InterruptedException, TimeoutException {
        return new JSONObject(getUriRaw(page));
    }
    public JsonGetter() throws Exception {
        loadSettings();
        // for ssl supports.
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        ClientConnector clientConnector = new ClientConnector();
        clientConnector.setSslContextFactory(sslContextFactory);

        m_client = new HttpClient(new HttpClientTransportDynamic(clientConnector));
        m_client.setFollowRedirects(true);
        if (httpproxyEnabled)
        {
            ProxyConfiguration proxyConfig = m_client.getProxyConfiguration();
            var type = m_propetries.getOrDefault("proxytype", "http").toString().trim();
            if (type.equals("http")) {
                org.eclipse.jetty.client.HttpProxy proxy = new HttpProxy(httpProxyHost, httpProxyPort);
                proxyConfig.addProxy(proxy);
            } else if (type.equals("socks4")) {
                org.eclipse.jetty.client.Socks4Proxy proxy = new org.eclipse.jetty.client.Socks4Proxy(httpProxyHost, httpProxyPort);
                proxyConfig.addProxy(proxy);
            } else {
                throw new Exception("Proxy type unsupported");
            }

        }
        m_client.start();
    }
}
