package com.skku.grad;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class TTSActivity extends AppCompatActivity {
    private final Bundle params = new Bundle();
    private TextToSpeech tts;
    private Button btn;
    private Button btn2;
    private Button btn3;
    private Button btn4;
    private Button btn5, btn6;

    private static final String CHANNEL_ID = "channelId";
    private static final int notificationId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("#onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts);

        //TTS
        btn = findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = "이번역은 성균관대역입니다";
                tts.speak(text, TextToSpeech.QUEUE_ADD, params, text);
            }
        });
        initTTS();

        //notify
        btn2 = findViewById(R.id.button2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doNotify();
            }
        });

        //service start
        btn3 = findViewById(R.id.button3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MyService.class);
                intent.putExtra("name", "gildong");
                startService(intent);
            }
        });

        //location service start
        btn4 = findViewById(R.id.button4);
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LocService.class);
                intent.setAction(ACTION_START_LOCATION_SERVICE);
                startService(intent);
            }
        });

        //location service stop
        btn5 = findViewById(R.id.button5);
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LocService.class);
                intent.setAction(ACTION_START_LOCATION_SERVICE);
                stopLocationService();
            }
        });

        //stt test
        btn6 = findViewById(R.id.button6);
        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecord();
            }
        });

    }

    @Override
    protected void onResume() {
        System.out.println("#onResume");
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("custom-event-name"));
    }

    @Override
    protected void onPause() {
        System.out.println("#onPause");
        super.onPause();
        // Unregister since the activity is paused.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                mMessageReceiver);
    }

    @Override
    protected void onDestroy() {
        tts.stop();
        tts.shutdown();

        System.out.println("#onDestroy");
        Intent intent = new Intent(getApplicationContext(), LocService.class);
        intent.setAction(ACTION_START_LOCATION_SERVICE);
        stopLocationService();

        super.onDestroy();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            System.out.println("#message reciever: " + message);
        }
    };

    private static final String ACTION_START_LOCATION_SERVICE = "startLocationService";
    private static final String ACTION_STOP_LOCATION_SERVICE = "stopLocationService";

    private void stopLocationService() {
        if (isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocService.class);
            intent.setAction(ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "Location service stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (LocService.class.getName().equals(service.service.getClassName())) {
                    if (service.foreground) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    //notify 테스트코드
    private void doNotify() {
        createNotificationChannel();

        Intent intent = new Intent(this, TTSActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("My notification")
                .setContentText("Hello World!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, builder.build());
        //notificationManager.
    }

    private void createNotificationChannel() {
        // Android8.0 이상인지 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "10";
            String description = "channel description";

            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    //TTS 테스트코드
    private void initTTS() {
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, null);
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int state) {
                if (state == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.KOREAN);
                } else {
                    //showState("TTS 객체 초기화 중 문제가 발생했습니다.");
                    System.out.println("#tts error");
                }
            }
        });

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
            }

            @Override
            public void onDone(String s) {
                //clearAll();
            }

            @Override
            public void onError(String s) {
                //showState("재생 중 에러가 발생했습니다.");
            }

            @Override
            public void onRangeStart(String utteranceId, int start, int end, int frame) {
            }
        });
    }


    //code for service start test
    @Override
    protected void onNewIntent(Intent intent) {
        System.out.println("#onNewIntent");
        processIntent(intent);
        super.onNewIntent(intent);
    }

    private void processIntent(Intent intent) {
        if (intent == null) {
            ;
        } else {
            String temp = intent.getStringExtra("temp");
            System.out.println("#processIntent: " + temp);
        }
    }

    //code for BroadcastReceiver test
    private void sendMessageTest(double latitude, double longitude) {
        System.out.println("#sendMessage2");
        Intent intent = new Intent(this, TestBroadcastReceiver.class);
        intent.setAction("test.exam");
        intent.putExtra("message", "This is location message2!");
        sendBroadcast(intent);
    }

    //code for stt
    private Intent sttIntent;
    private SpeechRecognizer speechRecognizer;
    private void startRecord() {
        System.out.println("#startRecord");
        checkPermission();
        //RecognizerIntent 객체 생성
        sttIntent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        sttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");   //한국어

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        speechRecognizer.setRecognitionListener(listener);
        speechRecognizer.startListening(sttIntent);
    }

    final int PERMISSION = 1;
    private void checkPermission() {
        //안드로이드 버전이 6.0 이상
        if ( Build.VERSION.SDK_INT >= 23 ){
            //인터넷이나 녹음 권한이 없으면 권한 요청
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED ) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.INTERNET,
                                Manifest.permission.RECORD_AUDIO},PERMISSION);
            }
        }
    }

    RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
        }
        @Override
        public void onBeginningOfSpeech() {
            //사용자가 말하기 시작
        }
        @Override
        public void onRmsChanged(float v) {
        }
        @Override
        public void onBufferReceived(byte[] bytes) {
        }
        @Override
        public void onEndOfSpeech() {
            //사용자가 말을 멈추면 호출
            //인식 결과에 따라 onError나 onResults가 호출됨
        }
        @Override
        public void onError(int error) {    //에러 출력
            String message;
            System.out.println("stt error: "+error);
        }
        @Override
        public void onResults(Bundle bundle) {
            ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);	//인식 결과를 담은 ArrayList
            //String originText = contents.getText().toString();  //기존 text
            //인식 결과
            String newText="";
            for (int i = 0; i < matches.size() ; i++) {
                newText += matches.get(i);
            }
            System.out.println("#stt result: "+newText);
            //contents.setText(originText + newText + " ");	//기존의 text에 인식 결과를 이어붙임
            //speechRecognizer.startListening(intent);    //녹음버튼을 누를 때까지 계속 녹음해야 하므로 녹음 재개
        }
        @Override
        public void onPartialResults(Bundle bundle) {
        }
        @Override
        public void onEvent(int i, Bundle bundle) {
        }
    };
}