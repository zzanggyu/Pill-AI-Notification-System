package com.example.finalpillapp.PillManagement;

import com.example.finalpillapp.PillInfo.PillInfo;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DrugResponse {

    @SerializedName("body")
    private Body body;

    public Body getBody() {
        return body;
    }

    public class Body {
        @SerializedName("items")
        private List<PillInfo> items;

        public List<PillInfo> getItems() {
            return items;
        }
    }
}
