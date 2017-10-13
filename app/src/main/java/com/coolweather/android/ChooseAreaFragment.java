package com.coolweather.android;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.HttpHelper;
import com.coolweather.android.util.UrlHelper;
import com.coolweather.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Adminisitrator on 2017/10/12.
 */

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private Button backButton;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter adapter;
    private List<String> listData = new ArrayList<>(); //适配器数据
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private ProgressDialog progressDialog;

    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        backButton = (Button) view.findViewById(R.id.back);
        titleText = (TextView)view.findViewById(R.id.title_text);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter=new ArrayAdapter(getContext(),android.R.layout.simple_list_item_1,listData);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("ChooseAreaFragment", "onItemClick: ");
                if(currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    Log.d("ChooseAreaFragment", "onItemClick: ");
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if(currentLevel ==LEVEL_COUNTY){

                }

            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel ==LEVEL_CITY){
                    queryProvinces();
                }else if(currentLevel ==LEVEL_COUNTY){
                    queryCities();
                }
            }
        });
        queryProvinces();
    }

    /**
     * 优先从数据库中查询省数据，查询不到则从服务器中查询
     */
    private void queryProvinces(){
        backButton.setVisibility(View.GONE);
        titleText.setText("中国");
        provinceList = DataSupport.findAll(Province.class);
        if(provinceList.size()!=0){
            listData.clear();
            for(Province province : provinceList){
                listData.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            Log.d("ChooseAreaFragment", "queryProvinces: ");
            currentLevel = LEVEL_PROVINCE;
        }else{
            requestFromServer(UrlHelper.address_url,LEVEL_PROVINCE);
        }
    }

    /**
     * 优先从数据库中查询城市数据，查询不到则从服务器中查询
     */
    private void queryCities(){
        Log.d("ChooseAreaFragment", "queryCities: ");
        backButton.setVisibility(View.VISIBLE);
        titleText.setText(selectedProvince.getProvinceName());
        cityList = DataSupport.where("provinceid = ?",String.valueOf(selectedProvince.getProvinceId()))
                .find(City.class);
        if(cityList.size() != 0){
            listData.clear();
            for(City city : cityList){
                listData.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else{
            String address = UrlHelper.address_url + "/" + selectedProvince.getProvinceId();
            requestFromServer(address,LEVEL_CITY);
        }


    }

    /**
     * 优先从数据库中查询县数据，查询不到则从服务器中查询
     */
    private void queryCounties(){
        backButton.setVisibility(View.VISIBLE);
        titleText.setText(selectedCity.getCityName());
        countyList = DataSupport.where("cityid=?",String.valueOf(selectedCity.getCityId()))
                .find(County.class);
        if(countyList.size() != 0){
            listData.clear();
            for(County county : countyList){
                listData.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else{
            String address = UrlHelper.address_url + "/" +selectedProvince.getProvinceId()
                    + "/" +selectedCity.getCityId();
            requestFromServer(address,LEVEL_COUNTY);
        }
    }

    /**
     * 从服务器中请求省、市、县数据
     */
    private void requestFromServer(String address, final int type){
        showProgressDialog();
        HttpHelper.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result = false;
                if(type == LEVEL_PROVINCE){
                    result = Utility.handleProvinces(responseText);
                }else if(type == LEVEL_CITY){
                    result = Utility.handleCities(responseText,selectedProvince.getProvinceId());
                }else if(type == LEVEL_COUNTY){
                    result = Utility.handleCounties(responseText,selectedCity.getCityId());
                }
                //response.body().close();
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if(type == LEVEL_PROVINCE){
                                queryProvinces();
                            }else if(type == LEVEL_CITY){
                                queryCities();
                            }else if(type == LEVEL_COUNTY){
                                queryCounties();
                            }
                        }
                    });

                }

            }
        });
    }

    private void showProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载中...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
