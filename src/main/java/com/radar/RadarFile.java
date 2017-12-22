package com.radar;

public class RadarFile {
    RadarHeadfile headfile;
    String imgUrl;

    public RadarHeadfile getHeadfile() {
        return headfile;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setHeadfile(RadarHeadfile headfile) {
        this.headfile = headfile;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
