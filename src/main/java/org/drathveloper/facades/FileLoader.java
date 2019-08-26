package org.drathveloper.facades;

import org.drathveloper.main.App;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FileLoader {

    private static FileLoader instance;

    private final char separator;

    private FileLoader(){
        if(System.getProperty("os.name").toLowerCase().contains("windows")){
            separator = '\\';
        } else {
            separator = '/';
        }
    }

    public static FileLoader getInstance(){
        if(instance==null){
            instance = new FileLoader();
        }
        return instance;
    }

    public Map<String, String> readPropertiesFile(String path) throws FileNotFoundException {
        Map<String, String> readProps = new HashMap<>();
        try {
            FileInputStream fs = new FileInputStream(path);
            Properties props = new Properties();
            props.load(fs);
            for(String key : props.stringPropertyNames()){
                readProps.put(key, props.getProperty(key));
            }
            fs.close();
        } catch (IOException e) {
            throw new FileNotFoundException(e.getMessage());
        }
        return readProps;
    }

    public String getExecutionPath(){
        File jarPath = new File(App.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        return jarPath.getParentFile().getAbsolutePath() + separator;
    }
}
