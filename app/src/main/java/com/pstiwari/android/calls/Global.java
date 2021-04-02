package com.pstiwari.android.calls;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.PowerManager;

import com.pstiwari.android.ApiService;
import com.pstiwari.android.Notification.FCMclient;


import java.util.ArrayList;
import java.util.List;


public class Global {
//Firebase SERVER KEY For  NOTIFICATIONS
    public static final String Server_Key = "AAAA5eoEY_Q:APA91bHmLZvmMnZfuvQbi61erb2zOoI1XsYAT9sJlvW9NSB6vKoz06Z-KL3edksShSeHSYvsu-Oh56pf_uCxpH6TZqLkGNuLUQ9dce5YEehQxFpY4Vzg-3xI_kiNjXbDV4KPHqu3uD85";

    public static final String fcmurl = "https://fcm.googleapis.com/";

    public static ApiService getFCMservies() {
        return FCMclient.getClient(fcmurl).create(ApiService.class);
    }
    //admob ads
    public final static boolean ADMOB_ENABLE = true;
    public final static String USERS_Profile = "Cleint";
    public final static String Lawyer_Profile = "Lawyer";
    public final static String All_Users = "All_Users";
    public final static String All_Clients = "All_Clients";
    public final static String All_Lawyer = "All_Lawyers";
    public final static String Profile = "Profile";
    public final static String LawyerTime = "LawyerTime";
    //Database Constants
    public final static String USERS = "Users";
    public final static String CHATS = "Chats";
    public final static String TIME = "Time";
    public final static String MUTE = "Mute";
    public final static String BLOCK = "Block";
    public final static String FAV = "Favourite";
    public final static String GROUPS = "Groups";
    public final static String Phones = "Phones";
    public final static String Online = "online";
    public final static String avatar = "avatar";
    public final static String time = "time";
    public final static String Messages = "messages";
    public final static String tokens = "Tokens";
    public final static String CALLS = "Calls";
    public final static String First_CALL = "First_call";
    public final static String device = "device";
    public final static String Status_Onine="available";
    public final static String Status_Offline="notavailable";

    //Storage Constants
    public final static String AvatarS = "Avatar";
    public final static String StoryS = "Stories";
    public final static String myStoryS = "MyStories";
    public final static String Mess = "Message";
    public final static String GroupAva = "GroupsAva";


    //App constatnts
    public final static int STATUE_LENTH = 20;
    public final static int STORY_NAME_LENTH = 10;
    public final static int FileName_LENTH = 30;
    public final static int NOTIFYTIME = 3000;
    public final static int SHAKE_UNDO_TIMEOUT = 30; //time in sec
    public static String DEFAULT_STATUE = "Hello World!!";
    public static boolean DARKSTATE = false;
    public static boolean netconnect = false;
    public static boolean local_on = true;
    public static boolean yourM = true;

    public static ArrayList<String> btnid;


    //local vars (my data)
    public static String nameLocal = "";
    public static String statueLocal = "";
    public static String avaLocal = "";
    public static String idLocal = "";
    public static String phoneLocal = "";
    public static boolean blockedLocal = false;
    public static boolean stickerIcon = true;
    public static boolean myonstate;
    public static boolean myscreen;

    public static ArrayList<calls> callList;
    public static ArrayList<String> blockList;
    public static ArrayList<UserData> tempUser;
    public static ArrayList<String> mutelist;
    public static String currentpageid = "";


    public static Activity currentactivity;
    public static Activity currentfragment;
    public static Activity chatactivity;
    public static Activity mainActivity;

    public static Activity IncAActivity = null;
    public static Activity IncVActivity = null;
    public static ArrayList<String> groupids;
    public static ArrayList<String> forwardids;


    public static ArrayList<String> inviteNums;
    public static PowerManager.WakeLock wl;
    public static PowerManager pm;



    //app media max number chooser
    public static int photoS = 5; //photos max number to select in one time
    public static int audioS = 1; //audio max number to select in one time
    public static int videoS = 1; //video max number to select in one time
    public static int fileS = 1; //files max number to select in one time

    //storage
    public static Context conA;
    public static Context conMain;
    //friend (friend data)
    public static String currFid = "";
    public static String currAva = "";
    public static String currname = "";
    public static String currstatue = "";
    public static String currphone = "";
    public static ArrayList<String> currGUsers;
    public static ArrayList<UserData> currGUsersU;
    public static ArrayList<UserData> adminList;
    public static ArrayList<String> currGUsersAva;
    public static ArrayList<String> currGAdmins;
    public static ArrayList<String> currblockList;
    public static boolean currblocked = false;


    public static boolean onstate;
    public static boolean currscreen;
    public static long currtime = 0;



    //encryption
    public static String salt = "codeslu8882888plaxsalt";
    public static String keyE = "€codeslu€8882888€plax€key€";

    //story
    public static boolean storyFramel = true;

    //Device related constants
    public static final String Device_Type_Android = "Android";
    //check internet
    public static Boolean check_int(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            return true;
        } else
            return false;
    }

    //check if user in activity or not
    public static boolean isRunning(Context ctx) {
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > 20) {
            List<ActivityManager.RunningAppProcessInfo> tasks = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo task : tasks) {
                if (ctx.getPackageName().equalsIgnoreCase(task.processName))
                    return true;
            }
        } else {
            List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
            for (ActivityManager.RunningTaskInfo task : tasks) {
                if (ctx.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName()))
                    return true;
            }
        }

        return false;
    }


}
