package com.example.carassistant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chahinem.pageindicator.PageIndicator;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class MainActivity extends AppCompatActivity implements RecognitionListener {
    private static final String MENU_SEARCH = "menu";

    private ArrayList<String> locations = new ArrayList<>(Arrays.asList("beau bassin", "rosehill", "portlouis", "reduit", "curepipe", "moka"));
    private ArrayList<String> contacts = new ArrayList<>(Arrays.asList("lakaz", "girish", "davish", "nabeel", "paul"));
    private ArrayList<String> songs = new ArrayList<>(Arrays.asList("laglwar", "confians"));

    // Weather skill command - letan
    private boolean weatherFlag = false;

    // Call skill command - apel
    private boolean callFlag = false;

    // Contacts - lakaz, girish, davish, nabeel, paul
    private boolean contactFlag = false;

    // Music skill command - sante, mizik
    private boolean musicFlag = false;

    // Navigation skill command - direksyon
    private boolean navigationFlag = false;

    // Search skill command - restoran
    private boolean restaurantFlag = false;

    // Message skill command - messaz
    private boolean messageSkillFlag = false;

    private boolean messageBodyFlag = false;
    private boolean confirmFlag = false;

    private static final int PERMISSIONS_RECORD_AUDIO = 1;
    private static final int PERMISSIONS_REQUEST_CALL = 2;
    private static final int PERMISSIONS_SEND_SMS = 3;

    private static final String TAG = "MainActivity";

    private TextView textViewMic;

    private SpeechRecognizer recognizer;

    private TextToSpeech tts;

    private Handler handler = new Handler();

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize text to speech
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.FRENCH);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, "onInit: Language not supported");
                    }
                } else {
                    Log.e(TAG, "onInit: Initialization failed");
                }
            }
        });

        // Enable night mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        // Reference
        textViewMic = findViewById(R.id.text_view_mic);

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        ArrayList<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItem(R.mipmap.ic_sun, "Letan"));
        menuItems.add(new MenuItem(R.mipmap.ic_navigation, "Direksyon"));
        menuItems.add(new MenuItem(R.mipmap.ic_music, "La Mizik"));
        menuItems.add(new MenuItem(R.mipmap.ic_phone, "Telefonn"));
        menuItems.add(new MenuItem(R.mipmap.ic_search, "Resers"));
        menuItems.add(new MenuItem(R.mipmap.ic_traffic, "Trafik"));
        menuItems.add(new MenuItem(R.mipmap.ic_message, "Messaz"));
        menuItems.add(new MenuItem(R.mipmap.ic_radio, "Radio"));

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 3);
        MenuItemAdapter adapter = new MenuItemAdapter(menuItems);

        adapter.setOnItemClickListener(new MenuItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String text = menuItems.get(position).getText();

                if (text.equals("Letan")) {
                    Intent intent = new Intent(MainActivity.this, WeatherActivity.class);
                    intent.putExtra("locationName", "current");
                    startActivity(intent);
                } else if (text.equals("Direksyon")) {
                    Intent intent = new Intent(MainActivity.this, NavigationActivity.class);
                    startActivity(intent);
                } else if (text.equals("La Mizik")) {
                    Intent intent = new Intent(MainActivity.this, MusicActivity.class);
                    startActivity(intent);
                } else if (text.equals("Telefonn")) {
                    Intent intent = new Intent(MainActivity.this, CallActivity.class);
                    startActivity(intent);
                } else if (text.equals("Trafik")) {

                } else if (text.equals("Radio")) {
                    Intent intent = getPackageManager().getLaunchIntentForPackage("mu.app.topfm");

                    if (intent != null) {
                        startActivity(intent);
                    }
                }
            }
        });

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // Check if user has given permission to record audio
        int permissionRecordAudioCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (permissionRecordAudioCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_RECORD_AUDIO);
            return;
        }

        // Check if user has given permission to make phone call
        int permissionRequestCallCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
        if (permissionRequestCallCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSIONS_REQUEST_CALL);
        }

        // Check is user has given permission to send SMS
        int permissionSendSmsCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        if (permissionSendSmsCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSIONS_SEND_SMS);
        }

        new SetupTask(this).execute();
    }

    // Enable or disable speech recognition
    public void enableDisableASR(View view) {
        ImageView imageView = (ImageView) view;

        if (imageView.getTag().toString().equals("purple")) {
            // Disable speech recognition
            recognizer.stop();
            imageView.setTag("white");
            imageView.setImageResource(R.drawable.ic_mic_disabled);
        } else {
            // Enable speech recognition
            recognizer.startListening(MENU_SEARCH);
            imageView.setTag("purple");
            imageView.setImageResource(R.drawable.ic_mic_enabled);
            speak("Bonzour kouma mo kapav aide ou?");
        }
    }

    private static class SetupTask extends AsyncTask<Void, Void, Exception> {
        WeakReference<MainActivity> activityReference;

        SetupTask(MainActivity activity) {
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        protected Exception doInBackground(Void... voids) {
            try {
                Assets assets = new Assets(activityReference.get());
                File assetDir = assets.syncAssets();
                activityReference.get().setUpRecognizer(assetDir);
            } catch (IOException e) {
                return e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Exception e) {
            if (e != null) {
                Log.d(TAG, "Failed to init recognizer " + e);
            } else {
                activityReference.get().switchSearch(MENU_SEARCH);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new SetupTask(this).execute();
            } else {
                Toast.makeText(this, "Record audio permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        if (requestCode == PERMISSIONS_REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "Call phone permission denied", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == PERMISSIONS_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "Send SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        super.onDestroy();

        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {
        if (!recognizer.getSearchName().equals(MENU_SEARCH)) {
            switchSearch(MENU_SEARCH);
        }
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null) {
            return;
        }

        String text = hypothesis.getHypstr();

        // Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        Log.d(TAG, text);
        textViewMic.setText(text);

        // Weather skill
        if (weatherFlag) {
            for (String location : locations) {
                if (text.contains(location)) {
                    weatherFlag = false;
                    Intent intent = new Intent(this, WeatherActivity.class);
                    intent.putExtra("locationName", location);
                    startActivity(intent);
                    recognizer.stop();
                }
            }

            if (!containsLocation(text)) {
                // Prompt the user for a location
                speak("Nouvel les temps dans qui l'endroit");
                recognizer.stop();
                sleep(500);
                recognizer.startListening(MENU_SEARCH);
            }
        }

        if (text.contains("letan") && !weatherFlag) {
            weatherFlag = true;
        }

        // Call skill
        if (callFlag) {
            for (String contact : contacts) {
                if (text.contains(contact)) {
                    callFlag = false;
                    String capitalized = contact.substring(0, 1).toUpperCase() + contact.substring(1);
                    makePhoneCall(capitalized);
                    recognizer.stop();
                    speak("Mo p telefonn " + contact);
                }
            }

            if (!containsContact(text)) {
                // Prompt the user for the name of a person
                speak("Appel kisannla?");
                recognizer.stop();
                sleep(500);
                recognizer.startListening(MENU_SEARCH);
            }
        }

        if ((text.contains("apel") || text.contains("telefonn")) && !callFlag) {
            callFlag = true;
        }

        // Music skill
        if (musicFlag) {
            for (String song : songs) {
                if (text.contains(song)) {
                    musicFlag = false;
                    String capitalized = song.substring(0, 1).toUpperCase() + song.substring(1);
                    playSong(capitalized);
                    recognizer.stop();
                    speak("Mo p zouer santé " + song);
                }
            }

            if (!containsSong(text)) {
                // Prompt the user for the name of a song
                speak("Zouer qui santé?");
                recognizer.stop();
                sleep(500);
                recognizer.startListening(MENU_SEARCH);
            }
        }

        if ((text.contains("sante") || text.contains("mizik")) && !musicFlag) {
            musicFlag = true;
        }

        // Radio skill
        if (text.contains("radio")) {
            Intent intent = getPackageManager().getLaunchIntentForPackage("mu.app.topfm");

            if (intent != null) {
                startActivity(intent);
            }
        }

        // Navigation skill
        if (navigationFlag) {
            for (String location : locations) {
                if (text.contains(location)) {
                    navigationFlag = false;
                    showDirections(location);
                    recognizer.stop();
                    speak("Direction pou alle " + location);
                }
            }

            if (!containsLocation(text)) {
                // Prompt the user for a location
                speak("Direction poux alle qui l'endroit?");
                recognizer.stop();
                sleep(600);
                recognizer.startListening(MENU_SEARCH);
            }
        }

        if (text.contains("direksyon") && !navigationFlag) {
            navigationFlag = true;
        }

        // Search skill
        if (restaurantFlag) {
            for (String location : locations) {
                if (text.contains(location)) {
                    restaurantFlag = false;
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    CollectionReference locationsRef = db.collection("Locations");
                    locationsRef.whereEqualTo("name", location)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        Location location = documentSnapshot.toObject(Location.class);
                                        double lat = location.getLat();
                                        double lon = location.getLon();
                                        searchLocation(lat, lon, "restaurant");
                                        recognizer.stop();
                                        speak("Bann restaurant " + location.getName());
                                    }
                                }
                            });
                }
            }

            if (!containsLocation(text)) {
                // Prompt the user for a location
                speak("Restaurant dans qui l'endroit?");
                recognizer.stop();
                sleep(500);
                recognizer.startListening(MENU_SEARCH);
            }
        }

        if (text.contains("restoran") && !restaurantFlag) {
            restaurantFlag = true;
        }

        // Message skill
        if (text.contains("messaz") && !messageSkillFlag) {
            messageSkillFlag = true;
            if (!containsContact(text)) {
                // Prompt the user for the name of a person
                speak("Avoy enn messaz kisannla?");
                recognizer.stop();
                sleep(700);
                recognizer.startListening(MENU_SEARCH);
            }
        }

        if (messageSkillFlag && !contactFlag) {
            for (String contact : contacts) {
                if (text.contains(contact) && !contactFlag) {
                    contactFlag = true;

                    // Save the name of the contact
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("messageRecipient", contact);
                    editor.apply();

                    // Prompt the user for a message
                    speak("Qui messaz poux avoy " + contact + "?");
                    recognizer.stop();
                    sleep(500);
                    recognizer.startListening(MENU_SEARCH);
                }
            }
        }

        if (contactFlag && !messageBodyFlag) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Get message
                    String message = textViewMic.getText().toString();

                    // Save message
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("messageBody", message);
                    editor.apply();

                    speak("Eski mo avoy messaz " + message);
                    recognizer.stop();
                    sleep(2000);
                    recognizer.startListening(MENU_SEARCH);
                }
            }, 10000);

            messageBodyFlag = true;
        }

        if (messageBodyFlag) {
            if (text.contains("wi")) {
                confirmFlag = true;
                String messageRecipient = sharedPreferences.getString("messageRecipient", "");
                String messageBody = sharedPreferences.getString("messageBody", "");

                Log.d(TAG, "messageRecipient: " + messageRecipient);
                Log.d(TAG, "messageBody: " + messageBody);

                // Capitalize contact name
                String capitalized = messageRecipient.substring(0, 1).toUpperCase() + messageRecipient.substring(1);

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("Contacts")
                        .whereEqualTo("name", capitalized)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    Contact contact = documentSnapshot.toObject(Contact.class);

                                    // Get phone number
                                    String phone = contact.getPhone();

                                    // Send message
                                    SmsManager smsManager = SmsManager.getDefault();
                                    smsManager.sendTextMessage(phone, null, messageBody, null, null);

                                    speak("Mo fini avoy messaz la " + messageRecipient);
                                    recognizer.stop();
                                    sleep(500);
                                    recognizer.startListening(MENU_SEARCH);
                                }
                            }
                        });

                messageSkillFlag = false;
                contactFlag = false;
                messageBodyFlag = false;
                confirmFlag = false;
            } else if (text.contains("non")) {
                messageSkillFlag = false;
                contactFlag = false;
                messageBodyFlag = false;
                confirmFlag = false;
            }
        }

        // Prompt the user
        if (text.contains("zwe") && !text.contains("sante") && !text.contains("mizik") && !text.contains("radio")) {
            if (text.contains("zwe")) {
                speak("Zouer qui zafer?");
                recognizer.stop();
                sleep(500);
                recognizer.startListening(MENU_SEARCH);
            } else if (text.contains("met")) {
                speak("Mett qui zafer?");
                recognizer.stop();
                sleep(500);
                recognizer.startListening(MENU_SEARCH);
            }
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
        }
    }

    @Override
    public void onError(Exception e) {
        Log.d(TAG, e.getMessage());
    }

    @Override
    public void onTimeout() {
        switchSearch(MENU_SEARCH);
        textViewMic.setText("");
    }

    private void switchSearch(String searchName) {
        recognizer.stop();

        if (searchName.equals(MENU_SEARCH)) {
            recognizer.startListening(searchName);
        } else {
            recognizer.startListening(searchName, 10000);
        }
    }

    private void setUpRecognizer(File assetsDir) throws IOException {
        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "cmusphinx-fr-ptm-5.2-adapt"))
                .setDictionary(new File(assetsDir, "fr.dict"))
                .setRawLogDir(assetsDir)
                .getRecognizer();

        recognizer.addListener(this);

        File languageModelFile = new File(assetsDir, "lm.lm");
        recognizer.addNgramSearch(MENU_SEARCH, languageModelFile);
    }

    private void makePhoneCall(String name) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Contacts")
                .whereEqualTo("name", name)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Contact contact = documentSnapshot.toObject(Contact.class);
                            String phone = contact.getPhone();
                            String dial = "tel:" + phone;
                            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
                        }
                    }
                });
    }

    private void playSong(String name) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Songs")
                .whereEqualTo("name", name)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Song song = documentSnapshot.toObject(Song.class);
                            Intent intent = new Intent(MainActivity.this, PlayActivity.class);
                            intent.putExtra("name", name);
                            intent.putExtra("imageUrl", song.getImageUrl());
                            intent.putExtra("fileUrl", song.getFileUrl());
                            startActivity(intent);
                        }
                    }
                });
    }

    private void showDirections(String destination) {
        try {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + destination);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        } catch (ActivityNotFoundException e) {
            // Google Maps app is not installed
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    private void searchLocation(double lat, double lon, String location) {
        Log.d(TAG, "searchLocation: ");
        try {
            Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lon + "?q=" + location);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        } catch (ActivityNotFoundException e) {
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    // Stops the recognizer for some milliseconds and then restarts it
    private void delay(int milliseconds) {
        recognizer.stop();
        handler.postDelayed(runnable, milliseconds);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // After delay
            recognizer.startListening(MENU_SEARCH);
        }
    };

    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Log.d(TAG, "sleep: " + e.getMessage());
        }
    }

    // Method to send a message
    @SuppressLint("QueryPermissionsNeeded")
    private void composeMmsMessage(String message, String name) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setData(Uri.parse("smsto:")); // This ensures only SMS apps respond
        intent.putExtra("sms_body", message);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void speak(String text) {
        float pitch = 1f;
        float speed = 1f;

        tts.setPitch(pitch);
        tts.setSpeechRate(speed);

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        recognizer.startListening(MENU_SEARCH);
    }

    // Check if hypothesis text contains a location
    private boolean containsLocation(String text) {
        for (String location : locations) {
            if (text.contains(location)) {
                return true;
            }
        }
        return false;
    }

    // Check if hypothesis text contains a contact
    private boolean containsContact(String text) {
        for (String contact : contacts) {
            if (text.contains(contact)) {
                return true;
            }
        }
        return false;
    }

    // Check if hypothesis text contains a song
    private boolean containsSong(String text) {
        for (String song : songs) {
            if (text.contains(song)) {
                return true;
            }
        }
        return false;
    }
}