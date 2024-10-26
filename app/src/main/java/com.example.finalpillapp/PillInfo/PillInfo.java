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
    public PillInfo() {
    }

    // 모든 필드를 포함하는 생성자
    public PillInfo(String itemSeq, String itemName, String companyName, String efficacy,
                    String usage, String precautionsWarning, String precautions,
                    String interactions, String sideEffects, String storage,
                    String imageUrl, String printFront, String printBack,
                    String color, String shape, String etcotc, String entpName,
                    String efcyQesitm, String useMethodQesitm, String atpnQesitm, String depositMethodQesitm) {
        this.itemSeq = itemSeq;
        this.itemName = itemName;
        this.companyName = companyName;
        this.efficacy = efficacy;
        this.usage = usage;
        this.precautionsWarning = precautionsWarning;
        this.precautions = precautions;
        this.interactions = interactions;
        this.sideEffects = sideEffects;
        this.storage = storage;
        this.imageUrl = imageUrl;
        this.printFront = printFront;
        this.printBack = printBack;
        this.color = color;
        this.shape = shape;
        this.etcotc = etcotc;
        this.entpName = entpName;
        this.efcyQesitm = efcyQesitm;
        this.useMethodQesitm = useMethodQesitm;
        this.atpnQesitm = atpnQesitm;
        this.depositMethodQesitm = depositMethodQesitm;
    }

    // Parcelable 인터페이스를 위한 생성자
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

    // Parcelable 인터페이스 메소드
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
    public int describeContents() {
        return 0;
    }

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

    // Getter 및 Setter 메서드 정의
    // 모든 필드에 대해 getter/setter 메서드 제공

    public String getItemSeq() {
        return itemSeq;
    }

    public void setItemSeq(String itemSeq) {
        this.itemSeq = itemSeq;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEfficacy() {
        return efficacy;
    }

    public void setEfficacy(String efficacy) {
        this.efficacy = efficacy;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String getPrecautionsWarning() {
        return precautionsWarning;
    }

    public void setPrecautionsWarning(String precautionsWarning) {
        this.precautionsWarning = precautionsWarning;
    }

    public String getPrecautions() {
        return precautions;
    }

    public void setPrecautions(String precautions) {
        this.precautions = precautions;
    }

    public String getInteractions() {
        return interactions;
    }

    public void setInteractions(String interactions) {
        this.interactions = interactions;
    }

    public String getSideEffects() {
        return sideEffects;
    }

    public void setSideEffects(String sideEffects) {
        this.sideEffects = sideEffects;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPrintFront() {
        return printFront;
    }

    public void setPrintFront(String printFront) {
        this.printFront = printFront;
    }

    public String getPrintBack() {
        return printBack;
    }

    public void setPrintBack(String printBack) {
        this.printBack = printBack;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public String getEtcotc() {
        return etcotc;
    }

    public void setEtcotc(String etcotc) {
        this.etcotc = etcotc;
    }

    public String getEntpName() {
        return entpName;
    }

    public void setEntpName(String entpName) {
        this.entpName = entpName;
    }

    public String getEfcyQesitm() {
        return efcyQesitm;
    }

    public void setEfcyQesitm(String efcyQesitm) {
        this.efcyQesitm = efcyQesitm;
    }

    public String getUseMethodQesitm() {
        return useMethodQesitm;
    }

    public void setUseMethodQesitm(String useMethodQesitm) {
        this.useMethodQesitm = useMethodQesitm;
    }

    public String getAtpnQesitm() {
        return atpnQesitm;
    }

    public void setAtpnQesitm(String atpnQesitm) {
        this.atpnQesitm = atpnQesitm;
    }

    public String getDepositMethodQesitm() {
        return depositMethodQesitm;
    }

    public void setDepositMethodQesitm(String depositMethodQesitm) {
        this.depositMethodQesitm = depositMethodQesitm;
    }

    public String getItemImage() {
        return imageUrl;
    }

    public void setItemImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSeQesitm() {
        return sideEffects; // 부작용 정보를 반환하는 메서드
    }

}
