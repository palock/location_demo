package com.android.demo;
 
import java.io.BufferedReader;
import java.io.InputStreamReader;
 
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
 
import org.json.JSONArray;
import org.json.JSONObject;
 
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;
 
public class DemoActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
 
        /** 为按钮绑定事件 */
        Button btnGetLocation = (Button) findViewById(R.id.button1);
        btnGetLocation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                onBtnClick();
            }
        });
    }
     
    /** 基站信息结构体 */
    public class SCell{
        public int MCC;
        public int MNC;
        public int LAC;
        public int CID;
    }
     
    /** 经纬度信息结构体 */
    public class SItude{
        public String latitude;
        public String longitude;
    }
     
    /** 按钮点击回调函数 */
    private void onBtnClick() {
        /** 弹出一个等待状态的框 */
        ProgressDialog mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("正在获取中...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.show();
 
        try {
            /** 获取基站数据 */
            SCell cell = getCellInfo();

            /** 根据基站数据获取经纬度 */
            SItude itude = getItude(cell);
 
            /** 获取地理位置 */
            String location = getLocation(itude);
 
            /** 显示结果 */
            showResult(cell, location);
 
            /** 关闭对话框 */
            mProgressDialog.dismiss();
        } catch (Exception e) {
            /** 关闭对话框 */
            mProgressDialog.dismiss();
            /** 显示错误 */
            TextView cellText = (TextView) findViewById(R.id.cellText);
            cellText.setText(e.getMessage());
            Log.e("Error", e.getMessage());
        }
    }
     
    /**
     * 获取基站信息
     *
     * @throws Exception
     */
    private SCell getCellInfo() throws Exception {
        SCell cell = new SCell();
 
        /** 调用API获取基站信息 */
        TelephonyManager mTelNet = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation location = (GsmCellLocation) mTelNet.getCellLocation();
        if (location == null)
            throw new Exception("获取基站信息失败");
 
        String operator = mTelNet.getNetworkOperator();
        int mcc = Integer.parseInt(operator.substring(0, 3));
        int mnc = Integer.parseInt(operator.substring(3));
        int cid = location.getCid();
        int lac = location.getLac();
 
        /** 将获得的数据放到结构体中 */
        cell.MCC = mcc;
        cell.MNC = mnc;
        cell.LAC = lac;
        cell.CID = cid;

        return cell;
    }
     
    /**
     * 获取经纬度
     *
     * @throws Exception
     */
    private SItude getItude(SCell cell) throws Exception {
        SItude itude = new SItude();
     	
        
//        http://v.juhe.cn/cell/get?dtype=json&hex=10&mnc=0&cell=10174&lac=4335&key=ebfafe8efd1eb6ec36007bb4864908ff
//        	 mnc 	 int 	 是 	 移动基站：0 联通基站:1 默认:0
//        	 lac 	 int 	 是 	 小区号
//        	 cell 	 int 	 是 	 基站号
//        	 hex 	 INT 	 否 	 进制类型，16或10，默认：10
//        	 dtype 	 string 	 否 	 返回的数据格式：json/xml/jsonp
//        	 callback 	 string 	 否 	 当选择jsonp格式时必须传递
//        	 key 	 string 	 是 	 APPKEY
        
        
        /** 采用Android默认的HttpClient */
        HttpClient client = new DefaultHttpClient();
        /** 采用GET方法 */
        String url = "http://v.juhe.cn/cell/get?dtype=json&hex=10&mnc=0&cell="+cell.CID+"&lac="+cell.LAC+"&key=ebfafe8efd1eb6ec36007bb4864908ff";
        HttpGet httpGet = new HttpGet(url);
        try {
            /** 获取返回数据 */
            HttpResponse response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            BufferedReader buffReader = new BufferedReader(new InputStreamReader(entity.getContent()));
            StringBuffer strBuff = new StringBuffer();
            String result = null;
            while ((result = buffReader.readLine()) != null) {
                strBuff.append(result);
            }
 
            /** 解析返回的JSON数据获得经纬度 */
// 字段 	 类型 	 说明
// resultcode 	 int 	 返回码
// reason 	 string 	 返回说明
// data 	 - 	 返回结果集
// 　　LAC 	 string 	 小区号
// 　　CELL 	 string 	 基站号
// 　　LNG 	 string 	 纬度
// 　　LAT 	 string 	 经度
// 　　O_LNG 	 string 	 纠偏后的纬度（用于google地图显示）
// 　　O_LAT 	 string 	 纠偏后的经度（用于google地图显示）
// 　　PRECISION 	 string 	 基站信号覆盖范围（单位：米，半径）
// 　　ADDRESS 	 string 	 地址
   
            JSONObject json = new JSONObject(strBuff.toString());
            int resultcode = json.getInt("resultcode");
            if(resultcode == 200){
            	JSONObject subjosn = new JSONObject(json.getString("result"));
            	JSONArray data = subjosn.getJSONArray("data");
            	JSONObject location = data.getJSONObject(0);
            	itude.latitude = location.getString("O_LAT");
            	itude.longitude = location.getString("O_LNG");
            
            	Log.i("Itude", itude.latitude + itude.longitude);
            }
        } catch (Exception e) {
            Log.e(e.getMessage(), e.toString());
            throw new Exception("获取经纬度出现错误:"+e.getMessage());
        } finally{
            httpGet.abort();
            client = null;
        }
         
        return itude;
    }
     
    /**
     * 获取地理位置
     *
     * @throws Exception
     */
    private String getLocation(SItude itude) throws Exception {
        String resultString = "";
//https://maps.google.com/maps?hl=zh-CN&q=39.94817843967,116.46284342448
//http://apis.juhe.cn/geo/?dtype=json&key=67d12521d42f036096265a0ad7b19371&lat=39.94817843967&lng=116.46284342448&type=3
//        lng 	 String 	 Y 	 经度 (如：119.9772857)
//        lat 	 String 	 Y 	 纬度 (如：27.327578)
//        key 	 String 	 Y 	 申请的APPKEY
//        type 	 Int 	 Y 	 传递的坐标类型,1:GPS 2:百度经纬度 3：谷歌经纬度
//        dtype 	 String 	 N 	 返回数据格式：json或xml,默认json
        /** 这里采用get方法，直接将参数加到URL上 */
        String urlString = String.format("http://apis.juhe.cn/geo/?dtype=json&key=67d12521d42f036096265a0ad7b19371&lat=%s&lng=%s&type=3", itude.latitude, itude.longitude);
        Log.i("URL", urlString);
 
        /** 新建HttpClient */
        HttpClient client = new DefaultHttpClient();
        /** 采用GET方法 */
        HttpGet get = new HttpGet(urlString);
        try {
            /** 发起GET请求并获得返回数据 */
            HttpResponse response = client.execute(get);
            HttpEntity entity = response.getEntity();
            BufferedReader buffReader = new BufferedReader(new InputStreamReader(entity.getContent()));
            StringBuffer strBuff = new StringBuffer();
            String result = null;
            while ((result = buffReader.readLine()) != null) {
                strBuff.append(result);
            }
            resultString = strBuff.toString();
 
            /** 解析JSON数据，获得物理地址 */
            if (resultString != null && resultString.length() > 0) {
                JSONObject jsonobject = new JSONObject(resultString);
                int resultcode = jsonobject.getInt("resultcode");
                if(resultcode == 200){
                	JSONObject resultobj = new JSONObject(jsonobject.get("result").toString());
                	resultString = resultobj.getString("address");
                }else{
                	resultString = "";
                }
            }
        } catch (Exception e) {
            throw new Exception("获取物理位置出现错误:" + e.getMessage());
        } finally {
            get.abort();
            client = null;
        }
 
        return resultString;
    }
     
    /** 显示结果 */
    private void showResult(SCell cell, String location) {
        TextView cellText = (TextView) findViewById(R.id.cellText);
        cellText.setText(String.format("基站信息：mcc:%d, mnc:%d, lac:%d, cid:%d",
                cell.MCC, cell.MNC, cell.LAC, cell.CID));
 
        TextView locationText = (TextView) findViewById(R.id.lacationText);
        locationText.setText("物理位置：" + location);
    }
}