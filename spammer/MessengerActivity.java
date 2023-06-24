package com.example.spammer;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class MessengerActivity extends AppCompatActivity {
    private EditText editText;
    private ListView messagesView;
    private ImageButton btnSend;
    private MessageAdapter messageAdapter;
    static String message;
    private static int MAX_MSG_SIZE = 100;
    String sessionKey_str;
    SecretKeySpec sessionKey;
    int flag_change_key = 0;
    String publicKey_str;
    Crypt crypt = new Crypt();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("messages");
    DatabaseReference keysRef = database.getReference("keys");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.msg_table);
        btnSend = (ImageButton)findViewById(R.id.btnSend);
        editText = (EditText) findViewById(R.id.editText);
        messageAdapter = new MessageAdapter(this);
        messagesView = (ListView) findViewById(R.id.messages_view);
        messagesView.setAdapter(messageAdapter);

        keysRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null && flag_change_key == 0) //первый клиент закинул свой публичный ключ
                {
                    publicKey_str = crypt.generateKey();
                    keysRef.setValue(publicKey_str);//положили публичный
                    myRef.removeValue();
                    flag_change_key = 1;
                } else {

                    if (flag_change_key == 0) //второй клиент сгенерил сеансовый, зашифровал на публичном первого клиента, закинул
                    {
                        String publicKey_client = snapshot.getValue(String.class);
                
                        btnSend.setClickable(true);
                        try {
                            sessionKey_str = crypt.secretKeySpec();
                            String secretKey = crypt.encryptSecKey(publicKey_client, sessionKey_str);//зашифровали секретный публичным
                            flag_change_key = 2;
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            keysRef.setValue(secretKey);
                            sessionKey = crypt.getKeyFromEncrKey(sessionKey_str);

                        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (sessionKey_str == null && snapshot.getValue(String.class) == publicKey_str) {

                            btnSend.setClickable(false);


                        }
                        if (sessionKey_str == null && snapshot.getValue(String.class) != publicKey_str)//первый клиент забирает шифрованный сеансовый ключ и расшифровывает на своем секретном
                        {

                            btnSend.setClickable(true);

                            String secretKey = snapshot.getValue(String.class);
                            try {
                                sessionKey_str = crypt.decryptSecKey(secretKey);
                                sessionKey = crypt.getKeyFromEncrKey(sessionKey_str);
                                keysRef.removeValue();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        myRef.addChildEventListener(new ChildEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)//вызывается при push
            {
                String msg = snapshot.getValue(String.class);//шифрованное
                try {
                    String decr_msg = crypt.decryptData(msg, sessionKey);
                    onMessage(decr_msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendMessage(View view) throws IOException {

        message = editText.getText().toString();
        if (message.length() > 0 && message.length() < MAX_MSG_SIZE) {

            try {
                String encr_msg = crypt.encryptData(sessionKey, message);
                myRef.push().setValue(encr_msg);
            } catch (Exception e) {
                e.printStackTrace();
            }

            editText.getText().clear();
        } else if (message.length() >= MAX_MSG_SIZE)
            Toast.makeText(this, "So long message!", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Enter a message!", Toast.LENGTH_SHORT).show();

    }

    public void onMessage(String msg_data) {
        MemberData memberData;
        Message msg;
        if (!msg_data.equals(message)) {
            memberData = new MemberData("Somebody", "#3E3C3C");
            msg = new Message(msg_data, memberData, false);
        } else {
            memberData = new MemberData("Me", "#E00404");
            msg = new Message(msg_data, memberData, true);
        }

        messageAdapter.add(msg);
        messagesView.setSelection(messagesView.getCount() - 1);
    }


}
