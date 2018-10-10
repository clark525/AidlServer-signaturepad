package com.hangc.signaturepad.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class SignBean implements Parcelable {
    private String imgPath;

    protected SignBean(Parcel in) {
        imgPath = in.readString();
    }

    public static final Creator<SignBean> CREATOR = new Creator<SignBean>() {
        @Override
        public SignBean createFromParcel(Parcel in) {
            return new SignBean(in);
        }

        @Override
        public SignBean[] newArray(int size) {
            return new SignBean[size];
        }
    };

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(imgPath);
    }
}
