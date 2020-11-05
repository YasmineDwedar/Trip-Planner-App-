package com.vaatu.tripmate.ui.home.addButtonActivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.vaatu.tripmate.R;
import com.vaatu.tripmate.utils.TripModel;
import com.vaatu.tripmate.utils.alarmManagerReciever.AlarmEventReciever;
import com.vaatu.tripmate.utils.dateTimePicker.DatePickerFragment;
import com.vaatu.tripmate.utils.dateTimePicker.TimePickerFragment;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddBtnActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    public static final String NEW_TRIP_OBJECT = "NEW_TRIP_OBJECT";
    public static final String NEW_TRIP_OBJ_SERIAL = "NEW_TRIP_OBJECT";

    @BindView(R.id.add_trip_btn)
    Button addTripBtn;
    @BindView(R.id.cancel_btn)
    Button cancelBtn;
    @BindView(R.id.repeat_spinner)
    Spinner repeatSpinner;
    @BindView(R.id.trip_way_spinner)
    Spinner tripWaySpinner;
    @BindView(R.id.repeat_spin_linearlayout)
    LinearLayout repeatSpinLinearlayout;
    @BindView(R.id.add_note_btn)
    ImageButton addNoteBtn;
    @BindView(R.id.note_text_field)
    TextInputLayout noteTextField;
    @BindView(R.id.notes_linearLayout)
    LinearLayout notesLinearLayout;
    @BindView(R.id.dateTextField)
    TextInputEditText dateTextField;
    @BindView(R.id.timeTextField)
    TextInputEditText timeTextField;
    @BindView(R.id.linearLayout)
    LinearLayout linearLayout;
    @BindView(R.id.trip_name_text_field)
    TextInputLayout tripNameTextField;

    PlacesClient mPlacesClient;
    List<Place.Field> placeField = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS);
    int hour;
    int minutes;
    int increasedID = 0;
    ArrayAdapter<CharSequence> adapterTripDirectionSpin;
    ArrayAdapter<CharSequence> adapterTripRepeatSpin;
    List<TextInputLayout> mNotesTextInputLayout = new ArrayList<>();
    String selectedStartPlace = "";
    String selectedEndPlace = "";
    List<String> notesList = new ArrayList<>();

    AlarmManager alarmManager;
    PendingIntent pendingIntent;

    Calendar mCalendar;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_btn);
        ButterKnife.bind(this);
        mCalendar = Calendar.getInstance();
        hideProgressBar();

        //Auto Complete Google
        setUpAutoComplete();

        //Spinner init
        spinnerInit();

        // add first Note to mNotesTextInputLayout !
        mNotesTextInputLayout.add(noteTextField);
    }

    private void setUpAutoComplete() {
        AutocompleteSupportFragment placeStartPointAutoComplete;
        AutocompleteSupportFragment placeDestPointAutoComplete;
        if (!Places.isInitialized()) {
            // @TODO Get Places API key
            Places.initialize(getApplicationContext(), "AIzaSyDbQxlvW4q0t1rhRHicRHVDzWbDP8y1Hlc");
        }
        //Init Frags
        placeStartPointAutoComplete = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.start_autoComplete_Frag);
        placeStartPointAutoComplete.setPlaceFields(placeField);

        placeDestPointAutoComplete = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.dest_autoComplete_Frag);
        placeDestPointAutoComplete.setPlaceFields(placeField);

        placeStartPointAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i("Places", "Place: " + place.getAddress() + ", " + place.getId());
                selectedStartPlace = place.getAddress();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Places", "An error occurred: " + status);
            }
        });
        placeDestPointAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i("Places", "Place: " + place.getAddress() + ", " + place.getId());
                selectedEndPlace = place.getAddress();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Places", "An error occurred: " + status);
            }
        });

    }

    @OnClick({R.id.add_trip_btn, R.id.add_note_btn, R.id.dateTextField, R.id.timeTextField,R.id.cancel_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.add_trip_btn:
                showProgressBar();
                //@TODO Copy this to another place !
                for (TextInputLayout txtLayout : mNotesTextInputLayout) {
                    Log.i("Notes List", txtLayout.getEditText().getText().toString());
                    notesList.add(txtLayout.getEditText().getText().toString());
                }
                if (tripNameTextField.getEditText().getText().toString().equals("")) {
                    tripNameTextField.setError("Cannot be blank!");
                } else if (dateTextField.getText().toString().equals("")) {
                    dateTextField.setError("Cannot be blank!");
                } else if (timeTextField.getText().toString().equals("")) {
                    timeTextField.setError("Cannot be blank!");
                } else {
                    TripModel newTrip = new TripModel(selectedStartPlace, selectedEndPlace, dateTextField.getText().toString(),
                            timeTextField.getText().toString(), tripNameTextField.getEditText().getText().toString(), null, notesList, mCalendar.getTime().toString());

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("NEWTRIP", newTrip);
                    startAlarm(newTrip);
                    setResult(Activity.RESULT_OK, resultIntent);
                    Toast.makeText(this, "Added Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case R.id.add_note_btn:
                generateNoteLayout(view);
                break;
            case R.id.dateTextField:
                DialogFragment datepicker = new DatePickerFragment();

                datepicker.show(getSupportFragmentManager(), "date");

                break;
            case R.id.timeTextField:
                DialogFragment timepicker = new TimePickerFragment();
                timepicker.show(getSupportFragmentManager(), "time");
                break;
            case R.id.cancel_btn:
                finish();
                break;
        }
    }

    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, i);
        c.set(Calendar.MONTH, i1);
        c.set(Calendar.DAY_OF_MONTH, i2);
        String currentDateString = DateFormat.getDateInstance(DateFormat.FULL).format(c.getTime());
        Log.i("Date Time Picker", currentDateString);
        dateTextField.setText(currentDateString);
        mCalendar.set(Calendar.YEAR, i);
        mCalendar.set(Calendar.MONTH, i1); // Month is zero-based
        mCalendar.set(Calendar.DAY_OF_MONTH, i2);

    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {

        hour = i;
        minutes = i1;
        String timeSet = "";
        if (hour > 12) {
            hour -= 12;
            timeSet = "PM";
        } else if (hour == 0) {
            hour += 12;
            timeSet = "AM";
        } else if (hour == 12) {
            timeSet = "PM";
        } else {
            timeSet = "AM";
        }

        String min = "";
        if (minutes < 10)
            min = "0" + minutes;
        else
            min = String.valueOf(minutes);

        // Append in a StringBuilder
        String aTime = new StringBuilder().append(hour).append(':')
                .append(min).append(" ").append(timeSet).toString();
        timeTextField.setText(aTime);

        // Set calendat item
        mCalendar = Calendar.getInstance();


        mCalendar.set(Calendar.HOUR_OF_DAY, i);
        mCalendar.set(Calendar.MINUTE, i1);
        mCalendar.set(Calendar.SECOND, 0);

    }

    private void spinnerInit() {
        //Trip Direction Spinner
        adapterTripDirectionSpin = ArrayAdapter.createFromResource(this,
                R.array.trip_direction_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterTripDirectionSpin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        tripWaySpinner.setAdapter(adapterTripDirectionSpin);
        tripWaySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                Log.i("Spinner", adapterView.getItemAtPosition(pos).toString() + "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Trip Repeat Spinner
        adapterTripRepeatSpin = ArrayAdapter.createFromResource(this,
                R.array.times_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterTripRepeatSpin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        repeatSpinner.setAdapter(adapterTripRepeatSpin);
        repeatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                Log.i("Spinner", adapterView.getItemAtPosition(pos).toString() + "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void generateNoteLayout(View view) {
        LinearLayout currentParent = findViewById(R.id.notes_parent_linear_Layout);

        View linearLayout = getLayoutInflater().inflate(R.layout.add_notes_sayout_sample, null);

        TextInputLayout noteTextInput = linearLayout.findViewById(R.id.note_text_field_input);
        mNotesTextInputLayout.add(noteTextInput);

        ImageButton subImgBtn = linearLayout.findViewById(R.id.sub_note_img_btn);
        subImgBtn.setOnClickListener(v -> {
            currentParent.removeView(linearLayout);
            mNotesTextInputLayout.remove(noteTextInput);
        });

        currentParent.addView(linearLayout);
        increasedID++;

    }

    private void startAlarm(TripModel tripModel) {

        alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
//        Log.i("time", mCalendar.getTime().toString());
//        long alarmTime = mCalendar.getTimeInMillis();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        try {
            cal.setTime(sdf.parse(tripModel.dateTime));// all done
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(this, AlarmEventReciever.class);
        intent.putExtra(NEW_TRIP_OBJECT, tripModel);

        Bundle b = new Bundle();
        b.putSerializable(AddBtnActivity.NEW_TRIP_OBJ_SERIAL, tripModel);
        intent.putExtra(NEW_TRIP_OBJECT, b);


        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarmManager.set(AlarmManager.RTC, cal.getTimeInMillis(), pendingIntent);
    }


    private void cancelAlarm() {
        alarmManager.cancel(pendingIntent);
        Toast.makeText(getApplicationContext(), "Alarm Cancelled", Toast.LENGTH_LONG).show();
    }


    private void hideProgressBar(){
        progressBar.setVisibility(View.VISIBLE);
    }
    private void showProgressBar(){
        progressBar.setVisibility(View.INVISIBLE);
    }


}
