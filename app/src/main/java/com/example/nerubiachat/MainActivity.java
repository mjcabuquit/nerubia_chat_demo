package com.example.nerubiachat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final View welcomeScreen = findViewById(R.id.layout_welcome);
        final View chatScreen = findViewById(R.id.layout_chat);

        welcomeScreen.setVisibility(View.VISIBLE);
        chatScreen.setVisibility(View.GONE);

        final TextInputLayout tilName = findViewById(R.id.til_name);
        findViewById(R.id.btn_cta).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                welcomeScreen.setVisibility(View.GONE);
                chatScreen.setVisibility(View.VISIBLE);
            }
        });

        MessageInput miMessage = findViewById(R.id.mi_message);
        MessagesList mlMessages = findViewById(R.id.ml_messages);

        final String userId = UUID.randomUUID().toString();
        final MessagesListAdapter<Message> adapter = new MessagesListAdapter<>(userId, new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                imageView.setImageBitmap(generateImageFrom(MainActivity.this, url));
            }
        });

        mlMessages.setAdapter(adapter);

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("messages");

        miMessage.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                Message m = new Message(UUID.randomUUID().toString(), input.toString(), new User(userId, tilName.getEditText().getText().toString()), Calendar.getInstance().getTime());
                databaseReference.child(m.getId()).setValue(m);
                return true;
            }
        });

        databaseReference.orderByChild("date").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message m = dataSnapshot.getValue(Message.class);
                if (m != null) {
                    adapter.addToStart(m, true);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static class Message implements IMessage {

        private static final SimpleDateFormat ISO_8601_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss'Z'");

        private String id;
        private String text;
        private User user;
        private String date;

        public Message() {
        }

        public Message(String id, String text, User user, Date date) {
            this.id = id;
            this.text = text;
            this.user = user;
            this.date = ISO_8601_FORMAT.format(date);
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public IUser getUser() {
            return user;
        }

        @Exclude
        @Override
        public Date getCreatedAt() {
            try {
                return ISO_8601_FORMAT.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }

        public String getDate() {
            return date;
        }
    }

    public static class User implements IUser {

        private String id;
        private String name;

        public User() {
        }

        public User(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getAvatar() {
            return getInitial();
        }

        @Exclude
        public String getInitial() {
            StringBuffer capBuffer = new StringBuffer();
            Matcher capMatcher = Pattern.compile("([a-z])([a-z/-]*)", Pattern.CASE_INSENSITIVE).matcher(getName());
            while (capMatcher.find()) {
                capBuffer.append(capMatcher.group(1).toUpperCase());
            }

            return capBuffer.length() > 2 ? capBuffer.substring(0, 2) : capBuffer.toString().trim();
        }
    }

    public static Bitmap generateImageFrom(Context context, String value) {
        int m = (int) (40f * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));

        Bitmap b = Bitmap.createBitmap(m, m, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);


        Paint bgPaint = new Paint();
        Random rnd = new Random();

        bgPaint.setColor(Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setAntiAlias(true);

        canvas.drawCircle(m / 2, m / 2, m / 2, bgPaint);


        float fontSize = (17f * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));

        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(fontSize);
        textPaint.setColor(Color.WHITE);

        int x = (int) ((m - textPaint.measureText(value)) / 2f);
        int y = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2));

        canvas.drawText(value, x, y, textPaint);

        return b;
    }
}
