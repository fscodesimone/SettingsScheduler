package it.fdesimone.batterysaver;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

import it.fdesimone.batterysaver.tasks.Tasks;

/**
 * @author Francesco De Simone
 */
public class JobGroupProvider extends ContentProvider{

	private SQLiteOpenHelper mOpenHelper;
	
	private static final int JOBGROUPS = 1;
    private static final int JOBGROUPS_ID = 2;
    private static final UriMatcher sURLMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        sURLMatcher.addURI("it.fdesimone.batterysaver", "jobgroups", JOBGROUPS);
        sURLMatcher.addURI("it.fdesimone.batterysaver", "jobgroups/#", JOBGROUPS_ID);
    }
	
    private static class DatabaseHelper extends SQLiteOpenHelper {

    	private static final String DATABASE_NAME = "batterysaver.db";
        private static final int DATABASE_VERSION = 1;
    	
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE jobgroups (" +
                    "_id INTEGER PRIMARY KEY," +
                    "hour INTEGER, " +
                    "minutes INTEGER, " +
                    "days_of_week INTEGER, " +
                    "enabled INTEGER, " +
                    "label TEXT, " +
                    "job_action_ids TEXT);");

         // insert default jobgroups
         String insertMe = "INSERT INTO jobgroups " +
                 "(hour, minutes, days_of_week, enabled, label, job_action_ids) " +
                 "VALUES ";
         db.execSQL(insertMe + "(9, 0, 31, 1, 'before work', '" + Tasks.id_EnableWifiTask + "');");
         db.execSQL(insertMe + "(18, 0, 31, 1, 'after work', '" + Tasks.id_DisableWifiTask + "');");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (Log.LOGV) Log.v(
                    "Upgrading batterysaver database from version " +
                    oldVersion + " to " + newVersion +
                    ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS jobgroups");
            onCreate(db);
		}
    	
		
    }
    
    public JobGroupProvider() {

    }
    
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		 SQLiteDatabase db = mOpenHelper.getWritableDatabase();
	        int count;
//	        long rowId = 0;
	        switch (sURLMatcher.match(uri)) {
	            case JOBGROUPS:
	                count = db.delete("jobgroups", selection, selectionArgs);
	                break;
	            case JOBGROUPS_ID:
	                String segment = uri.getPathSegments().get(1);
//	                rowId = Long.parseLong(segment);
	                if (TextUtils.isEmpty(selection)) {
	                	selection = "_id=" + segment;
	                } else {
	                	selection = "_id=" + segment + " AND (" + selection + ")";
	                }
	                count = db.delete("jobgroups", selection, selectionArgs);
	                break;
	            default:
	                throw new IllegalArgumentException("Cannot delete from URI: " + uri);
	        }

	        getContext().getContentResolver().notifyChange(uri, null);
	        return count;
	}

	@Override
	public String getType(Uri uri) {
		int match = sURLMatcher.match(uri);
        switch (match) {
            case JOBGROUPS:
                return "vnd.android.cursor.dir/jobgroups";
            case JOBGROUPS_ID:
                return "vnd.android.cursor.item/jobgroups";
            default:
                throw new IllegalArgumentException("Unknown URI");
        }
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (sURLMatcher.match(uri) != JOBGROUPS) {
            throw new IllegalArgumentException("Cannot insert into URI: " + uri);
        }

        ContentValues cv = new ContentValues(values);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert("jobgroups", JobGroup.Columns.LABEL, cv);
        if (rowId < 0) {
            throw new SQLException("Failed to insert row into " + uri);
        }
        if (Log.LOGV) Log.v("Added jobGroup rowId = " + rowId);

        Uri newUri = ContentUris.withAppendedId(JobGroup.Columns.CONTENT_URI, rowId);
        getContext().getContentResolver().notifyChange(newUri, null);
        return newUri;
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        // Generate the body of the query
        int match = sURLMatcher.match(uri);
        switch (match) {
            case JOBGROUPS:
                qb.setTables("jobgroups");
                break;
            case JOBGROUPS_ID:
                qb.setTables("jobgroups");
                qb.appendWhere("_id=");
                qb.appendWhere(uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor ret = qb.query(db, projection, selection, selectionArgs,
                              null, null, sortOrder);

        if (ret == null) {
            if (Log.LOGV) Log.v("JobGroups.query: failed");
        } else {
            ret.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return ret;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int count;
        long rowId = 0;
        int match = sURLMatcher.match(uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (match) {
            case JOBGROUPS_ID: {
                String segment = uri.getPathSegments().get(1);
                rowId = Long.parseLong(segment);
                count = db.update("jobgroups", values, "_id=" + rowId, null);
                break;
            }
            default: {
                throw new UnsupportedOperationException(
                        "Cannot update URL: " + uri);
            }
        }
        if (Log.LOGV) Log.v("*** notifyChange() rowId: " + rowId + " URI " + uri);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

}
