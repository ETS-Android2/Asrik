/*
    Fragment under Chat tab
    Shows the list of users with whom the currently logged in user
    has chat history with
    Also provides access to ChatBot fragment via a Floating Action Button
 */

package com.mitrukahitesh.asrik.fragments.chatfragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.adapters.ChatListAdapter;
import com.mitrukahitesh.asrik.models.chat.ChatInfo;
import com.mitrukahitesh.asrik.helpers.Constants;
import com.mitrukahitesh.asrik.models.chat.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatList extends Fragment {

    private final List<ChatInfo> chatInfoList = new ArrayList<>();
    public static final List<Message> botChat = new ArrayList<>();
    private RecyclerView recyclerView;
    private ChatListAdapter adapter;
    private TextView noChats;
    private FloatingActionButton fab;

    public ChatList() {
    }

    /**
     * Called to do initial creation of a fragment.
     * This is called after onAttach and before onCreateView
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This will be called between onCreate and onViewCreated
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_list, container, false);
    }

    /**
     * Called immediately after onCreateView has returned,
     * but before any saved state has been restored in to the view.
     * Set references to views
     * Set listeners to views
     * Set initial values of views
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (recyclerView != null) {
            adapter = (ChatListAdapter) recyclerView.getAdapter();
        } else {
            adapter = new ChatListAdapter(requireContext(), chatInfoList, Navigation.findNavController(view));
            fetchChats();
        }
        fab = view.findViewById(R.id.fab);
        noChats = view.findViewById(R.id.no_chats);
        recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);
        if (chatInfoList.size() == 0) {
            noChats.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noChats.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_chatList_to_chatBot);
            }
        });
    }

    /**
     * Fetches all the chats user is involved in and
     * adds to a list which is displayed in a recycler view
     */
    private void fetchChats() {
        if (FirebaseAuth.getInstance().getUid() == null)
            return;
        /*
            Get ChatIDs of user's chat and fetch required chat data
         */
        FirebaseFirestore.getInstance()
                .collection(Constants.USERS)
                .document(FirebaseAuth.getInstance().getUid())
                .collection(Constants.CHATS)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value == null)
                            return;
                        for (DocumentChange snapshot : value.getDocumentChanges()) {
                            if (snapshot.getType().equals(DocumentChange.Type.ADDED)) {
                                ChatInfo info = new ChatInfo();
                                info.setChatId(snapshot.getDocument().getString(Constants.CHAT_ID));
                                info.setUid(snapshot.getDocument().getId());
                                getUserInfo(info);
                            }
                        }
                    }

                    /*
                        Get user information like name, profile pic url, number
                     */
                    private void getUserInfo(ChatInfo info) {
                        FirebaseFirestore.getInstance()
                                .collection(Constants.USERS)
                                .document(info.getUid())
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        info.setName(documentSnapshot.getString(Constants.NAME));
                                        info.setProfileUrl(documentSnapshot.getString(Constants.PROFILE_PIC_URL));
                                        info.setNumber(documentSnapshot.getString(Constants.NUMBER));
                                        getLastMessage(info);
                                    }
                                });
                    }

                    /*
                        Get last message of the conversation
                        to display in chat list
                     */
                    private void getLastMessage(ChatInfo info) {
                        FirebaseFirestore.getInstance()
                                .collection(Constants.CHATS)
                                .document(info.getChatId())
                                .collection(Constants.MESSAGES)
                                .orderBy(Constants.TIME.toLowerCase(Locale.ROOT), Query.Direction.DESCENDING)
                                .limit(1)
                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                        if (value == null)
                                            return;
                                        if (value.size() == 0)
                                            return;
                                        String lastMessage = "";
                                        Long time = 0L;
                                        for (DocumentChange snapshot : value.getDocumentChanges()) {
                                            if (snapshot.getType().equals(DocumentChange.Type.MODIFIED))
                                                return;
                                            if (snapshot.getType().equals(DocumentChange.Type.ADDED)) {
                                                if (snapshot.getDocument().getBoolean(Constants.MEDIA.toLowerCase(Locale.ROOT))) {
                                                    info.setMedia(true);
                                                    lastMessage = "".equals(snapshot.getDocument().getString(Constants.MESSAGE.toLowerCase(Locale.ROOT))) ?
                                                            snapshot.getDocument().getString("fileName") :
                                                            snapshot.getDocument().getString(Constants.MESSAGE.toLowerCase(Locale.ROOT));
                                                } else {
                                                    info.setMedia(false);
                                                    lastMessage = snapshot.getDocument().getString(Constants.MESSAGE.toLowerCase(Locale.ROOT));
                                                }
                                                time = snapshot.getDocument().getLong(Constants.TIME.toLowerCase(Locale.ROOT));
                                                break;
                                            }
                                        }
                                        for (int i = 0; i < chatInfoList.size(); ++i) {
                                            if (info.getChatId().equals(chatInfoList.get(i).getChatId())) {
                                                chatInfoList.get(i).setLastMessage(lastMessage);
                                                chatInfoList.get(i).setTime(time);
                                                chatInfoList.get(i).setMedia(info.getMedia());
                                                sortAndNotify();
                                                return;
                                            }
                                        }
                                        info.setLastMessage(lastMessage);
                                        info.setTime(time);
                                        chatInfoList.add(info);
                                        sortAndNotify();
                                        noChats.setVisibility(View.GONE);
                                        recyclerView.setVisibility(View.VISIBLE);
                                    }

                                    @SuppressLint("NotifyDataSetChanged")
                                    private void sortAndNotify() {
                                        chatInfoList.sort((o1, o2) -> (int) -(o1.getTime() - o2.getTime()));
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    }
                });
    }
}