package it.fdesimone.batterysaver;

import java.util.ArrayList;
import java.util.List;


import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import it.fdesimone.batterysaver.tasks.Tasks;


/**
 * @author Francesco De Simone
 */
public class JobGroupActivity extends PreferenceActivity implements TimePickerDialog.OnTimeSetListener, Preference.OnPreferenceChangeListener {

	private CheckBoxPreference enabled;
	private Preference time;
	private RepeatPreference repeat;
	private EditTextPreference label;
	
	private CheckBoxPreference
							task_enableWifi,
							task_disableWifi,
                            task_enableDataConnection,
                            task_disableDataConnection,
                            task_BluetoothOn,
                            task_BluetoothOff;
	
	private JobGroup jobGroup;
	
	private int mHour, mMinutes;
	
	private List<String> enabledTasks;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startService();
		enabledTasks = new ArrayList<String>();
		
		setContentView(R.layout.jobgroup);
		
		addPreferencesFromResource(R.xml.jobgroup_settings);
		
		int jobGroupId = getIntent().getIntExtra(JobGroup.JOBGROUP_ID, -1);
		
		if ( jobGroupId == -1 ) {
			jobGroup = new JobGroup();
		}
		else {
			jobGroup = JobGroups.getJobGroup(getContentResolver(), jobGroupId);

            if (jobGroup == null) {
                finish();
                return;
            }
		}
		
		enabled = (CheckBoxPreference) findPreference("enabled");
		enabled.setOnPreferenceChangeListener(this);
		
		time = findPreference("time");
		
		repeat = (RepeatPreference) findPreference("setRepeat");
		repeat.setOnPreferenceChangeListener(this);
		
		
		label = (EditTextPreference) findPreference("label");
		label.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				
				String val = (String) newValue;
				preference.setSummary(val);
				
