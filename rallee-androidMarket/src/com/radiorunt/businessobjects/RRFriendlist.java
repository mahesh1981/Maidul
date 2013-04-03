package com.radiorunt.businessobjects;

import android.os.Parcel;
import android.os.Parcelable;

public class RRFriendlist {// implements Parcelable {
// public static final Parcelable.Creator<RRFriendlist> CREATOR = new
// Creator<RRFriendlist>() {
// @Override
// public RRFriendlist createFromParcel(final Parcel source) {
// return new RRFriendlist(source);
// }
//
// @Override
// public RRFriendlist[] newArray(final int size) {
// return new RRFriendlist[size];
// }
// };

	public String id;
	public String name;
	public String listType;
	public Boolean hidden;
	public RRUser[] users;

	/**
	 * Value signaling whether this channel has just been removed. Once this
	 * value is set the connection signals one last update for the channel which
	 * should result in the channel being removed from all the caches where it
	 * might be stored.
	 */
	public RRFriendlist() {
		// TODO Auto-generated constructor stub,
		hidden = false;
	}

	// public RRFriendlist(final Parcel parcel) {
	// readFromParcel(parcel);
	// }

	// @Override
	// public int describeContents() {
	// // TODO Auto-generated method stub
	// return 0;
	// }

	@Override
	public final boolean equals(final Object o) {
		if (!(o instanceof RRFriendlist)) {
			return false;
		}
		return id == ((RRFriendlist) o).id;
	}

	// @Override
	// public final String hashCode() {
	// return id;
	// }

	@Override
	public final String toString() {
		return "Friendlist [id=" + id + ", name=" + name + ", listType="
				+ listType + ", hidden=" + hidden + "]";
	}

	// @Override
	// public void writeToParcel(final Parcel dest, final int flags) {
	// dest.writeInt(0); // Version
	//
	// dest.writeInt(id);
	// dest.writeString(name);
	// dest.writeInt(userCount);
	// }
	//
	// private void readFromParcel(final Parcel in) {
	// in.readInt(); // Version
	//
	// id = in.readInt();
	// name = in.readString();
	// userCount = in.readInt();
	// }
}
