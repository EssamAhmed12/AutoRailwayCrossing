package com.example.railway;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<ChatMessage>  {
    int SENDER_MESSAGE = 0;
    int RECEIVER_MESSAGE = 1;
    FirebaseUser firebaseUser;
    FirebaseUserMetadata metadata;

    private List<ChatMessage> mChat;
    private String  nour_email="NpvcVFRj5FOfD7ymf2URPWsS0BX2";
    private String  nour_phone="WjEy2P1yhTU3xe54QrUre90Ac5o1";
    private String  essam_email="WjEy2P1yhTU3xe54QrUre90Ac5o1";
    private String  essam_phone="ysrqmb1asqZ8uWOkJ8ONr7qbZm33";
    private final String TAG="MessageAdapter";


    public MessageAdapter(Context context, int resource, List<ChatMessage> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();

        Log.e(TAG,"position :"+position+"parent : "+parent+":convertView :   "+convertView);
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_message_sent,null);
            //if(firebaseUser.getUid().equals(essam_email)||firebaseUser.getUid().equals(nour_email)) {
          //      convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.activity_sign_in.item_message_sent,null);
           // }else if(firebaseUser.getUid().equals(nour_phone)||firebaseUser.getUid().equals(essam_phone)){
          //      convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.activity_sign_in.item_message_received, null);
          //  }
        }

        ImageView photoImageView = convertView.findViewById(R.id.photoImageView);
        TextView messageTextView =  convertView.findViewById(R.id.messageTextView);
        TextView authorTextView = convertView.findViewById(R.id.nameTextView);
        TextView timeTextView = convertView.findViewById(R.id.text_message_time);

        ChatMessage message = getItem(position);

        boolean isPhoto = message.getPhotoUrl() != null;
        if (isPhoto) {
            messageTextView.setVisibility(View.GONE);
            photoImageView.setVisibility(View.VISIBLE);
            Glide.with(photoImageView.getContext())
                    .load(message.getPhotoUrl())
                    .into(photoImageView);
        } else {
            messageTextView.setVisibility(View.VISIBLE);
            photoImageView.setVisibility(View.GONE);
            messageTextView.setText(message.getText());
        }
        authorTextView.setText(message.getName());

        return convertView;
    }


}
