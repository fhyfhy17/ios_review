package com;

import lombok.Data;

@Data
public class Context
{
	private long id;
	private int rating;
	private String review;
	private String nickname;
	private String storeFront;
	private String appVersionString;
	private long lastModified;
	private int helpfulViews;
	private int totalViews;
	private boolean edited;
	private DeveloperResponse developerResponse=new DeveloperResponse();
	
	@Data
	private static class DeveloperResponse
	{
		private long responseId;
		private String response;
		private long lastModified;
		private boolean isHidden;
		private String pendingState;
		
		
	}
	
	
}
