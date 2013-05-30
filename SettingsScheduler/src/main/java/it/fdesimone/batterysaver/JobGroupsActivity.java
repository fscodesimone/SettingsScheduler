package it.fdesimone.batterysaver;

import java.util.Calendar;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class JobGroupsActivity extends Activity {
    
	private LayoutInflater mInflater;
	private Cursor mCursor;
	
	private Bitmap icon_green;
    private Bitmap icon_gray;
    private Bitmap icon_arrow;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jobgroups);
        mInflater = LayoutInflater.from(this);
        if ( mCursor != null )
        	mCursor.close();
        mCursor = JobGroups.getJobGroupsCursor(getContentResolver());
        
        ListView view = (ListView) findViewById(R.id.jgroups_list);
        
        view.setAdapter(new JobGroupsListAdapter(this, mCursor));
        
        Resources res = getResources();
        
        icon_green = BitmapFactory.decodeResource(res, R.drawable.on);
        icon_gray = BitmapFactory.decodeResource(res, R.drawable.off);
        //icon_arrow = BitmapFactory.decodeResource(res, R.drawable.ic_launcher);
        
        view.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent = new Intent(getApplicationContext(), JobGroupActivity.class);
		        intent.putExtra(JobGroup.JOBGROUP_ID, (int) arg3);
		        startActivity(intent);
			}
        	
		});
        
        view.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				JobGroup jb = JobGroups.getJobGroup(getContentResolver(), (int) arg3);
				jb.setEnabled(!jb.isEnabled());
				
				ContentValues cv = new ContentValues(1);
				cv.put(JobGroup.Columns.ENABLED, jb.isEnabled() ? 1 : 0);
				JobGroups.updateJobGroup(getApplicationContext(), (int) arg3, cv);
				
				return true;
			}
		
        });
        
        // We have to do this to get the save/cancel buttons to highlight on
        // their own.
        view.setItemsCanFocus(true);
        
        Button b = (Button) findViewById(R.id.jobgroups_add);
        b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    addJobGroup();
                }
        });
        
    }
	
	private void addJobGroup() {
		startActivity(new Intent(this, JobGroupActivity.class));
	}
	
	class JobGroupsListAdapter extends CursorAdapter {

		public JobGroupsListAdapter(Context context, Cursor c) {
			super(context, c);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			final JobGroup jobGroup = new JobGroup(cursor);
			
			((ImageView) view.findViewById(R.id.icon_status)).setImageBitmap(jobGroup.isEnabled() ? icon_green : icon_gray);
			
			DigitalClock digitalClock =
                (DigitalClock) view.findViewById(R.id.digitalClock);
			final Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, jobGroup.getHour());
            c.set(Calendar.MINUTE, jobGroup.getMinutes());
            digitalClock.updateTime(c);
            digitalClock.setTypeface(Typeface.DEFAULT);
			
            // Set the repeat text or leave it blank if it does not repeat.
            TextView daysOfWeekView =
                    (TextView) digitalClock.findViewById(R.id.daysOfWeek);
            final String daysOfWeekStr =
            	jobGroup.getDaysOfWeek().toString(JobGroupsActivity.this);
            if (daysOfWeekStr != null && daysOfWeekStr.length() != 0) {
                daysOfWeekView.setText(daysOfWeekStr);
                daysOfWeekView.setVisibility(View.VISIBLE);
            } else {
                daysOfWeekView.setVisibility(View.GONE);
            }

            // Display the label
            TextView labelView =
                    (TextView) view.findViewById(R.id.label);
            if (jobGroup.getLabel() != null && jobGroup.getLabel().length() != 0) {
                labelView.setText(jobGroup.getLabel());
                labelView.setVisibility(View.VISIBLE);
            } else {
                labelView.setVisibility(View.GONE);
            }

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View ret = mInflater.inflate(R.layout.jgroups_item, parent, false);
			
			DigitalClock digitalClock =
                (DigitalClock) ret.findViewById(R.id.digitalClock);
			digitalClock.setLive(false);
			
			//((ImageView) ret.findViewById(R.id.icon_arrow)).setImageBitmap(icon_arrow);
			
			return ret;
		}
		
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();

		mCursor.close();
	}

}