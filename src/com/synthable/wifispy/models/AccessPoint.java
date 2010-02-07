package com.synthable.wifispy.models;


public class AccessPoint
{
	private int id;
	private String ssid;
	private String bssid;
	private String capabilities;
	private int frequency;
	private int dbm;
	private Double Lat;
	private Double Long;
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

	public void setLat(Double lat) {
		Lat = lat;
	}

	public Double getLat() {
		return Lat;
	}

	public void setLong(Double _long) {
		Long = _long;
	}

	public Double getLong() {
		return Long;
	}

	public void setBssid(String bssid) {
		this.bssid = bssid;
	}

	public String getBssid() {
		return bssid;
	}
}
