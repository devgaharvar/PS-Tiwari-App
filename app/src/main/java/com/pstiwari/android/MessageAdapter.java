package com.pstiwari.android;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.pstiwari.android.Notification.SendNotificationUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pstiwari.android.common.Utill;
import com.rygelouv.audiosensei.player.AudioSenseiListObserver;
import com.rygelouv.audiosensei.player.AudioSenseiPlayerView;
import com.rygelouv.audiosensei.player.OnPlayerViewClickListener;
import com.rygelouv.audiosensei.recorder.AudioSensei;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;

import static android.content.Context.CLIPBOARD_SERVICE;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    ArrayList<String> imgeslist1;
    private String currentUserID = "";
    private Activity mctx;
    private String audioUrl = "";
String phone;
    private DatabaseReference RootRef,messgaRe;
    private String senderMessageId, receiverMessageId;
    private onclickListener listener;

    private AudioSenseiPlayerView playerView;
    private int currentPlayingPosition;

    private MessageViewHolder holder;

    interface onclickListener {
        void onvideoClick(String url);
    }

    void navigator(onclickListener listener) {
        this.listener = listener;
    }

    public MessageAdapter(List<Messages> userMessagesList, String sendermessageid, String receivermessageid, Activity c) {
        this.userMessagesList = userMessagesList;
        this.mctx = c;
        this.senderMessageId = sendermessageid;
        this.receiverMessageId = receivermessageid;

    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessageText, receiverMessageText, senderTimeText, receiverTimeText, duration,txtSeenMeesage;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture, play_receiver_image_view, paly_sender_image_view, receiver_alpha_view, send_alpha_view,audiosender,audioreceiver;
        private AudioSenseiPlayerView receivervoicePlayerView, sendervoicePlayerView;
        private RelativeLayout image_receiver_layou, image_sender_layou, seekbarrLayout;
        private LinearLayout receiver_message_layout, sender_message_layout;
        private AudioSenseiPlayerView audio_player;
        private CardView play;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_messsage_text);
            senderTimeText = (TextView) itemView.findViewById(R.id.sender_time_text);
            receiverTimeText = (TextView) itemView.findViewById(R.id.receiver_time_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
            paly_sender_image_view = itemView.findViewById(R.id.paly_sender_image_view);
            play_receiver_image_view = itemView.findViewById(R.id.play_receiver_image_view);
            receivervoicePlayerView = itemView.findViewById(R.id.receivervoicePlayerView);
            sendervoicePlayerView = itemView.findViewById(R.id.sendervoicePlayerView);
            image_receiver_layou = itemView.findViewById(R.id.image_receiver_layou);
            audiosender=itemView.findViewById(R.id.audiosender);
            audioreceiver=itemView.findViewById(R.id.audioreceiver);
            image_sender_layou = itemView.findViewById(R.id.image_sender_layou);
            receiver_alpha_view = itemView.findViewById(R.id.receiver_alpha_view);
            send_alpha_view = itemView.findViewById(R.id.send_alpha_view);

            txtSeenMeesage = itemView.findViewById(R.id.txtSeenMeesage);


            receiver_message_layout = itemView.findViewById(R.id.receiver_message_layout);
            sender_message_layout = itemView.findViewById(R.id.sender_message_layout);
        }


    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }


    void getBitmap(final String url, final ImageView imagev) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(url, new HashMap<String, String>());
                Bitmap image = retriever.getFrameAtTime(2000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                imagev.setImageBitmap(image);
            }
        }, 5000);


    }


    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int position) {
        final String messageSenderId = mAuth.getCurrentUser().getUid();
        final Messages messages = userMessagesList.get(position);
        imgeslist1=new ArrayList<>();
        phone=mctx.getIntent().getExtras().get("visit_phone").toString();
        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();
        RootRef = FirebaseDatabase.getInstance().getReference();
        messgaRe = FirebaseDatabase.getInstance().getReference().child("Messages");
        for (int i=0;i<userMessagesList.size();i++)
        {
            if (userMessagesList.get(i).getType().equals("image"))
            {
                imgeslist1.add(userMessagesList.get(i).getMessage());
            }
        }
//        if (fromUserID != messageSenderId && recevierImage.isEmpty()) {
//
//            RootRef.keepSynced(true);
//            usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
//            usersRef.keepSynced(true);
//            usersRef.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    if (dataSnapshot.hasChild("image")) {
//                        String receiverImage = dataSnapshot.child("image").getValue().toString();
//                        recevierImage = receiverImage;
//                        Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//        }else {
        if (Utill.ContacterImage != null)
        {
            if (!Utill.ContacterImage.isEmpty()) {
                Picasso.get().load(Utill.ContacterImage).placeholder(R.drawable.dp2).into(messageViewHolder.receiverProfileImage);
            } else {
                messageViewHolder.receiverProfileImage.setImageResource(R.drawable.dp2);
            }
        }
//        }

        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverTimeText.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.senderTimeText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);
        messageViewHolder.sendervoicePlayerView.setVisibility(View.GONE);
        messageViewHolder.receivervoicePlayerView.setVisibility(View.GONE);
        messageViewHolder.image_receiver_layou.setVisibility(View.GONE);
        messageViewHolder.image_sender_layou.setVisibility(View.GONE);
        messageViewHolder.receivervoicePlayerView.setVisibility(View.GONE);
        messageViewHolder.paly_sender_image_view.setVisibility(View.GONE);
        messageViewHolder.send_alpha_view.setVisibility(View.GONE);
        messageViewHolder.receiver_alpha_view.setVisibility(View.GONE);
        messageViewHolder.play_receiver_image_view.setVisibility(View.GONE);
        messageViewHolder.sender_message_layout.setVisibility(View.GONE);
        messageViewHolder.receiver_message_layout.setVisibility(View.GONE);

        currentUserID = mAuth.getCurrentUser().getUid();
        if (position == userMessagesList.size() -1 && messages.getFrom().equals(senderMessageId)) {
            messageViewHolder.txtSeenMeesage.setVisibility(View.VISIBLE);
            System.out.println("333333333333333333333333333");
            ///////

            try {
                messgaRe.child(messages.getTo()).child("Receive").child(currentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Messages messages2 = snapshot.getValue(Messages.class);
                                if (messages2.getIsseen() == false) {
                                    messageViewHolder.txtSeenMeesage.setText("Delivered");
                                }
                                else {
                                    messageViewHolder.txtSeenMeesage.setText("Seen");
                                }
                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

            /////////
            /*
            if (messages.getIsseen()) {
                messageViewHolder.txtSeenMeesage.setText("Seen");
            } else {
                messageViewHolder.txtSeenMeesage.setText("Delivered");
            }*/
        } else {
            messageViewHolder.txtSeenMeesage.setVisibility(View.GONE);
        }


/*
        messageViewHolder.senderMessageText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showContextMenu(messageViewHolder.senderMessageText.getText().toString());
                return true;
            }
        });*/


        if (fromMessageType.equals("text")) {
            if (fromUserID.equals(messageSenderId)) {
                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.senderTimeText.setVisibility(View.VISIBLE);
                messageViewHolder.sender_message_layout.setVisibility(View.VISIBLE);
                messageViewHolder.receiver_message_layout.setVisibility(View.GONE);

                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messagees_layout);
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                messageViewHolder.senderMessageText.setText(messages.getMessage());
                messageViewHolder.senderTimeText.setText(messages.getTime() + " - " + messages.getDate() + "   ");
            } else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.receiverTimeText.setVisibility(View.VISIBLE);
//                messageViewHolder.sender_message_layout.setVisibility(View.GONE);
                messageViewHolder.sender_message_layout.setVisibility(View.GONE);
                messageViewHolder.receiver_message_layout.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                messageViewHolder.receiverMessageText.setText(messages.getMessage());
                messageViewHolder.receiverTimeText.setText(messages.getTime() + " - " + messages.getDate() + "   ");
            }
        } else if (fromMessageType.equals("image")) {
            if (fromUserID.equals(messageSenderId)) {
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                messageViewHolder.image_sender_layou.setVisibility(View.VISIBLE);
                messageViewHolder.sender_message_layout.setVisibility(View.GONE);

                if(!messages.getMessage().isEmpty()) {
                    Picasso.get().load(messages.getMessage()).placeholder(R.drawable.dp2).into(messageViewHolder.messageSenderPicture);
                } else {
                    messageViewHolder.messageSenderPicture.setImageResource(R.drawable.dp2);
                }
            } else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                messageViewHolder.image_receiver_layou.setVisibility(View.VISIBLE);
                messageViewHolder.receiver_message_layout.setVisibility(View.GONE);
                if(!messages.getMessage().isEmpty()) {
                    Picasso.get().load(messages.getMessage()).placeholder(R.drawable.dp2).into(messageViewHolder.messageReceiverPicture);
                } else {
                    messageViewHolder.messageReceiverPicture.setImageResource(R.drawable.dp2);
                }
            }
        } else if (fromMessageType.equals("video")) {

            messageViewHolder.paly_sender_image_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onvideoClick((messages.getMessage()));
                }
            });
            messageViewHolder.messageSenderPicture.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    senderVideoLongMenu(position, messageViewHolder, messages);
                    return true;
                }
            });
            if (fromUserID.equals(messageSenderId)) {
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                messageViewHolder.paly_sender_image_view.setVisibility(View.VISIBLE);
                messageViewHolder.image_sender_layou.setVisibility(View.VISIBLE);
                messageViewHolder.send_alpha_view.setVisibility(View.VISIBLE);
                messageViewHolder.sender_message_layout.setVisibility(View.GONE);
                Glide.with(mctx)
                        .load(messages.getMessage())
                        .apply(new RequestOptions())
                        .thumbnail(Glide.with(mctx).load(messages.getMessage()))
                        .into(messageViewHolder.messageSenderPicture);
//                new DownloadImage(messageViewHolder.messageSenderPicture).execute(messages.getMessage());
//                getBitmap(messages.getMessage(), messageViewHolder.messageSenderPicture);
//                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);
            } else {
                messageViewHolder.play_receiver_image_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onvideoClick((messages.getMessage()));

                    }
                });
                messageViewHolder.messageReceiverPicture.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        receiverVideoLongMenu(position,messageViewHolder,messages);
                        return true;
                    }
                });
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                messageViewHolder.play_receiver_image_view.setVisibility(View.VISIBLE);
                messageViewHolder.receiver_message_layout.setVisibility(View.GONE);
                messageViewHolder.receiver_alpha_view.setVisibility(View.VISIBLE);
