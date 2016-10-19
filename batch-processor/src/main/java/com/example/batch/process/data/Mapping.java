package com.example.batch.process.data;

import java.io.Serializable;

public class Mapping implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6337597032763662846L;
	
	private String clientId;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
}
