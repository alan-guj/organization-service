package top.jyx365.organizationService;


import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.context.annotation.PropertySource;

import org.springframework.test.annotation.ProfileValueSource;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
public class Configuration implements ProfileValueSource {

    private Map<String ,String> groups = new HashMap<String, String>();

    public Configuration() {
        groups.put("dept-test-group", "update-role");
    }

    public String get(String key) {
        String ret = System.getProperty(key);
        if(ret != null) return ret;
        return groups.get(key);

    }
}


