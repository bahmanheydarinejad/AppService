package ir.appservice_v1.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class AppIcons implements Serializable {

    private final Logger logger = LoggerFactory.getLogger(AppIcons.class);

    private List<Icon> icons;

    public AppIcons() {
        init();
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public void init() {
        icons = new ArrayList<>();

        String url = "https://raw.githubusercontent.com/primefaces/primeicons/99d7fd02312a0386df891062775f0b479b7c8d13/selection.json";
        try {
            JSONObject json = readJsonFromUrl(url);
            JSONArray iconsArray = json.getJSONArray("icons");
            for (int i = 0; i < iconsArray.length(); i++) {
                JSONObject properties = iconsArray.optJSONObject(i).getJSONObject("properties");
                icons.add(new Icon(properties.getString("name"), properties.getInt("code")));
            }
        } catch (IOException | JSONException ex) {
            ex.printStackTrace();
        }
    }

    public JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    public List<Icon> getIcons() {
        return icons;
    }

    public void setIcons(List<Icon> icons) {
        this.icons = icons;
    }

    public class Icon {

        private String name;
        private int key;

        public Icon(String name, int key) {
            this.name = name;
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getKey() {
            return key;
        }

        public void setKey(int key) {
            this.key = key;
        }
    }
}
