package com.example.batch.process.v1;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class JobExecutionBody {

	private String clientId;
	
	private String internalJobName;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getInternalJobName() {
		return internalJobName;
	}

	public void setInternalJobName(String internalJobName) {
		this.internalJobName = internalJobName;
	}
	
	@JsonIgnore
	public Map<String, String> getValues() {
		final Map<String, String> values = new HashMap<>();
		Arrays.asList(getClass().getDeclaredFields()).stream().forEach(field -> {
			try {
				Object object = field.get(this);
				if (object != null) {
					values.put(field.getName(), object.toString());
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}	
		});
		return values;
	}
}
