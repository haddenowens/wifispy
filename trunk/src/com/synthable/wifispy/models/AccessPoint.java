package com.synthable.wifispy.models;


public class AccessPoint
{
	private int id;
	private String ssid;
	private boolean isNew = false;

	public AccessPoint()
	{
		isNew = true;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setSsid(String ssid) {
		this.ssid = ssid;
	}

	public String getSsid() {
		return ssid;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	public boolean isNew() {
		return isNew;
	}
}
