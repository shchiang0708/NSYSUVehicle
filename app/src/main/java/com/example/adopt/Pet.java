package com.example.adopt;
import android.graphics.Bitmap;

public class Pet {
    private Bitmap imgUrl;
    private String kind;
    private String shelter;

    private String tel; //

    public void setImgUrl(Bitmap imgUrl) {
        this.imgUrl = imgUrl;
    }
    public void setKind(String kind) {
        this.kind = kind;
    }
    public void setShelter(String shelter) {
        this.shelter = shelter;
    }

    public void setTel(String tel) {
        this.tel = tel;
    } //

    public String getKind() {
        return kind;
    }
    public String getShelter() {
        return shelter;
    }
    public Bitmap getImgUrl() {
        return imgUrl;
    }

    public String getTel(){return tel;} //


}

