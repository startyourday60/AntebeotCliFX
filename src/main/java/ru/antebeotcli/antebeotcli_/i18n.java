package ru.antebeotcli.antebeotcli_;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;


final public class i18n {
    private Locale m_currentLocale;
    private ResourceBundle m_messages;
    private static final String fileName = "strings";
    private void loadFromOneLang(String lang)
    {
        m_currentLocale = new Locale(lang.split("_")[0], lang.split("_")[1]);
        m_messages =
                ResourceBundle.getBundle("strings",m_currentLocale);
    }
    public i18n(String lang)
    {
        loadFromOneLang(lang);
    }
    public i18n(String language, String country)
    {
        m_currentLocale = new Locale(language, country);
        m_messages =
                ResourceBundle.getBundle("strings",m_currentLocale);
    }
    public String getString(String s)
    {
        try {
            //System.out.println("get s: "+s);
            return m_messages.getString(s);
        } catch(Exception e)
        {
            return s;
        }
    }
    final private String defLang = "ru_RU";
    public i18n()
    {
        String lang;
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream("settings.prop"));
            lang = prop.getOrDefault("userLang",defLang).toString();
        } catch(IOException e)
        {
            lang = defLang;
        }
        loadFromOneLang(lang);
    }
}
