package com.synthable.wifispy.models;


public class AccessPoint
{
	private int id;
	private String ssid;
	private String capabilities;
	private int frequency;
	private int dbm;
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

	public void setCapabilities(String capabilities) {
		this.capabilities = capabilities;
	}

	public String getCapabilities() {
		return capabilities;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setDbm(int dbm) {
		this.dbm = dbm;
	}

	public int getDbm() {
		return dbm;
	}
}
