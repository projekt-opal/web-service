package org.dice_research.opal.webservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "theme")
@PropertySource("themes.properties")
public class ThemeConfiguration {
    private List<String> uri;
    private List<String> value;

    private Map<String, String> map = new HashMap<>();
    private Map<String, String> revMap = new HashMap<>();

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    public Map<String, String> getRevMap() {
        return revMap;
    }

    public void setRevMap(Map<String, String> revMap) {
        this.revMap = revMap;
    }

    @PostConstruct
    public void assignMappings() {
        int len = Math.min(uri.size(), value.size());
        for (int i = 0; i < len; i++) {
            map.put(uri.get(i), value.get(i));
            revMap.put(value.get(i), uri.get(i));
        }
    }

    public List<String> getUri() {
        return uri;
    }

    public void setUri(List<String> uri) {
        this.uri = uri;
    }

    public List<String> getValue() {
        return value;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }


}