				// fire change if the value is not equal to the previous one
				if ( val != null && !val.equals(label.getText()) ) {
					return JobGroupActivity.this.onPreferenceChange(preference, newValue);
				}
				return true;
			}
		});
		
		task_enableWifi = (CheckBoxPreference) findPreference(getResources().getString(R.string.task_enableWifi));
		task_enableWifi.setOnPreferenceChangeListener(this);
		
		task_disableWifi = (CheckBoxPreference) findPreference(getResources().getString(R.string.task_disableWifi));
		task_disableWifi.setOnPreferenceChangeListener(this);

        task_enableDataConnection = (CheckBoxPreference) findPreference(getResources().getString(R.string.task_enable3G));
        task_enableDataConnection.setOnPreferenceChangeListener(this);

        task_disableDataConnection = (CheckBoxPreference) findPreference(getResources().getString(R.string.task_disable3G));
        task_disableDataConnection.setOnPreferenceChangeListener(this);

        task_BluetoothOn = (CheckBoxPreference) findPreference(getResources().getString(R.string.task_BluetoothOn));
        task_BluetoothOn.setOnPreferenceChangeListener(this);

        task_BluetoothOff = (CheckBoxPreference) findPreference(getResources().getString(R.string.task_BluetoothOff));
        task_BluetoothOff.setOnPreferenceChangeListener(this);

		
		updatePrefs(jobGroup);
		
		// We have to do this to get the save/cancel buttons to highlight on
        // their own.
        getListView().setItemsCanFocus(true);

        // Attach actions to each button.
        Button b = (Button) findViewById(R.id.jobgroup_save);
        b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    saveJobGroup();
                    finish();
                }
        });
        
        final Button revert = (Button) findViewById(R.id.jobgroup_revert);
        revert.setEnabled(false);
        revert.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                	revert();
                    revert.setEnabled(false);
                }
        });
        
        b = (Button) findViewById(R.id.jobgroup_delete);
        if (jobGroupId == -1) {
            b.setEnabled(false);
        } else {
            b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    deleteJobGroup();
                }
            });
        }
	}
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if ( preference == time ) {
			showTimePicker();
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
	
	
	
	// Used to post runnables asynchronously.
    private static final Handler sHandler = new Handler();
	
	public boolean onPreferenceChange(final Preference preference, Object newValue) {
		// Asynchronously save the jobgroup since this method is called _before_
        // the value of the preference has changed.
		
        sHandler.post(new Runnable() {
            public void run() {
            	enabled.setTitle(enabled.isChecked() ? R.string.enabled_jobgroup : R.string.disabled_jobgroup);
        		enableRevert();
            }
        });
        
		return true;
	}
	
	@Override
	public void onBackPressed() {
		final Button revert = (Button) findViewById(R.id.jobgroup_revert);
		if ( revert.isEnabled() ) {
			// changes are made ... ask the user to save or cancel
			
			new AlertDialog.Builder(this)
        	.setTitle(getString(R.string.save_dialog))
        	.setMessage(getString(R.string.confirm))
        	.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        saveJobGroup();
                        finish();
                    }
                })
	        .setNegativeButton(android.R.string.cancel, 
	        		new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialog, int which) {
							revert();
							finish();
						}
			})
	        .show();
		}
		else {
			finish();
		}
	}
	
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mHour = hourOfDay;
        mMinutes = minute;
        updateTime();
        
        enableRevert();
	}
	
	private void enableRevert() {
		final Button revert = (Button) findViewById(R.id.jobgroup_revert);
        revert.setEnabled(true);
	}
	
	private void revert() {
		updatePrefs(jobGroup);
	}

	private void saveJobGroup() {
		
		if ( jobGroup.getId() == -1 ) {
			// add
			JobGroups.addJobGroup(getApplicationContext(), updateJobGroupFromCurrentView(jobGroup));
		}
		else {
			// update
			JobGroups.updateJobGroup(getApplicationContext(), updateJobGroupFromCurrentView(jobGroup));
		}
	}
	
	private void deleteJobGroup() {
		new AlertDialog.Builder(this)
        	.setTitle(getString(R.string.delete_jobgroup))
        	.setMessage(getString(R.string.confirm))
        	.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                        JobGroups.deleteJobGroup(JobGroupActivity.this, jobGroup.getId());
                        finish();
                    }
                })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
	}
	
	private JobGroup updateJobGroupFromCurrentView(JobGroup jobGroup) {
		jobGroup.setDaysOfWeek(repeat.getDaysOfWeek());
		jobGroup.setEnabled(enabled.isChecked());
		jobGroup.setHour(mHour);
		jobGroup.setMinutes(mMinutes);
		jobGroup.setLabel(label.getText());
		
		enabledTasks.clear();
		if ( task_enableWifi.isChecked() )
			enabledTasks.add(String.valueOf(Tasks.id_EnableWifiTask));
		if ( task_disableWifi.isChecked() )
			enabledTasks.add(String.valueOf(Tasks.id_DisableWifiTask));
        if ( task_disableDataConnection.isChecked() )
            enabledTasks.add(String.valueOf(Tasks.id_DisableDataConnection));
        if ( task_enableDataConnection.isChecked() )
            enabledTasks.add(String.valueOf(Tasks.id_EnableDataConnection));
        if ( task_BluetoothOff.isChecked() )
            enabledTasks.add(String.valueOf(Tasks.id_DisableBluehtooth));
        if ( task_BluetoothOn.isChecked() )
            enabledTasks.add(String.valueOf(Tasks.id_EnableBluehtooth));
		
		jobGroup.setJobActionIDs(enabledTasks);
		
		return jobGroup;
	}
	
	private void updateTime() {
		time.setSummary(JobGroups.formatTime(this, mHour, mMinutes, repeat.getDaysOfWeek()));
	}
	
	private void updatePrefs(JobGroup jobGroup) {
		mHour = jobGroup.getHour();
		mMinutes = jobGroup.getMinutes();
		enabled.setChecked(jobGroup.isEnabled());
		enabled.setTitle(jobGroup.isEnabled() ? R.string.enabled_jobgroup : R.string.disabled_jobgroup);
		repeat.setDaysOfWeek(jobGroup.getDaysOfWeek());
		label.setText(jobGroup.getLabel());
		label.setSummary(jobGroup.getLabel());
		
		task_enableWifi.setChecked(jobGroup.containsJobActionId(Tasks.id_EnableWifiTask));
		task_disableWifi.setChecked(jobGroup.containsJobActionId(Tasks.id_DisableWifiTask));
        task_disableDataConnection.setChecked(jobGroup.containsJobActionId(Tasks.id_DisableDataConnection));
        task_enableDataConnection.setChecked(jobGroup.containsJobActionId(Tasks.id_EnableDataConnection));
        task_BluetoothOn.setChecked(jobGroup.containsJobActionId(Tasks.id_EnableBluehtooth));
        task_BluetoothOff.setChecked(jobGroup.containsJobActionId(Tasks.id_DisableBluehtooth));
		updateTime();
	}
	
	private void showTimePicker() {
        new TimePickerDialog(this, this, mHour, mMinutes,
                DateFormat.is24HourFormat(this)).show();
    }



    private void startService(){

        Intent serviceIntent = new Intent(this, BatterySaverService.class);
        Utils.setServiceEnabledOnBoot(getApplicationContext());
        startService(serviceIntent);
    }
}