//                new DownloadImage(messageViewHolder.messageReceiverPicture).execute(messages.getMessage());
                Glide.with(mctx)
                        .load(messages.getMessage())
                        .apply(new RequestOptions())
                        .thumbnail(Glide.with(mctx).load(messages.getMessage()))
                        .into(messageViewHolder.messageReceiverPicture);
                messageViewHolder.image_receiver_layou.setVisibility(View.VISIBLE);

            }
        } else if (fromMessageType.equals("audio")) {

            if (fromUserID.equals(messageSenderId)) {
                messageViewHolder.sendervoicePlayerView.setVisibility(View.VISIBLE);
                messageViewHolder.sender_message_layout.setVisibility(View.GONE);
                messageViewHolder.sendervoicePlayerView.setAudioTarget(messages.getMessage());
                messageViewHolder.sendervoicePlayerView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        sendervoiceLongMenu(position,messageViewHolder,messages);
                        return true;
                    }
                });
            } else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receivervoicePlayerView.setVisibility(View.VISIBLE);
                messageViewHolder.receivervoicePlayerView.setAudioTarget(messages.getMessage());
                messageViewHolder.receiver_message_layout.setVisibility(View.GONE);
                messageViewHolder.receivervoicePlayerView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        receivervoiceLongMenu(position,messageViewHolder,messages);
                        return true;
                    }
                });

            }
        } else if (fromMessageType.equals("pdf") || fromMessageType.equals("docx")) {
            if (fromUserID.equals(messageSenderId)) {
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                messageViewHolder.image_sender_layou.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load("https://firebasestorage.googleapis.com/v0/b/pstiwari-6c947.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=c22bdeb2-36e3-4eb4-8f42-7fa02fb36e68")
                        .into(messageViewHolder.messageSenderPicture);
            } else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                messageViewHolder.image_receiver_layou.setVisibility(View.VISIBLE);

                Picasso.get()
                        .load("https://firebasestorage.googleapis.com/v0/b/pstiwari-6c947.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=c22bdeb2-36e3-4eb4-8f42-7fa02fb36e68")
                        .into(messageViewHolder.messageReceiverPicture);
            }
        }

        messageViewHolder.senderMessageText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                senderLongMenu(position, messageViewHolder, messages);
                return true;
            }
        });


        messageViewHolder.receiverMessageText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                receiverLongMenu(position, messageViewHolder, messages);
                return true;
            }
        });

        if (fromUserID.equals(messageSenderId)) {
            messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    senderLongMenu(position, messageViewHolder, messages);
                    return true;
                }


            });
        } else {


            messageViewHolder.paly_sender_image_view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    receiverLongMenu(position, messageViewHolder, messages);
                    return true;
                }
            });
            messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    receiverLongMenu(position, messageViewHolder, messages);
                    return true;
                }
            });
        }
        //priview image code start...............................................
        messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                senderPreviewimg(position,messageViewHolder,messages);
            }
        });
        messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reciverPreviewimg(position,messageViewHolder,messages);
            }
        });

        //end.............................................................
    }

    private void reciverPreviewimg(final int position,final MessageViewHolder messageViewHolder,final Messages messages) {
        if (userMessagesList.get(position).getType().equals("image")){
            Intent intent = new Intent(messageViewHolder.itemView.getContext(), imageSingleView.class);
            intent.putExtra("url", userMessagesList.get(position).getMessage());
            messageViewHolder.itemView.getContext().startActivity(intent);
        }else {
            // Toast.makeText(mctx, "Error Occurred...", Toast.LENGTH_SHORT).show();
        }
    }
    private void reciverLongPreviewimg(final int position,final MessageViewHolder messageViewHolder,final Messages messages) {
        if (userMessagesList.get(position).getType().equals("image")){
            Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
            intent.putExtra("mylist", imgeslist1);
            intent.putExtra("url", userMessagesList.get(position).getMessage());
            messageViewHolder.itemView.getContext().startActivity(intent);
        }else {
            //Toast.makeText(mctx, "Error Occurred...", Toast.LENGTH_SHORT).show();
        }
    }
    private void senderLongPreviewimg(final int position,final MessageViewHolder messageViewHolder,final Messages messages) {
        if (userMessagesList.get(position).getType().equals("image")){
            Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
            intent.putExtra("mylist", imgeslist1);
            intent.putExtra("url", userMessagesList.get(position).getMessage());
            messageViewHolder.itemView.getContext().startActivity(intent);
        }else {
            //Toast.makeText(mctx, "Error Occurred...", Toast.LENGTH_SHORT).show();
        }
    }

    private void senderPreviewimg(final int position,final MessageViewHolder messageViewHolder,final Messages messages) {
        if (userMessagesList.get(position).getType().equals("image")){
            Intent intent = new Intent(messageViewHolder.itemView.getContext(), imageSingleView.class);
            intent.putExtra("url", userMessagesList.get(position).getMessage());
            messageViewHolder.itemView.getContext().startActivity(intent);

        }else {
            //Toast.makeText(mctx, "Error Occurred...", Toast.LENGTH_SHORT).show();
        }
    }

    private void receiverLongMenu(final int position, @NonNull final MessageViewHolder messageViewHolder, final Messages messages) {
        if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")) {
            CharSequence option[] = new CharSequence[]
                    {
                            "Delete For Me",
                            "Download document",
                            "Cancel",
                            "Forward",
                            "Clear Chat"
                    };
            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
            builder.setTitle("Delete Message?");

            builder.setItems(option, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == 0) {
                        deleteReceivedMessage(position, messageViewHolder);
//                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
//                                    messageViewHolder.itemView.getContext().startActivity(intent);
                    } else if (i == 1) {
//                                    openUrl(userMessagesList.get(position).getMessage());
                        /*Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        mctx.startActivity(intent);*/
                        String url = userMessagesList.get(position).getMessage();
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                        request.setTitle("Download");
                        request.setDescription("Downloading File....");
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,""+System.currentTimeMillis() +".pdf");
                        DownloadManager manager = (DownloadManager) mctx.getSystemService(Context.DOWNLOAD_SERVICE);
                        manager.enqueue(request);
                        Toast.makeText(mctx, "Please check notification for view the file", Toast.LENGTH_SHORT).show();
                    } else if (i == 2) {
                        dialogInterface.dismiss();
                    } else if (i == 3) {
                        showContactList(messages);
                        //Toast.makeText(mctx,"Coming soon", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    } else if (i == 4) {
                        clearAllChat(senderMessageId, receiverMessageId);
                        dialogInterface.dismiss();
                    }
                }
            });
            builder.show();
        } else if (userMessagesList.get(position).getType().equals("text")) {
            CharSequence option[] = new CharSequence[]
                    {
                            "Delete For Me",
                            "Cancel",
                            "Copy",
                            "Forward",
                            "Clear Chat"
                    };
            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
            builder.setTitle("Delete Message?");

            builder.setItems(option, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == 0) {
                        deleteReceivedMessage(position, messageViewHolder);
//                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
//                                    messageViewHolder.itemView.getContext().startActivity(intent);
                    } else if (i == 1) {
                        dialogInterface.dismiss();
                    } else if (i == 2) {
                        ClipboardManager clipboard = (ClipboardManager) mctx.getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("text", messageViewHolder.
                                receiverMessageText.getText().toString());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(mctx, "Text Copied", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    } else if (i == 3) {
                        showContactList(messages);
                        //Toast.makeText(mctx,"Coming soon", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    } else if (i == 4) {
                        clearAllChat(senderMessageId, receiverMessageId);
                        dialogInterface.dismiss();
                    }
                }
            });
            builder.show();
        } else if (userMessagesList.get(position).getType().equals("image")) {
            CharSequence option[] = new CharSequence[]
                    {
                            "Delete For Me",
                            "View All Images",
                            "Cancel",
                            "Forward",
                            "Clear Chat"
                    };
            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
            builder.setTitle("Delete Message?");

            builder.setItems(option, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == 0) {
                        deleteReceivedMessage(position, messageViewHolder);
//                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
//                                    messageViewHolder.itemView.getContext().startActivity(intent);
                    } else if (i == 1) {
                       /* Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                        intent.putExtra("url", userMessagesList.get(position).getMessage());
                        messageViewHolder.itemView.getContext().startActivity(intent);*/
                        reciverLongPreviewimg(position,messageViewHolder,messages);
                    } else if (i == 2) {
                        dialogInterface.dismiss();
                    } else if (i == 3) {
                        showContactList(messages);
                        //Toast.makeText(mctx,"Coming soon", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    } else if (i == 4) {
                        clearAllChat(senderMessageId, receiverMessageId);
                        dialogInterface.dismiss();
                    }
                }
            });
            builder.show();
        }
    }
    private void receivervoiceLongMenu(final int position, @NonNull final MessageViewHolder messageViewHolder, final Messages messages) {
        if (userMessagesList.get(position).getType().equals("audio")) {
            CharSequence option[] = new CharSequence[]
                    {
                            "Delete For Me",
                            "Cancel",
                            "Forward",
                            "Clear Chat"
                    };
            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
            builder.setTitle("Delete Message?");

            builder.setItems(option, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == 0) {
                        deleteReceivedMessage(position, messageViewHolder);
//                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
//                                    messageViewHolder.itemView.getContext().startActivity(intent);
                    } else if (i == 1) {
                        dialogInterface.dismiss();
                    } else if (i == 2) {
                        showContactList(messages);
                        //Toast.makeText(mctx,"Coming soon", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    } else if (i == 3) {
                        clearAllChat(senderMessageId, receiverMessageId);
                        dialogInterface.dismiss();
                    }
                }
            });
            builder.show();
        }
    }

    private void receiverVideoLongMenu(final int position, @NonNull final MessageViewHolder messageViewHolder, final Messages messages) {
        if (userMessagesList.get(position).getType().equals("video")) {
            CharSequence option[] = new CharSequence[]
                    {
                            "Delete For Me",
                            "Cancel",
                            "Forward",
                            "Clear Chat"
                    };
            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
            builder.setTitle("Delete Message?");

            builder.setItems(option, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == 0) {
                        deleteReceivedMessage(position, messageViewHolder);
//                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
//                                    messageViewHolder.itemView.getContext().startActivity(intent);
                    }/* else if (i == 1) {
//                                    openUrl(userMessagesList.get(position).getMessage());
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        mctx.startActivity(intent);
                    }*/ else if (i == 1) {
                        dialogInterface.dismiss();
                    } else if (i == 2) {
                        showContactList(messages);
                        //Toast.makeText(mctx,"Coming soon", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    } else if (i == 3) {
                        clearAllChat(senderMessageId, receiverMessageId);
                        dialogInterface.dismiss();
                    }
                }
            });
            builder.show();
        }
    }

    private void senderLongMenu(final int position, final MessageViewHolder messageViewHolder, final Messages messages) {
        if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")) {
            CharSequence option[] = new CharSequence[]
                    {
                            "Delete For Me",
                            "Download document",
                            "Cancel",
                            "Delete for everyone",
                            "Forward",
                            "Clear Chat"
                    };
            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
            builder.setTitle("Delete Message?");

            builder.setItems(option, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == 0) {
                        deleteSentMessage(position, messageViewHolder);
//                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                    messageViewHolder.itemView.getContext().startActivity(intent);
                    } else if (i == 1) {
//                                    openUrl(userMessagesList.get(position).getMessage());
                        /*Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        mctx.startActivity(intent);*/
                        String url = userMessagesList.get(position).getMessage();
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                        request.setTitle("Download");
                        request.setDescription("Downloading File....");
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,""+System.currentTimeMillis() +".pdf");
                        DownloadManager manager = (DownloadManager) mctx.getSystemService(Context.DOWNLOAD_SERVICE);
                        manager.enqueue(request);
                        Toast.makeText(mctx, "Please check notification for view the file", Toast.LENGTH_SHORT).show();

                    } else if (i == 2) {
                        dialogInterface.dismiss();
                    } else if (i == 3) {
                        deleteMessagesForEveryone(position, messageViewHolder);
//                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                    messageViewHolder.itemView.getContext().startActivity(intent);
                    } else if (i == 4) {
                        showContactList(messages);
                        //Toast.makeText(mctx,"Coming soon", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    } else if (i == 5) {
                        clearAllChat(senderMessageId, receiverMessageId);
                        dialogInterface.dismiss();
                    }
                }
            });
            builder.show();
        } else if (userMessagesList.get(position).getType().equals("text")) {
            CharSequence option[] = new CharSequence[]
                    {
                            "Delete For Me",
                            "Cancel",
                            "Delete for everyone",
                            "Copy",
                            "Forward",
                            "Clear Chat"

                    };
            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
            builder.setTitle("Delete Message?");

            builder.setItems(option, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == 0) {
                        deleteSentMessage(position, messageViewHolder);
//                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                    messageViewHolder.itemView.getContext().startActivity(intent);
                    } else if (i == 1) {
                        dialogInterface.dismiss();
                    } else if (i == 2) {
                        deleteMessagesForEveryone(position, messageViewHolder);
//                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                    messageViewHolder.itemView.getContext().startActivity(intent);
                    } else if (i == 3) {
                        ClipboardManager clipboard = (ClipboardManager) mctx.getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("text", messageViewHolder.
                                senderMessageText.getText().toString());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(mctx, "Text Copied", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    } else if (i == 4) {
                        showContactList(messages);
                        //Toast.makeText(mctx,"Coming soon", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    } else if (i == 5) {
                        clearAllChat(senderMessageId, receiverMessageId);
                        dialogInterface.dismiss();
                    }
                }
            });
            builder.show();
        } else if (userMessagesList.get(position).getType().equals("image")) {
            CharSequence option[] = new CharSequence[]
                    {
                            "Delete For Me",
                            "View All Images",
                            "Cancel",
                            "Delete for everyone",
                            "Forward",
                            "Clear Chat"
                    };
            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
            builder.setTitle("Delete Message?");

            builder.setItems(option, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == 0) {
                        deleteSentMessage(position, messageViewHolder);
//                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                    messageViewHolder.itemView.getContext().startActivity(intent);
                    } else if (i == 1) {
                        /*Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                        intent.putExtra("url", userMessagesList.get(position).getMessage());
                        messageViewHolder.itemView.getContext().startActivity(intent);*/
                        senderLongPreviewimg(position,messageViewHolder,messages);
                    } else if (i == 2) {
                        dialogInterface.dismiss();
                    } else if (i == 3) {
                        deleteMessagesForEveryone(position, messageViewHolder);
//                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                    messageViewHolder.itemView.getContext().startActivity(intent);
                    } else if (i == 4) {
                        showContactList(messages);
                        //Toast.makeText(mctx,"Coming soon", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    } else if (i == 5) {
                        clearAllChat(senderMessageId, receiverMessageId);
                        dialogInterface.dismiss();
                    }
                }
            });
            builder.show();
        }
        //video long click lister .....................................................
        else if (userMessagesList.get(position).getType().equals("video")){
            CharSequence option[] = new CharSequence[]
                    {
                            "Delete For Me",
                            "View Image",
                            "Cancel",
                            "Delete for everyone",
                            "Forward",
                            "Clear Chat"
                    };
            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
            builder.setTitle("Delete Message?");
            builder.show();
        }
    }
    private void sendervoiceLongMenu(final int position, final MessageViewHolder messageViewHolder, final Messages messages) {
        if (userMessagesList.get(position).getType().equals("audio")) {
            CharSequence option[] = new CharSequence[]
                    {
                            "Delete For Me",
                            "Cancel",
                            "Delete for everyone",
                            "Forward",
                            "Clear Chat"
                    };
            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
            builder.setTitle("Delete Message?");

            builder.setItems(option, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == 0) {
                        deleteSentMessage(position, messageViewHolder);
//                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                    messageViewHolder.itemView.getContext().startActivity(intent);
                    } else if (i == 1) {
                        dialogInterface.dismiss();
                    } else if (i == 2) {
                        deleteMessagesForEveryone(position, messageViewHolder);
//                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                    messageViewHolder.itemView.getContext().startActivity(intent);
                    } else if (i == 3) {
                        showContactList(messages);
                        //Toast.makeText(mctx,"Coming soon", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    } else if (i == 5) {
                        clearAllChat(senderMessageId, receiverMessageId);
                        dialogInterface.dismiss();
                    }
                }
            });
            builder.show();
        }
        //video long click lister .....................................................
        else if (userMessagesList.get(position).getType().equals("video")){
            CharSequence option[] = new CharSequence[]
                    {
                            "Delete For Me",
                            "View Image",
                            "Cancel",
                            "Delete for everyone",
                            "Forward",
                            "Clear Chat"
                    };
            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
            builder.setTitle("Delete Message?");
            builder.show();
        }
    }

    private void senderVideoLongMenu(final int position, final MessageViewHolder messageViewHolder, final Messages messages) {
        if (userMessagesList.get(position).getType().equals("video")) {
            CharSequence option[] = new CharSequence[]
                    {
                            "Delete For Me",
                            "Cancel",
                            "Delete for everyone",
                            "Forward",
                            "Clear Chat"
                    };
            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
            builder.setTitle("Delete Message?");

            builder.setItems(option, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == 0) {
                        deleteSentMessage(position, messageViewHolder);
//                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                    messageViewHolder.itemView.getContext().startActivity(intent);
                    } /*else if (i == 1) {
//                                    openUrl(userMessagesList.get(position).getMessage());
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        mctx.startActivity(intent);

                    }*/ else if (i == 1) {
                        dialogInterface.dismiss();
                    } else if (i == 2) {
                        deleteMessagesForEveryone(position, messageViewHolder);
//                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                    messageViewHolder.itemView.getContext().startActivity(intent);
                    } else if (i == 3) {
                        showContactList(messages);
                        //Toast.makeText(mctx,"Coming soon", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    } else if (i == 4) {
                        clearAllChat(senderMessageId, receiverMessageId);
                        dialogInterface.dismiss();
                    }
                }
            });
            builder.show();
        }

    }
    /*
    private void showContextMenu(final String txt){
        final CharSequence[] items = { "Copy", "Forward", "Clear Chat"};

        AlertDialog.Builder builder = new AlertDialog.Builder(mctx);

        builder.setTitle("Select Action:");
        builder.setItems(items, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int pos) {
                if(pos == 0){
                    ClipboardManager clipboard = (ClipboardManager) mctx.getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", txt);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(mctx,"Text Copied", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
                else if(pos == 1){
                    Toast.makeText(mctx,"Coming soon", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
                else if(pos == 2){
                    RootRef.child("Messages").child(senderMessageId).child(receiverMessageId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(mctx,"Success", Toast.LENGTH_SHORT).show();
                            ((ChatActivity) mctx).refreshChatList();
                        }
                    });
                    //Toast.makeText(mctx,"Coming soon", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }

        });
        builder.show();
    }*/

    private void clearAllChat(final String senderMsgId, final String receiverMsgId) {
//        deleteAllMessage();
        RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.child("Messages").child(senderMsgId).child(receiverMsgId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
//                Toast.makeText(mctx, "Success", Toast.LENGTH_SHORT).show();
//                ((ChatActivity) mctx).refreshChatList();
                RootRef.child("Contacts").child(senderMsgId).child(receiverMsgId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //done
                        Intent intent = new Intent(mctx, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mctx.startActivity(intent);
                    }
                });
            }
        });


    }

    private void showContactList(final Messages messages) {

        String messageReceiverImage = mctx.getIntent().getExtras().get("visit_image").toString();
        String nameu=mctx.getIntent().getExtras().get("visit_user_name").toString();
             Intent intent=new Intent(mctx,ForwardContactsList.class);
             intent.putExtra("messagelist", messages);
             intent.putExtra("username",((ChatActivity) mctx).myUserName);
             intent.putExtra("visit_image",messageReceiverImage);
             intent.putExtra("visit_user_name",nameu);
             mctx.startActivity(intent);

    /*    final List<String> mContacts = new ArrayList<>();
        final List<String> mIdList = new ArrayList<>();
        final DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mContacts.clear();
                mIdList.clear();


                for (DataSnapshot sp : snapshot.getChildren()) {
                    Contacts ct = sp.getValue(Contacts.class);

                    assert ct != null;
                    assert firebaseUser != null;

                    if (ct != null && firebaseUser != null && ct.getUid() != null && firebaseUser.getUid() != null) {
                        if (!ct.getUid().equals(firebaseUser.getUid())) {
                            if (ContactsFragment.contactExists((ChatActivity) mctx, ct.getPhone())) {
                                mContacts.add(ct.getName());
                                mIdList.add(ct.getUid());
                            }
                            //mContacts.add(ct.getName());
                            //mIdList.add(ct.getUid());
                        }
                    }

                }
                if (mContacts.size() == 0) {
                    Toast.makeText(mctx, "No contact found", Toast.LENGTH_SHORT).show();
                    UsersRef.removeEventListener(this);
                    return;
                }

                ArrayAdapter<String> adapter;
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(mctx);
                View rowList = LayoutInflater.from(mctx).inflate(R.layout.dialog_select_contact, null);
                ListView listView = rowList.findViewById(R.id.listView);
                adapter = new ArrayAdapter<String>(mctx, android.R.layout.simple_list_item_1, mContacts);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                alertDialog.setView(rowList);
                final AlertDialog dialog = alertDialog.create();

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        forwardMessage(messages, mIdList.get(i), firebaseUser.getUid());
                        dialog.dismiss();
                    }
                });
                dialog.show();
                UsersRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
    }


    private void forwardMessage(final Messages messages, final String messageReceiverID, final String messageSenderId) {
        DatabaseReference ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        ContactsRef.keepSynced(true);
        ContactsRef.child(messageSenderId).child(messageReceiverID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //contact added
                        }
                    }
                });

        DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                .child(messageSenderId).child(messageReceiverID).push();

        final String messagePushID = userMessageKeyRef.getKey();

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        String saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        String saveCurrentTime = currentTime.format(calendar.getTime());

        Map messageFileBody = new HashMap();
        messageFileBody.put("message", messages.getMessage());
        messageFileBody.put("name", messages.getName());
        messageFileBody.put("type", messages.getType());
        messageFileBody.put("from", messageSenderId);
        messageFileBody.put("to", messageReceiverID);
        messageFileBody.put("messageID", messagePushID);
        messageFileBody.put("time", saveCurrentTime);
        messageFileBody.put("date", saveCurrentDate);
        messageFileBody.put("isseen", false);

        final String messageSenderRef = "Messages/" + messageSenderId + "/" + messageReceiverID;
        final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderId;


        Map messageBodyDetail = new HashMap();
        messageBodyDetail.put(messageSenderRef + "/" + messagePushID, messageFileBody);
        messageBodyDetail.put(messageReceiverRef + "/" + messagePushID, messageFileBody);

        setUserLastMessage(messageFileBody,messagePushID, messageReceiverID, messageSenderId);

        RootRef.updateChildren(messageBodyDetail).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Toast.makeText(mctx, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                    String messageReceiverImage = mctx.getIntent().getExtras().get("visit_image").toString();
                    String nameu=mctx.getIntent().getExtras().get("visit_user_name").toString();
                    new SendNotificationUtil().sendNotifiaction(mctx, messageReceiverID,
                            ((ChatActivity) mctx).myUserName, messages.getType().equals("text") ? messages.getMessage() : "File", messageSenderId,phone,messageReceiverImage,nameu);
                } else {
                    Toast.makeText(mctx, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setUserLastMessage(Map lastMessage, String messagePushID, final String forwardReceiverId, final String forwardSenderId ) {
        RootRef.child("Users").child(forwardSenderId).child("lastMessage").child(forwardReceiverId).setValue(lastMessage);
        RootRef.child("Users").child(forwardReceiverId).child("lastMessage").child(forwardSenderId).setValue(lastMessage);
        RootRef.child("Contacts").child(forwardSenderId).child(forwardReceiverId).child("key").setValue(messagePushID);
        RootRef.child("Contacts").child(forwardReceiverId).child(forwardSenderId).child("key").setValue(messagePushID);
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }


    private void deleteSentMessage(final int position, final MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        deleteForMeMessage(position);
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(mctx, "Message Deleted Successfully...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mctx, "Error Occurred...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void deleteReceivedMessage(final int position, final MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        deleteForMeMessage(position);
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(mctx, "Message Deleted Successfully...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mctx, "Error Occurred...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void deleteMessagesForEveryone(final int position, final MessageViewHolder holder) {
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        deleteEveryOneMessage(position);
        final String senderID = userMessagesList.get(position).getFrom();
        final String recieverID = userMessagesList.get(position).getTo();
        final String messageID = userMessagesList.get(position).getMessageID();

        rootRef.child("Messages")
                .child(senderID)
                .child(recieverID)
                .child(messageID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    rootRef.child("Messages")
                            .child(recieverID)
                            .child(senderID)
                            .child(messageID)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(mctx, "Message Deleted Successfully...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(mctx, "Error Occurred...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void playvoice(final String message, final VoicePlayerView playerView) {

        playerView.setImgPlayClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerView.setAudio(message);
            }
        });


    }


    public class DownloadImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImage(ImageView bmImage) {
            this.bmImage = (ImageView) bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            Bitmap myBitmap = null;
            MediaMetadataRetriever mMRetriever = null;
            try {
                mMRetriever = new MediaMetadataRetriever();
                if (Build.VERSION.SDK_INT >= 14)
                    mMRetriever.setDataSource(urls[0], new HashMap<String, String>());
                else
                    mMRetriever.setDataSource(urls[0]);
                myBitmap = mMRetriever.getFrameAtTime();
            } catch (Exception e) {
                e.printStackTrace();


            } finally {
                if (mMRetriever != null) {
                    mMRetriever.release();
                }
            }
            return myBitmap;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }


    }

    private void openUrl(String url) {
//        CustomTabsIntent.Builder builder  = new CustomTabsIntent.Builder();
//        builder.setToolbarColor(ContextCompat.getColor(mctx, R.color.colorPrimary));
//         CustomTabsIntent customTabsIntent = builder.build();
//        customTabsIntent.launchUrl(mctx, Uri.parse(url));
    }

    private void deleteForMeMessage(int position) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        if (position == userMessagesList.size() - 1) {
            if (position > 0) {
                Messages index = userMessagesList.get(position - 1);
                rootRef.child("Users").child(receiverMessageId).child("lastMessage").setValue(index);
                rootRef.child("Contacts").child(senderMessageId).child(receiverMessageId).child("key").setValue(index.getMessageID());
            } else {
                rootRef.child("Users").child(receiverMessageId).child("lastMessage").setValue(null);
                rootRef.child("Contacts").child(senderMessageId).child(receiverMessageId).child("key").setValue(null);
            }
        }

    }

    private void deleteEveryOneMessage(int position) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        if (position == userMessagesList.size() - 1) {
            if (position > 0) {
                Messages index = userMessagesList.get(position - 1);
                rootRef.child("Users").child(receiverMessageId).child("lastMessage").setValue(index);
                rootRef.child("Users").child(senderMessageId).child("lastMessage").setValue(index);
                rootRef.child("Contacts").child(senderMessageId).child(receiverMessageId).child("key").setValue(index.getMessageID());
                rootRef.child("Contacts").child(receiverMessageId).child(senderMessageId).child("key").setValue(index.getMessageID());
            } else {
                rootRef.child("Users").child(receiverMessageId).child("lastMessage").setValue(null);
                rootRef.child("Users").child(senderMessageId).child("lastMessage").setValue(null);
                rootRef.child("Contacts").child(senderMessageId).child(receiverMessageId).child("key").setValue(null);
                rootRef.child("Contacts").child(receiverMessageId).child(senderMessageId).child("key").setValue(null);
            }
        }

    }

    private void deleteAllMessage() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Users").child(receiverMessageId).child("lastMessage").setValue(null);
        rootRef.child("Users").child(senderMessageId).child("lastMessage").setValue(null);
        rootRef.child("Contacts").child(senderMessageId).child(receiverMessageId).child("key").setValue(null);
        rootRef.child("Contacts").child(receiverMessageId).child(senderMessageId).child("key").setValue(null);
    }
}
