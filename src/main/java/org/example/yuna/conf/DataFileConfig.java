package org.example.yuna.conf;

import cn.hutool.setting.Setting;

// config file setting
public class DataFileConfig {
    private static Setting setting;

    static {
        setting = new Setting("Config.setting");
    }
    public static String getSetting(String key){
        return setting.getByGroup(key,"windows");

    }
}
