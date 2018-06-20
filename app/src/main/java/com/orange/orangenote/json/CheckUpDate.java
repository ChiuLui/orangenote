package com.orange.orangenote.json;

import java.util.List;

/**
 * @author 神经大条蕾弟
 * @version 1.0
 * @date 2018/06/17 14:39
 * @copyright 赵蕾
 */

public class CheckUpDate {

    public String name;

    public int version;

    public String versionName;

    public String content;

    public String downloadURL;

    public List<String> marketList;

    public String pageURL;

    public String getPageURL() {
        return pageURL;
    }

    public void setPageURL(String pageURL) {
        this.pageURL = pageURL;
    }

    public List<String> getMarketList() {
        return marketList;
    }

    public void setMarketList(List<String> marketList) {
        this.marketList = marketList;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }
}
