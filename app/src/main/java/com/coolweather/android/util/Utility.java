package com.coolweather.android.util;

import android.text.TextUtils;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Adminisitrator on 2017/10/12.
 */

public class Utility {

    /**
     * 用于解析服务器返回的省数据，并保存到数据库中
     * @param response
     */
    public static boolean handleProvinces(String response){
        if(!TextUtils.isEmpty(response)) {
            try {
                JSONArray allprovinces = new JSONArray(response);
                for (int i = 0; i < allprovinces.length(); i++) {
                    JSONObject cityObject = allprovinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(cityObject.getString("name"));
                    province.setProvinceId(cityObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 用于解析服务器返回的城市数据，并保存到数据库中
     * @param response
     * @param provinceId
     */
    public static boolean handleCities(String response,int provinceId){
        if(!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityId(cityObject.getInt("id"));
                    city.setCityName(cityObject.getString("name"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;

    }

    /**
     * 用于解析服务器返回的县数据，并保存到数据库中
     * @param reponse
     * @param cityId
     */
    public static boolean handleCounties(String reponse,int cityId){
        if(!TextUtils.isEmpty(reponse)){
            try{
                JSONArray allCounties = new JSONArray(reponse);
                for(int i = 0;i < allCounties.length();i++){
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCountyName(countyObject.getString("name"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        return false;

    }

    /**
     * 将返回的JSON数据解析成Weather实体类
     */
    public static Weather handleWeatherResponse(String response){
        try{
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather5");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
