package com.radiorunt.businessobjects;

import junit.framework.Assert;
import android.os.Parcel;
import android.os.Parcelable;

public class RRUser implements Parcelable {
	public static final Parcelable.Creator<RRUser> CREATOR = new Creator<RRUser>() {
		@Override
		public RRUser createFromParcel(final Parcel source) {
			return new RRUser(source);
		}

		@Override
		public RRUser[] newArray(final int size) {
			return new RRUser[size];
		}
	};

	public static final int TALKINGSTATE_PASSIVE = 0;
	public static final int TALKINGSTATE_TALKING = 1;
	public static final int TALKINGSTATE_SHOUTING = 2;
	public static final int TALKINGSTATE_WHISPERING = 3;

	public static final int USERSTATE_NONE = 0;
	public static final int USERSTATE_MUTED = 1;
	public static final int USERSTATE_DEAFENED = 2;

	public int session;
	public int id;
	public String userName;
	public String FirstName;
	public String LastName;
	public String Email;
	public String Password;
	public float averageAvailable;
	public int talkingState;
	public int userState;
	public boolean isCurrent;
	public boolean isOnline;

	public boolean muted;
	public boolean deafened;
	public boolean hasApp;

	public String picUrl;
	public String location;

	private RRChannels channel;

	public RRUser() {
	}

	public RRUser(final Parcel in) {
		readFromParcel(in);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public final boolean equals(final Object o) {
		if (!(o instanceof RRUser)) {
			return false;
		}
		return session == ((RRUser) o).session;
	}

	public final RRChannels getChannel() {
		return this.channel;
	}

	public void setChannel(RRChannels channel) {
		this.channel = channel;
	}

	@Override
	public final int hashCode() {
		return session;
	}

	/*
	 * public void setChannel(final RRChannels newChannel) { // Moving user to
	 * another channel? // If so, remove the user from the original first. if
	 * (this.channel != null) { this.channel.userCount--; }
	 * 
	 * // User should never leave channel without joining a new one?
	 * Assert.assertNotNull(newChannel);
	 * 
	 * this.channel = newChannel; this.channel.userCount++; }
	 */
	@Override
	public final String toString() {
		return "User [session=" + session + ", name=" + userName + ", channel="
				+ channel + "]";
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeInt(0); // Version

		dest.writeInt(session);
		dest.writeString(userName);
		dest.writeString(FirstName);
		dest.writeFloat(averageAvailable);
		dest.writeInt(talkingState);
		dest.writeBooleanArray(new boolean[] { isCurrent, muted, deafened,
				hasApp });
		dest.writeString(picUrl);
		dest.writeString(location);
		dest.writeParcelable(channel, 0);
	}

	private void readFromParcel(final Parcel in) {
		in.readInt(); // Version

		session = in.readInt();
		userName = in.readString();
		FirstName = in.readString();
		averageAvailable = in.readFloat();
		talkingState = in.readInt();
		final boolean[] boolArr = new boolean[3];
		in.readBooleanArray(boolArr);
		isCurrent = boolArr[0];
		muted = boolArr[1];
		deafened = boolArr[2];
		hasApp = boolArr[3];
		picUrl = in.readString();
		location = in.readString();
		channel = in.readParcelable(null);
	}
}
