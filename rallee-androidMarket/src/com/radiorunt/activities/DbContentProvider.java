package com.radiorunt.activities;

import com.radiorunt.utilities.Globals;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class DbContentProvider extends ContentProvider {

	public static final String PROVIDER_NAME = "com.radiorunt.activities.DbContentProvider";

	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ PROVIDER_NAME + "/server");

	public static final String _ID = "_id";
	public static final String NAME = "name";
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";

	private static final int SERVER = 1;
	private static final int SERVER_ID = 2;
	private static final int USER = 3;
	private static final int USER_ID = 4;
	private static final int MISSED_CALL = 5;
	private static final int CHANNEL = 6;
	private static final int CHANNEL_ID = 7;
	private static final int FB_GROUP = 8;
	private static final int MISSED_CALL_TIMESTAMP = 9;
	private static final int FB_GROUP_ID_STRING = 10;

	private static final int PRIVATE_GROUP = 11;
	private static final int PRIVATE_GROUP_ID_NUM = 12;
	private static final int GROUP_MEMBERS = 13;

	private static final int CHANNEL_DISTINCT = 14;
	private static final int CHANNEL_CATEGORY = 15;

	private static final int TRANSCRIPT = 16;

	private static final int CHANNEL_CHAR = 17;

	private static final int CALL_HISTORY = 18;
	private static final int CALL_HISTORY_TYPE = 19;

	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, "server", SERVER);
		uriMatcher.addURI(PROVIDER_NAME, "server/#", SERVER_ID);

		uriMatcher.addURI(PROVIDER_NAME, "user", USER);
		uriMatcher.addURI(PROVIDER_NAME, "user/*", USER_ID);

		uriMatcher.addURI(PROVIDER_NAME, "missed_calls", MISSED_CALL);
		uriMatcher.addURI(PROVIDER_NAME, "missed_calls/#",
				MISSED_CALL_TIMESTAMP);
		uriMatcher.addURI(PROVIDER_NAME, "channel", CHANNEL);
		uriMatcher.addURI(PROVIDER_NAME, "channel/#", CHANNEL_ID);
		uriMatcher.addURI(PROVIDER_NAME, "channel/*", CHANNEL_CHAR);
		uriMatcher.addURI(PROVIDER_NAME, "channel_distinct", CHANNEL_DISTINCT);
		uriMatcher.addURI(PROVIDER_NAME, "channel_category", CHANNEL_CATEGORY);

		uriMatcher.addURI(PROVIDER_NAME, "fb_groups", FB_GROUP);
		uriMatcher.addURI(PROVIDER_NAME, "fb_groups/*", FB_GROUP_ID_STRING);

		uriMatcher.addURI(PROVIDER_NAME, "private_group", PRIVATE_GROUP);
		uriMatcher.addURI(PROVIDER_NAME, "private_group/#",
				PRIVATE_GROUP_ID_NUM);

		uriMatcher.addURI(PROVIDER_NAME, "group_members", GROUP_MEMBERS);

		uriMatcher.addURI(PROVIDER_NAME, "call_history", CALL_HISTORY);

		uriMatcher.addURI(PROVIDER_NAME, "transcript", TRANSCRIPT);
	}

	// ---for database use---
	private SQLiteDatabase db;

	// --------
	public static final String DATABASE_NAME = "rallee_content.db";
	public static final int DATABESE_VERSION = 1;
	public static final String SERVER_TABLE = "server";
	public static final String SERVER_COL_ID = "_id";
	public static final String SERVER_COL_NAME = "name";
	public static final String SERVER_COL_HOST = "host";
	public static final String SERVER_COL_PORT = "port";
	public static final String SERVER_COL_USERNAME = "username";
	public static final String SERVER_COL_PASSWORD = "password";
	// --------
	public static final String USER_TABLE = "user";
	public static final String USER_COL_ID = "_id";
	public static final String USER_COL_NAME = "name";
	public static final String USER_COL_LOCATION = "location";
	public static final String USER_COL_PIC_URL = "pic_url";
	public static final String USER_COL_INSTALLED = "installed";
	public static final String USER_COL_DELETED = "deleted";
	// --------
	public static final String PRIVATE_GROUP_TABLE = "private_group";
	public static final String PRIVATE_GROUP_COL_ID = "_id";
	public static final String PRIVATE_GROUP_COL_NAME = "name";
	public static final String PRIVATE_GROUP_COL_DESCRIPTION = "description";

	public static final String FB_GROUPS_TABLE = "fb_groups";
	public static final String FB_GROUP_COL_ID = "_id";
	public static final String FB_GROUP_COL_NAME = "name";

	public static final String GROUP_MEMBERS_TABLE = "group_members";
	public static final String GROUP_MEMBERS_COL_GROUP_ID = "groupId";
	public static final String GROUP_MEMBERS_COL_USER_ID = "userId";

	public static final String MISSED_CALLS_TABLE = "missed_calls";
	public static final String MISSED_CALLS_COL_TIMESTAMP = "_id";
	public static final String MISSED_CALLS_COL_SENDER = "sender";

	public static final String CHANNEL_TABLE = "channel";
	public static final String CHANNEL_TABLE_DISTINCT = "channel_distinct";
	public static final String CHANNEL_COL_ID = "_id";
	public static final String CHANNEL_COL_NAME = "name";
	public static final String CHANNEL_COL_CHANNEL_ID = "channel_id";
	public static final String CHANNEL_COL_PARENT = "parent";
	public static final String CHANNEL_COL_DESCRIPTION = "description";
	public static final String CHANNEL_TEMPORARY = "temporary";
	public static final String CHANNEL_COL_USER_COUNT = "userCount";
	public static final String CHANNEL_COL_SERVER_IP_ADR = "serverIpAdr";
	public static final String CHANNEL_COL_PORT = "port";
	public static final String CHANNEL_COL_CATEGORY = "category";

	public static final String CHANNEL_CATEGORY_TABLE = "channel_category";
	public static final String CHANNEL_CATEGORY_COL_ID = "_id";
	public static final String CHANNEL_CATEGORY_COL_NAME = "name";

	public static final String CALL_HISTORY_TABLE = "call_history";
	public static final String CALL_HISTORY_COL_TIMESTAMP = "_id";
	public static final String CALL_HISTORY_COL_DURATION = "duration";
	public static final String CALL_HISTORY_COL_TYPE = "type";
	public static final int CALL_HISTORY_TYPE_PRIVATE_CALL_OUTGOING = 0;
	public static final int CALL_HISTORY_TYPE_PRIVATE_CALL_INCOMING = 1;
	public static final int CALL_HISTORY_TYPE_PRIVATE_GROUP_CALL = 2;
	public static final int CALL_HISTORY_TYPE_PUBLIC_GROUP_CALL = 3;
	public static final int CALL_HISTORY_TYPE_FB_GROUP_CALL = 4;
	public static final int CALL_HISTORY_TYPE_RANDOM_CALL = 5;
	public static final String CALL_HISTORY_COL_USER_ID = "user_id";
	public static final String CALL_HISTORY_COL_PRIVATE_GROUP_ID = "private_group_id";
	public static final String CALL_HISTORY_COL_PUBLIC_GROUP_NAME = "public_group_name";
	public static final String CALL_HISTORY_COL_FB_GROUP_ID = "fb_group_id";

	public static final String TRANSCRIPT_TABLE = "transcript";
	public static final String TRANSCRIPT_COL_TIMESTAMP = "_id";
	public static final String TRANSCRIPT_COL_HISTORY_ID_TIMESTAMP = "historyTimestamp";
	public static final String TRANSCRIPT_COL_HISTORY_ID_TYPE = "historyType";
	public static final String TRANSCRIPT_COL_FROM = "from_user";
	public static final String TRANSCRIPT_COL_MESSAGE = "message";

	public static final Uri CONTENT_URI_SERVER_TABLE = Uri.parse("content://"
			+ PROVIDER_NAME + "/" + SERVER_TABLE);
	public static final Uri CONTENT_URI_USER_TABLE = Uri.parse("content://"
			+ PROVIDER_NAME + "/" + USER_TABLE);
	public static final Uri CONTENT_URI_MISSED_CALL_TABLE = Uri
			.parse("content://" + PROVIDER_NAME + "/" + MISSED_CALLS_TABLE);
	public static final Uri CONTENT_URI_CHANNEL_TABLE = Uri.parse("content://"
			+ PROVIDER_NAME + "/" + CHANNEL_TABLE);
	public static final Uri CONTENT_URI_FB_GROUPS_TABLE = Uri
			.parse("content://" + PROVIDER_NAME + "/" + FB_GROUPS_TABLE);
	public static final Uri CONTENT_URI_PRIVATE_GROUP_TABLE = Uri
			.parse("content://" + PROVIDER_NAME + "/" + PRIVATE_GROUP_TABLE);
	public static final Uri CONTENT_URI_GROUP_MEMBERS_TABLE = Uri
			.parse("content://" + PROVIDER_NAME + "/" + GROUP_MEMBERS_TABLE);
	public static final Uri CONTENT_URI_CHANNEL_TABLE_DISTINCT = Uri
			.parse("content://" + PROVIDER_NAME + "/" + CHANNEL_TABLE_DISTINCT);

	public static final Uri CONTENT_URI_CHANNEL_CATEGORY_TABLE = Uri
			.parse("content://" + PROVIDER_NAME + "/" + CHANNEL_CATEGORY_TABLE);
	public static final Uri CONTENT_URI_CALL_HISTORY_TABLE = Uri
			.parse("content://" + PROVIDER_NAME + "/" + CALL_HISTORY_TABLE);
	public static final Uri CONTENT_URI_TRANSCRIPT_TABLE = Uri
			.parse("content://" + PROVIDER_NAME + "/" + TRANSCRIPT_TABLE);

	public class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABESE_VERSION);
		}

		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + SERVER_TABLE + " ("
					+ "`_id` INTEGER PRIMARY KEY," + "`name` TEXT NOT NULL,"
					+ "`host` TEXT NOT NULL," + "`port` INTEGER,"
					+ "`username` TEXT NOT NULL," + "`password` TEXT" + ");");
			db.execSQL("CREATE TABLE " + USER_TABLE + " ("
					+ "`_id` TEXT PRIMARY KEY," + "`name` TEXT NOT NULL,"
					+ "`location` TEXT," + "`pic_url` TEXT,"
					+ "`installed` BOOLEAN DEFAULT 0,"
					+ "`deleted` BOOLEAN DEFAULT 0" // 0 = false
					+ ");");

			db.execSQL("CREATE TABLE " + MISSED_CALLS_TABLE + " ("
					+ "`_id` INTEGER PRIMARY KEY," + "`sender` TEXT NOT NULL"
					+ ");");

			db.execSQL("CREATE TABLE "
					+ CHANNEL_TABLE
					+ " ("
					+ "`_id` INTEGER PRIMARY KEY,"
					+ "`name` TEXT UNIQUE NOT NULL,"
					+ "`channel_id` INTEGER NOT NULL,"
					+ "`parent` INTEGER BOOLEAN DEFAULT 0,"
					+ "`description` TEXT,"
					+ "`temporary` BOOLEAN DEFAULT 0," // 0 = false
					+ "`userCount` INTEGER," + "`serverIpAdr` TEXT NOT NULL,"
					+ "`port` TEXT NOT NULL," + "`" + CHANNEL_COL_CATEGORY
					+ "` INTEGER REFERENCES " + CHANNEL_CATEGORY_TABLE + "("
					+ CHANNEL_CATEGORY_COL_ID + ") " + " DEFAULT 1" + ");");

			db.execSQL("CREATE TABLE " + CHANNEL_CATEGORY_TABLE + " (" + "`"
					+ CHANNEL_CATEGORY_COL_ID
					+ "` INTEGER PRIMARY KEY AUTOINCREMENT," + "`"
					+ CHANNEL_CATEGORY_COL_NAME + "` TEXT NOT NULL" + ");");

			db.execSQL("CREATE TABLE " + FB_GROUPS_TABLE + " ("
					+ "`_id` TEXT PRIMARY KEY," + "`name` TEXT" + ");");

			db.execSQL("CREATE TABLE " + PRIVATE_GROUP_TABLE + " ("
					+ "`_id` INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "`name` TEXT, " + "`" + PRIVATE_GROUP_COL_DESCRIPTION
					+ "` TEXT" + ");");

			db.execSQL("CREATE TABLE " + GROUP_MEMBERS_TABLE + " (" + "`"
					+ GROUP_MEMBERS_COL_GROUP_ID + "` INTEGER REFERENCES "
					+ PRIVATE_GROUP_TABLE + "(" + PRIVATE_GROUP_COL_ID + "),"
					+ "`" + GROUP_MEMBERS_COL_USER_ID + "` TEXT REFERENCES "
					+ USER_TABLE + "(" + USER_COL_ID + ")," + "FOREIGN KEY("
					+ GROUP_MEMBERS_COL_GROUP_ID + ") REFERENCES "
					+ PRIVATE_GROUP_TABLE + "(" + PRIVATE_GROUP_COL_ID + "),"
					+ "FOREIGN KEY(" + GROUP_MEMBERS_COL_USER_ID
					+ ") REFERENCES " + USER_TABLE + "(" + USER_COL_ID + "),"
					+ "PRIMARY KEY (" + GROUP_MEMBERS_COL_GROUP_ID + ","
					+ GROUP_MEMBERS_COL_USER_ID + "));");
			db.execSQL("CREATE TABLE " + CALL_HISTORY_TABLE + " (" + "`"
					+ CALL_HISTORY_COL_TIMESTAMP + "` INTEGER, `"
					+ CALL_HISTORY_COL_DURATION + "` INTEGER DEFAULT 0, `"
					+ CALL_HISTORY_COL_TYPE + "` INTEGER, `"
					+ CALL_HISTORY_COL_USER_ID + "` TEXT REFERENCES "
					+ USER_TABLE + "(" + USER_COL_ID + "), `"
					+ CALL_HISTORY_COL_PRIVATE_GROUP_ID
					+ "` INTEGER REFERENCES " + PRIVATE_GROUP_TABLE + "("
					+ PRIVATE_GROUP_COL_ID + "), `"
					+ CALL_HISTORY_COL_PUBLIC_GROUP_NAME + "` TEXT REFERENCES "
					+ CHANNEL_TABLE + "(" + CHANNEL_COL_NAME + "), `"
					+ CALL_HISTORY_COL_FB_GROUP_ID + "` TEXT REFERENCES "
					+ FB_GROUPS_TABLE + "(" + FB_GROUP_COL_ID + "), "
					+ "PRIMARY KEY (" + CALL_HISTORY_COL_TIMESTAMP + ","
					+ CALL_HISTORY_COL_TYPE + "));");

			db.execSQL("CREATE TABLE " + TRANSCRIPT_TABLE + " (" + "`"
					+ TRANSCRIPT_COL_TIMESTAMP + "` INTEGER NOT NULL, `"
					+ TRANSCRIPT_COL_FROM + "` TEXT NOT NULL, `"
					+ TRANSCRIPT_COL_MESSAGE + "` TEXT, `"
					+ TRANSCRIPT_COL_HISTORY_ID_TIMESTAMP
					+ "` INTEGER REFERENCES " + CALL_HISTORY_TABLE + "("
					+ CALL_HISTORY_COL_TIMESTAMP + "), `"
					+ TRANSCRIPT_COL_HISTORY_ID_TYPE + "` INTEGER REFERENCES "
					+ CALL_HISTORY_TABLE + "(" + CALL_HISTORY_COL_TYPE + "), "

					+ "PRIMARY KEY (" + TRANSCRIPT_COL_TIMESTAMP + "));");

		}

		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
				final int newVersion) {
			Globals.logWarn(this, "Database upgrade from " + oldVersion
					+ " to " + newVersion);
			if (oldVersion == 1) {
				db.execSQL("ALTER TABLE `server` RENAME TO `server_old`");
				db.execSQL("ALTER TABLE `user` RENAME TO `user_old`");

				db.execSQL("ALTER TABLE `missed_calls` RENAME TO `missed_calls_old`");
				db.execSQL("ALTER TABLE `channel` RENAME TO `channel_old`");
				db.execSQL("ALTER TABLE `" + CHANNEL_CATEGORY_TABLE
						+ "` RENAME TO `" + CHANNEL_CATEGORY_TABLE + "_old`");

				db.execSQL("ALTER TABLE `fb_groups` RENAME TO `fb_groups_old`");

				db.execSQL("ALTER TABLE `private_group` RENAME TO `private_group_old`");

				db.execSQL("ALTER TABLE `" + GROUP_MEMBERS_TABLE
						+ "` RENAME TO `" + GROUP_MEMBERS_TABLE + "_old`");

				db.execSQL("ALTER TABLE `" + TRANSCRIPT_TABLE + "` RENAME TO `"
						+ TRANSCRIPT_TABLE + "_old`");

				db.execSQL("ALTER TABLE `" + CALL_HISTORY_TABLE
						+ "` RENAME TO `" + CALL_HISTORY_TABLE + "_old`");

				onCreate(db);

				db.execSQL("INSERT INTO `server` SELECT "
						+ "`_id`, `name`, `host`, `port`, `username`, `password` "
						+ " FROM `server_old`");
				db.execSQL("DROP TABLE `server_old`");

				db.execSQL("INSERT INTO `" + USER_TABLE + "` SELECT `"
						+ USER_COL_ID + "`, `" + USER_COL_NAME + "`, `"
						+ USER_COL_LOCATION + "`, `" + USER_COL_PIC_URL
						+ "`, `" + USER_COL_INSTALLED + "` ,`"
						+ USER_COL_DELETED + "` FROM `" + USER_TABLE + "_old`");
				db.execSQL("DROP TABLE `" + USER_TABLE + "_old`");

				db.execSQL("INSERT INTO `" + MISSED_CALLS_TABLE + "` SELECT `"
						+ MISSED_CALLS_COL_TIMESTAMP + "`, `"
						+ MISSED_CALLS_COL_SENDER + "` FROM `"
						+ MISSED_CALLS_TABLE + "_old`");
				db.execSQL("DROP TABLE `missed_calls_old`");

				db.execSQL("INSERT INTO `channel` SELECT "
						+ "`_id`, `name`, `channel_id`, `parent`, `description`, `temporary`, `userCount`, `serverIpAdr`, `port`, `category`"
						+ " FROM `channel_old`");

				db.execSQL("INSERT INTO `" + CHANNEL_CATEGORY_TABLE
						+ "` SELECT `" + CHANNEL_CATEGORY_COL_ID + "`, `"
						+ CHANNEL_CATEGORY_COL_NAME + "` FROM `"
						+ CHANNEL_CATEGORY_TABLE + "_old`");

				db.execSQL("DROP TABLE `" + CHANNEL_CATEGORY_TABLE + "_old`");

				db.execSQL("INSERT INTO `fb_groups` SELECT " + "`_id`, `name` "
						+ "FROM `fb_groups_old`");
				db.execSQL("DROP TABLE `fb_groups_old`");

				db.execSQL("INSERT INTO `" + PRIVATE_GROUP_TABLE + "` SELECT `"
						+ PRIVATE_GROUP_COL_ID + "`, `"
						+ PRIVATE_GROUP_COL_NAME + "`, `"
						+ PRIVATE_GROUP_COL_DESCRIPTION + "` FROM `"
						+ PRIVATE_GROUP_TABLE + "_old`");
				db.execSQL("DROP TABLE `" + PRIVATE_GROUP_TABLE + "_old`");

				db.execSQL("INSERT INTO `" + GROUP_MEMBERS_TABLE + "` SELECT `"
						+ GROUP_MEMBERS_COL_GROUP_ID + "`, `"
						+ GROUP_MEMBERS_COL_USER_ID + "` FROM `"
						+ GROUP_MEMBERS_TABLE + "_old`");
				db.execSQL("DROP TABLE `" + GROUP_MEMBERS_TABLE + "_old`");

				db.execSQL("INSERT INTO `" + TRANSCRIPT_TABLE + "` SELECT "
						+ "`" + TRANSCRIPT_COL_TIMESTAMP + "`, `"
						+ TRANSCRIPT_COL_FROM + "`, `" + TRANSCRIPT_COL_MESSAGE
						+ "`, `" + TRANSCRIPT_COL_HISTORY_ID_TIMESTAMP + "`, `"
						+ TRANSCRIPT_COL_HISTORY_ID_TYPE + "` FROM `"
						+ TRANSCRIPT_TABLE + "_old`");
				db.execSQL("DROP TABLE `" + TRANSCRIPT_TABLE + "_old`");

				db.execSQL("INSERT INTO `" + CALL_HISTORY_TABLE + "` SELECT `"
						+ CALL_HISTORY_COL_TIMESTAMP + "`, `"
						+ CALL_HISTORY_COL_DURATION + "`, `"
						+ CALL_HISTORY_COL_TYPE + "`, `"
						+ CALL_HISTORY_COL_USER_ID + "`, `"
						+ CALL_HISTORY_COL_PRIVATE_GROUP_ID + "`, `"
						+ CALL_HISTORY_COL_PUBLIC_GROUP_NAME + "`, `"
						+ CALL_HISTORY_COL_FB_GROUP_ID + "` FROM `"
						+ CALL_HISTORY_TABLE + "_old`");
				db.execSQL("DROP TABLE `" + CALL_HISTORY_TABLE + "_old`");

			}
		}
	}

	// --------

	@Override
	public int delete(Uri uri, String whereClause, String[] whereArgs) {

		int count = 0;
		switch (uriMatcher.match(uri)) {
		case SERVER:
			count = db.delete(SERVER_TABLE, whereClause, whereArgs);
			break;
		case SERVER_ID:
			String id = uri.getLastPathSegment();
			count = db.delete(SERVER_TABLE, SERVER_COL_ID + " = " + id,
					whereArgs);
			break;
		case USER:
			count = db.delete(USER_TABLE, whereClause, whereArgs);
			break;

		case MISSED_CALL:
			count = db.delete(MISSED_CALLS_TABLE, whereClause, whereArgs);
			break;

		case MISSED_CALL_TIMESTAMP:
			String timestamp = uri.getLastPathSegment();
			count = db.delete(MISSED_CALLS_TABLE, MISSED_CALLS_COL_TIMESTAMP
					+ "=" + timestamp, null);
			break;

		case CHANNEL:
			count = db.delete(CHANNEL_TABLE, null, null);
			break;

		case CHANNEL_CHAR:
			String name = uri.getLastPathSegment();
			count = db.delete(CHANNEL_TABLE, CHANNEL_COL_NAME + " = '" + name
					+ "'", null);
			break;
		case CHANNEL_CATEGORY:
			count = db.delete(CHANNEL_TABLE, whereClause, whereArgs);
			break;

		case FB_GROUP:
			count = db.delete(FB_GROUPS_TABLE, null, null);
			break;
		case PRIVATE_GROUP:
			count = db.delete(PRIVATE_GROUP_TABLE, whereClause, whereArgs);
			break;
		case PRIVATE_GROUP_ID_NUM:
			id = uri.getLastPathSegment();
			count = db.delete(PRIVATE_GROUP_TABLE, PRIVATE_GROUP_COL_ID + "=?",
					new String[] { id });
			break;
		case GROUP_MEMBERS:
			count = db.delete(GROUP_MEMBERS_TABLE, whereClause, whereArgs);
			break;
		case CALL_HISTORY:
			count = db.delete(CALL_HISTORY_TABLE, whereClause, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {

		// case BOOKS:
		// return "vnd.android.cursor.dir/vnd.learn2develop.books ";
		//
		// case BOOK_ID:
		// return "vnd.android.cursor.item/vnd.learn2develop.books ";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Uri _uri = null;
		long rowID;
		switch (uriMatcher.match(uri)) {
		case SERVER:
			rowID = db.insert(SERVER_TABLE, "", values);
			// ---if added successfully---
			if (rowID > 0) {
				_uri = ContentUris.withAppendedId(CONTENT_URI_SERVER_TABLE,
						rowID);
				getContext().getContentResolver().notifyChange(_uri, null);

			}
			// throw new SQLException("Failed to insert row into " + uri);
			break;
		case USER:
			rowID = db.insert(USER_TABLE, "", values);
			// ---if added successfully---
			if (rowID > 0) {
				_uri = ContentUris
						.withAppendedId(CONTENT_URI_USER_TABLE, rowID);
				getContext().getContentResolver().notifyChange(_uri, null);

			}
			break;

		case MISSED_CALL:
			// Uri CONTENT_URI_MISSED_CALL_TABLE
			rowID = db.insert(MISSED_CALLS_TABLE, null, values);
			if (rowID > 0) {
				_uri = ContentUris.withAppendedId(
						CONTENT_URI_MISSED_CALL_TABLE, rowID);
				getContext().getContentResolver().notifyChange(_uri, null);

			}
			break;

		case CHANNEL:
			rowID = db.insert(CHANNEL_TABLE, null, values);
			if (rowID > 0) {
				_uri = ContentUris.withAppendedId(CONTENT_URI_CHANNEL_TABLE,
						rowID);
				getContext().getContentResolver().notifyChange(_uri, null);
			}

			break;

		case CHANNEL_CATEGORY:
			rowID = db.insert(CHANNEL_CATEGORY_TABLE, null, values);
			if (rowID > 0) {
				_uri = ContentUris.withAppendedId(
						CONTENT_URI_CHANNEL_CATEGORY_TABLE, rowID);
				getContext().getContentResolver().notifyChange(_uri, null);
			}

			break;

		case FB_GROUP:
			rowID = db.insert(FB_GROUPS_TABLE, null, values);
			if (rowID > 0) {
				_uri = ContentUris.withAppendedId(CONTENT_URI_FB_GROUPS_TABLE,
						rowID);
				getContext().getContentResolver().notifyChange(_uri, null);
			}
			break;
		case PRIVATE_GROUP:
			rowID = db.insert(PRIVATE_GROUP_TABLE, null, values);
			if (rowID > 0) {
				_uri = ContentUris.withAppendedId(
						CONTENT_URI_PRIVATE_GROUP_TABLE, rowID);
				getContext().getContentResolver().notifyChange(_uri, null);
			}
			break;
		case GROUP_MEMBERS:
			rowID = db.insert(GROUP_MEMBERS_TABLE, null, values);
			if (rowID > 0) {
				_uri = ContentUris.withAppendedId(
						CONTENT_URI_GROUP_MEMBERS_TABLE, rowID);
				getContext().getContentResolver().notifyChange(_uri, null);
			}
			break;
		case CALL_HISTORY:
			rowID = db.insert(CALL_HISTORY_TABLE, null, values);
			if (rowID > 0) {
				_uri = ContentUris.withAppendedId(
						CONTENT_URI_CALL_HISTORY_TABLE, rowID);
				getContext().getContentResolver().notifyChange(_uri, null);
			}
			break;

		case TRANSCRIPT:
			rowID = db.insert(TRANSCRIPT_TABLE, "", values);
			if (rowID > 0) {
				_uri = ContentUris.withAppendedId(CONTENT_URI_TRANSCRIPT_TABLE,
						rowID);
				getContext().getContentResolver().notifyChange(_uri, null);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		return _uri;
	}

	@Override
	public boolean onCreate() {
		Context context = getContext();
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
		return (db == null) ? false : true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		// sqlBuilder.setTables(SERVER_TABLE);
		/*
		 * if (uriMatcher.match(uri) == BOOK_ID) //---if getting a particular
		 * book--- sqlBuilder.appendWhere( _ID + " = " +
		 * uri.getPathSegments().get(1));
		 * 
		 * if (sortOrder==null || sortOrder=="") sortOrder = SERVER_COL_NAME;
		 */
		Cursor c = null;
		String id = null;
		boolean distinct = false;

		switch (uriMatcher.match(uri)) {
		case SERVER:

			sqlBuilder.setTables(SERVER_TABLE);
			c = sqlBuilder.query(db, projection, selection, selectionArgs,
					null, null, sortOrder);
			break;
		case SERVER_ID:
			sqlBuilder.setTables(SERVER_TABLE);
			id = uri.getLastPathSegment();
			c = sqlBuilder.query(db, projection, SERVER_COL_ID + " = " + id,
					null, null, null, null);
			if (c != null) {
				c.moveToFirst();
			}
			break;
		case USER:
			sqlBuilder.setTables(USER_TABLE);
			c = sqlBuilder
					.query(db,
							// new String[] { "rowid", USER_COL_ID,
							// USER_COL_NAME,
							// USER_COL_LOCATION, USER_COL_PIC_URL,
							// USER_COL_INSTALLED,
							// USER_COL_DELETED },
							projection, selection, selectionArgs, null, null,
							sortOrder);
			break;
		case USER_ID:
			sqlBuilder.setTables(USER_TABLE);
			id = uri.getLastPathSegment();
			c = db.query(USER_TABLE, projection, USER_COL_ID + "=?",
					new String[] { id }, null, null, null);
			if (c != null) {
				if (c.getCount() != 0) {
					c.moveToFirst();
				}
			}
			break;

		case MISSED_CALL:
			sqlBuilder.setTables(MISSED_CALLS_TABLE);
			c = db.query(MISSED_CALLS_TABLE, projection, null, null, null,
					null, sortOrder);
			break;
		case CHANNEL:
			sqlBuilder.setTables(CHANNEL_TABLE);
			// c = db.query(CHANNEL_TABLE, new String[] { CHANNEL_COL_NAME,
			// CHANNEL_COL_ID, CHANNEL_COL_PARENT,
			// CHANNEL_COL_DESCRIPTION, CHANNEL_TEMPORARY,
			// CHANNEL_COL_USER_COUNT, CHANNEL_COL_SERVER_IP_ADR,
			// CHANNEL_COL_PORT, CHANNEL_COL_CATEGORY }, null, null, null, null,
			// CHANNEL_COL_NAME);
			c = db.query(CHANNEL_TABLE, projection, selection, selectionArgs,
					null, null, CHANNEL_COL_NAME);
			break;
		case CHANNEL_DISTINCT:
			sqlBuilder.setTables(CHANNEL_TABLE);
			sqlBuilder.setDistinct(true);
			c = sqlBuilder.query(db, new String[] { CHANNEL_COL_CATEGORY },
					null, null, null, null, sortOrder);
			break;

		case CHANNEL_CHAR:
			sqlBuilder.setTables(CHANNEL_TABLE);
			String name = uri.getLastPathSegment();
			c = db.query(CHANNEL_TABLE, projection, CHANNEL_COL_NAME + " = '"
					+ name + "'", null, null, null, null);
			if (c != null) {
				if (c.getCount() != 0) {
					c.moveToFirst();
				}
			}
			break;
		case CHANNEL_CATEGORY:
			c = db.query(CHANNEL_CATEGORY_TABLE, projection, selection,
					selectionArgs, null, null, CHANNEL_CATEGORY_COL_NAME);
			break;

		case FB_GROUP:
			sqlBuilder.setTables(FB_GROUPS_TABLE);
			c = sqlBuilder.query(db, projection, selection, selectionArgs,
					null, null, sortOrder);
			if (c != null) {
				c.moveToFirst();
			}
			break;
		case FB_GROUP_ID_STRING:
			sqlBuilder.setTables(FB_GROUPS_TABLE);
			id = uri.getLastPathSegment();
			c = db.query(FB_GROUPS_TABLE, projection, FB_GROUP_COL_ID + " = '"
					+ id + "'", null, null, null, null);
			if (c != null) {
				if (c.getCount() != 0) {
					c.moveToFirst();
				}
			}
			break;
		case PRIVATE_GROUP:
			sqlBuilder.setTables(PRIVATE_GROUP_TABLE);
			c = sqlBuilder.query(db, projection, selection, selectionArgs,
					null, null, sortOrder);
			if (c != null) {
				c.moveToFirst();
			}
			break;
		case GROUP_MEMBERS:
			sqlBuilder.setTables(GROUP_MEMBERS_TABLE);
			c = sqlBuilder.query(db, projection, selection, selectionArgs,
					null, null, sortOrder);
			if (c != null) {
				c.moveToFirst();
			}
			break;
		case CALL_HISTORY:
			sqlBuilder.setTables(CALL_HISTORY_TABLE);
			c = sqlBuilder.query(db, projection, selection, selectionArgs,
					null, null, CALL_HISTORY_COL_TIMESTAMP + " DESC");
			if (c != null) {
				c.moveToFirst();
			}
			break;
		case TRANSCRIPT:
			sqlBuilder.setTables(TRANSCRIPT_TABLE);
			c = sqlBuilder.query(db, projection, selection, selectionArgs,
					null, null, TRANSCRIPT_COL_TIMESTAMP + " ASC");
			if (c != null) {
				c.moveToFirst();
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// ---register to watch a content URI for changes---
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int count = 0;
		String id = null;
		switch (uriMatcher.match(uri)) {
		case SERVER:
			id = uri.getLastPathSegment();
			count = db.update(SERVER_TABLE, values, SERVER_COL_ID + "=" + id,
					null);
			break;

		case SERVER_ID:
			id = uri.getLastPathSegment();
			count = db.update(SERVER_TABLE, values, SERVER_COL_ID + "=" + id,
					null);
			break;

		case USER:
			count = db.update(USER_TABLE, values, null, null);
			break;

		case USER_ID:
			id = uri.getLastPathSegment();
			count = db.update(USER_TABLE, values, USER_COL_ID + "=?",
					new String[] { id });
			break;
		// case MARKDeletedToAllUsers:
		// final ContentValues values = new ContentValues();
		// values.put(USER_COL_DELETED, deleted);
		// count = db.update( USER_TABLE, values, null, null);
		// break;

		case CHANNEL_CHAR:
			id = uri.getLastPathSegment();
			count = db.update(CHANNEL_TABLE, values, CHANNEL_COL_NAME + "=?",
					new String[] { id });
			break;
		case CHANNEL_CATEGORY:
			count = db.update(CHANNEL_CATEGORY_TABLE, values, selection,
					selectionArgs);
			break;
		case FB_GROUP:
			count = db
					.update(FB_GROUPS_TABLE, values, selection, selectionArgs);
			break;
		case FB_GROUP_ID_STRING:
			id = uri.getLastPathSegment();
			count = db.update(FB_GROUPS_TABLE, values, FB_GROUP_COL_ID + "=?",
					new String[] { id });
			break;
		case PRIVATE_GROUP:
			count = db.update(PRIVATE_GROUP_TABLE, values, selection,
					selectionArgs);
			break;
		case PRIVATE_GROUP_ID_NUM:
			id = uri.getLastPathSegment();
			count = db.update(PRIVATE_GROUP_TABLE, values, PRIVATE_GROUP_COL_ID
					+ "=?", new String[] { id });
			break;
		case GROUP_MEMBERS:
			count = db.update(GROUP_MEMBERS_TABLE, values, selection,
					selectionArgs);
			break;
		case CALL_HISTORY:
			count = db.update(CALL_HISTORY_TABLE, values, selection,
					selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}