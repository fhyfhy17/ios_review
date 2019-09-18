package com;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dingding.SendTextMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.net.AuthServiceConfig;
import com.net.ConnectTokens;
import com.net.SigninRequest;
import com.net.TokensCookieStore;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.net.Utils.createExecutor;

@Slf4j
public class IOSReview
{
    public static final String SIGNIN_PATH = "/auth/signin";
    public static final String APP_CONFIG_PATH = "/app/config";
    public static final String SESSION_PATH = "/session";

    public static final ObjectMapper objectMapper = new ObjectMapper();

    private final String itunesConnectHostname;
    private final String olympusUrl;
    
    private Executor curExecutor;

    public IOSReview(String itunesConnectHostname,String olympusUrl) {
        this.itunesConnectHostname = itunesConnectHostname;
        this.olympusUrl = olympusUrl;
    }
    public void queryReview(){
        log.info("进行查询");
        Response response=null;
        try
        {
            response=curExecutor.execute(Request.Get(Const.IOS_REVIEW_URL));
        }
        catch(IOException e)
        {
            log.error("查询报错 ",e);
        }
    
        try
        {
            String s=response.returnContent().asString();
            log.info("请求，返回信息 {}",s);
            if(s.contains("Unauthenticated")){
                try
                {
                    login(Const.userName,Const.password);
                }
                catch(URISyntaxException e)
                {
                    log.info("",e);
                }
                return;
            }
    
            JSONObject jsonObject1=JSONObject.parseObject(s);
            log.info("转成json  = {}",jsonObject1.toJSONString());
            JSONObject data1=jsonObject1.getJSONObject("data");
            JSONArray reviews=data1.getJSONArray("reviews");
            List<String> all=getAll();
            for(Object review : reviews)
            {
                Context context=((JSONObject)review).getObject("value",Context.class);
                if(!all.contains(String.valueOf(context.getId()))){
                    log.info("发送到钉钉  context ={}",context);
                    SendTextMessage.sendWithAtAll(context.getNickname()
                            +"\n"+context.getStoreFront()
                            +"\n"+context.getReview()
                    
                    );
                    write(String.valueOf(context.getId()));
                }
            }
        }
        catch(IOException e)
        {
           log.info("",e);
        }
        
    }
    
    public void write(String line){
            FileWriter fw = null;
            try {
                //如果文件存在，则追加内容；如果文件不存在，则创建文件
                File f=new File("storeSent");
                fw = new FileWriter(f, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            PrintWriter pw = new PrintWriter(fw);
            pw.println(line);
            pw.flush();
            try {
                fw.flush();
                pw.close();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
    
    public List<String> getAll(){
        File storeSent=new File("storeSent");
        List<String> strings =new ArrayList<>();
        if(!storeSent.exists()){
            try
            {
                storeSent.createNewFile();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        try
        {
           strings=Files.readAllLines(Paths.get(storeSent.getAbsolutePath()));
           
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return  strings;
    }

    public void login(String login, String password) throws IOException, URISyntaxException {
        final TokensCookieStore cookieStore = new TokensCookieStore();
        final Executor executor = createExecutor(cookieStore);
        connect(executor, login, password);
        final Optional<ConnectTokens> result = cookieStore.getTokens();
        if (result.isPresent()) {
            curExecutor=executor;
        } else {
            throw new IOException("Unable to login: needed cookies not found");
        }
    }

    private void connect(Executor executor, String login, String password) throws IOException, URISyntaxException {
        final AuthServiceConfig config = getConfig(executor);
        signin(executor, config.getAuthServiceUrl(), config.getAuthServiceKey(), login, password);
        log.info("sigin 完成");
        session(executor);
    }

    private void signin(Executor executor, String authServiceUrl, String authServiceKey, String login, String password) throws IOException {
        final SigninRequest request = new SigninRequest(login, password, false);
        executor.execute(Request.Post(authServiceUrl + SIGNIN_PATH).bodyString(objectMapper.writeValueAsString(request), ContentType.APPLICATION_JSON).addHeader("X-Apple-Widget-Key", authServiceKey));
    }

    private void session(Executor executor) throws IOException {
        executor.execute(Request.Get(olympusUrl + SESSION_PATH));
    }

    private AuthServiceConfig getConfig(Executor executor) throws URISyntaxException, IOException {
        final URI configUrl = new URIBuilder(olympusUrl + APP_CONFIG_PATH).addParameter("hostname", itunesConnectHostname).build();
        return objectMapper.readValue(executor.execute(Request.Get(configUrl)).returnContent().asString(), AuthServiceConfig.class);
    }
}
