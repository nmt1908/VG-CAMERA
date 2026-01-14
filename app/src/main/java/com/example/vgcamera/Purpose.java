package com.example.vgcamera;

import org.json.JSONException;
import org.json.JSONObject;

public class Purpose {
    public int id;
    public String vi;
    public String en;
    public String cn;

    public Purpose(int id, String vi, String en, String cn) {
        this.id = id;
        this.vi = vi;
        this.en = en;
        this.cn = cn;
    }

    public String getLabel(String lang) {
        if ("vi".equals(lang)) return vi;
        if ("cn".equals(lang)) return cn;
        return en;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("vi", vi);
        o.put("en", en);
        o.put("cn", cn);
        return o;
    }

    // ✅ Parse Purpose from JSON object
    public static Purpose fromJson(JSONObject json) throws JSONException {
        int id = json.getInt("id");
        String vi = json.getString("vi");
        String en = json.getString("en");
        String cn = json.getString("cn");
        return new Purpose(id, vi, en, cn);
    }

    // ✅ Parse List<Purpose> from JSON array
    public static java.util.List<Purpose> listFromJsonArray(org.json.JSONArray jsonArray) throws JSONException {
        java.util.List<Purpose> list = new java.util.ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return list;
    }
}
