package com.radiorunt.businessobjects;

import android.os.Parcel;
import android.os.Parcelable;
import com.radiorunt.businessobjects.RRChannels;
import com.radiorunt.businessobjects.RRUser;

public class RRMessages implements Parcelable {
	public static final int DIRECTION_SENT = 0;
	public static final int DIRECTION_RECEIVED = 1;

	public static final Parcelable.Creator<RRMessages> CREATOR = new Creator<RRMessages>() {
		@Override
		public RRMessages createFromParcel(final Parcel source) {
			return new RRMessages(source);
		}

		@Override
		public RRMessages[] newArray(final int size) {
			return new RRMessages[size];
		}
	};

	public String message;
	public String sender;
	public RRUser actor;
	public RRChannels channel;
	public long timestamp;
	public int channelIds;
	public int treeIds;

	public int direction;

	public RRMessages() {
	}

	public RRMessages(final Parcel parcel) {
		readFromParcel(parcel);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public void readFromParcel(final Parcel in) {
		in.readInt(); // Version

		message = in.readString();
		sender = in.readString();
		actor = in.readParcelable(null);
		channel = in.readParcelable(null);
		timestamp = in.readLong();
		channelIds = in.readInt();
		treeIds = in.readInt();
		direction = in.readInt();
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeInt(0); // Version

		dest.writeString(message);
		dest.writeString(sender);
		dest.writeParcelable(actor, 0);
		dest.writeParcelable(channel, 0);
		dest.writeLong(timestamp);
		dest.writeInt(channelIds);
		dest.writeInt(treeIds);
		dest.writeInt(direction);
	}
}
