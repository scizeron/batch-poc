package com.example.batch.scheduler.v1;

public class Response {

	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	
	public Response withMessage(String message) {
		this.message = message;
		return this;
	}
}
