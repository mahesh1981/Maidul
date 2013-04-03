/**
 * 
 */
package com.radiorunt.utilities;

/**
 *
 */
public class StringPair {
	String id = null;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	String url = null;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return "id: " + this.id + " url:" + this.url;
	}

	public boolean isInitialized() {
		return (this.id != null && this.url != null);
	}

	public void deinitailize() {
		this.id = null;
		this.url = null;
	}

}
