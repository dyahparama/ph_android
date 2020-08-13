package com.mge.pettycash;
import org.json.JSONException;
import org.json.JSONObject;

public class Helper {
  public String strToJSON(String json,String kol) throws JSONException {
    JSONObject jsondata = new JSONObject(json);
    String x = jsondata.getString(kol);
    return x;
  }
}
