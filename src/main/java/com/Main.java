package com;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Main
{
	public static void main(String[] args)
	{
		
		IOSReview iosReview=new IOSReview(Const.ITUNESCONNECT_HOSTNAME,Const.OLYMPUS_URL);

		try
		{
			iosReview.login(Const.userName,Const.password);
		}
		catch(IOException | URISyntaxException e)
		{
			log.info("",e);
		}
		ScheduledExecutorService service=Executors.newSingleThreadScheduledExecutor();
		
		service.scheduleWithFixedDelay(iosReview::queryReview,0,20,TimeUnit.SECONDS);
	
	}
}
