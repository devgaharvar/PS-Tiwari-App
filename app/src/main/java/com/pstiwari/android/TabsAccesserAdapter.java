package com.pstiwari.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabsAccesserAdapter extends FragmentPagerAdapter {

    FragmentManager fragManager;
    ChatsFragment chatsFragment;
    ContactsFragment contactsFragment;
    CallsFragment callsFragment;
    public TabsAccesserAdapter( FragmentManager fm) {
        super(fm);
        this.fragManager = fm;
    }

    @NonNull
    @Override
    public Fragment getItem(int i) {

        switch (i)
        {
            case 0:
                 chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 1: {
                System.out.println("qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq");
                 contactsFragment = new ContactsFragment();
                return contactsFragment;
            }

            case 2:
                 callsFragment = new CallsFragment();
                return callsFragment;

                default:
                    return null;
        }
    }


    @Override
    public int getCount() {
        return 3;
    }


    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position)
        {
            case 0:
                return "Chats";

            case 1:
                return "Contacts";

            case 2:
                return "Calls";

            default:
                return null;
        }
    }
}
