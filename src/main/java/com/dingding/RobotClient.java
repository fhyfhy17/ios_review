package com.dingding;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

@Slf4j
public class RobotClient
{
	HttpClient httpclient=HttpClients.createDefault();
	
	public SendResult send(String webhook,TextMessage message) throws IOException
	{
		HttpPost httppost=new HttpPost(webhook);
		httppost.addHeader("Content-Type","application/json; charset=utf-8");
		StringEntity se=new StringEntity(message.toJsonString(),"utf-8");
		httppost.setEntity(se);
		SendResult sendResult=new SendResult();
		HttpResponse response=httpclient.execute(httppost);
		if(response.getStatusLine().getStatusCode()==HttpStatus.SC_OK)
		{
			String result=EntityUtils.toString(response.getEntity());
			JSONObject obj=JSONObject.parseObject(result);
			Integer errcode=obj.getInteger("errcode");
			sendResult.setErrorCode(errcode);
			sendResult.setErrorMsg(obj.getString("errmsg"));
			sendResult.setIsSuccess(errcode.equals(0));
		}
		return sendResult;
	}
	
	public void sendAsync(String webhook,TextMessage message)
    {
        MediaType mediaType=MediaType.parse("application/json; charset=utf-8");
        Request request=new Request.Builder().url(webhook).post(RequestBody.create(mediaType,message.toJsonString())).build();
        OkHttpClient okHttpClient=new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call,IOException e)
            {
                log.info("onFailure: " + e.getMessage());
            }
            
            @Override
            public void onResponse(Call call,Response response) throws IOException
            {
                log.info(response.protocol() + " " + response.code() + " " + response.message());
                log.info("onResponse: " + response.body().string());
            }
        });
    }
}