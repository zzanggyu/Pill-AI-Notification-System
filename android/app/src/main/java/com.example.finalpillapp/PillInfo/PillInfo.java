// PillInfo.java
package com.example.finalpillapp.PillInfo;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class PillInfo implements Parcelable {

    @SerializedName("item_seq")
    private String itemSeq;

    @SerializedName("item_name")
    private String itemName;

    @SerializedName("company_name")
    private String companyName;

    @SerializedName("efficacy")
    private String efficacy;

    @SerializedName("usage")
    private String usage;

    @SerializedName("precautions_warning")
    private String precautionsWarning;

    @SerializedName("precautions")
    private String precautions;

    @SerializedName("interactions")
    private String interactions;

    @SerializedName("side_effects")
    private String sideEffects;

    @SerializedName("storage")
    private String storage;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("print_front")
    private String printFront;

    @SerializedName("print_back")
    private String printBack;

    @SerializedName("color")
    private String color;

    @SerializedName("shape")
    private String shape;

    @SerializedName("etcotc")
    private String etcotc;

    @SerializedName("entpName")
    private String entpName;

    @SerializedName("efcyQesitm")
    private String efcyQesitm;

    @SerializedName("useMethodQesitm")
    private String useMethodQesitm;

    @SerializedName("atpnQesitm")
    private String atpnQesitm;

    @SerializedName("depositMethodQesitm")
    private String depositMethodQesitm;

    // 기본 생성자
    public PillInfo() {}

    // Parcelable 인터페이스 구현
    protected PillInfo(Parcel in) {
        itemSeq = in.readString();
        itemName = in.readString();
        companyName = in.readString();
        efficacy = in.readString();
        usage = in.readString();
        precautionsWarning = in.readString();
        precautions = in.readString();
        interactions = in.readString();
        sideEffects = in.readString();
        storage = in.readString();
        imageUrl = in.readString();
        printFront = in.readString();
        printBack = in.readString();
        color = in.readString();
        shape = in.readString();
        etcotc = in.readString();
        entpName = in.readString();
        efcyQesitm = in.readString();
        useMethodQesitm = in.readString();
        atpnQesitm = in.readString();
        depositMethodQesitm = in.readString();
    }

    public static final Creator<PillInfo> CREATOR = new Creator<PillInfo>() {
        @Override
        public PillInfo createFromParcel(Parcel in) {
            return new PillInfo(in);
        }

        @Override
        public PillInfo[] newArray(int size) {
            return new PillInfo[size];
        }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(itemSeq);
        dest.writeString(itemName);
        dest.writeString(companyName);
        dest.writeString(efficacy);
        dest.writeString(usage);
        dest.writeString(precautionsWarning);
        dest.writeString(precautions);
        dest.writeString(interactions);
        dest.writeString(sideEffects);
        dest.writeString(storage);
        dest.writeString(imageUrl);
        dest.writeString(printFront);
        dest.writeString(printBack);
        dest.writeString(color);
        dest.writeString(shape);
        dest.writeString(etcotc);
        dest.writeString(entpName);
        dest.writeString(efcyQesitm);
        dest.writeString(useMethodQesitm);
        dest.writeString(atpnQesitm);
        dest.writeString(depositMethodQesitm);
    }

    // Getter 및 Setter 메서드 (중복 제거 및 이름 명확화)
    public String getItemSeq() { return itemSeq; }
    public String getItemName() { return itemName; }
    public String getCompanyName() { return companyName; }
    public String getEfficacy() { return efficacy; }
    public String getUsage() { return usage; }
    public String getPrecautionsWarning() { return precautionsWarning; }
    public String getPrecautions() { return precautions; }
    public String getInteractions() { return interactions; }
    public String getSideEffects() { return sideEffects; }
    public String getStorage() { return storage; }
    public String getImageUrl() { return imageUrl; }
    public String getPrintFront() { return printFront; }
    public String getPrintBack() { return printBack; }
    public String getColor() { return color; }
    public String getShape() { return shape; }
    public String getEtcotc() { return etcotc; }
    public String getEntpName() { return entpName; }
    public String getEfcyQesitm() { return efcyQesitm; }
    public String getUseMethodQesitm() { return useMethodQesitm; }
    public String getAtpnQesitm() { return atpnQesitm; }
    public String getDepositMethodQesitm() { return depositMethodQesitm; }
}
