package com.mitrukahitesh.asrik.fragments.chatfragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mitrukahitesh.asrik.App;
import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.adapters.ChatListAdapter;
import com.mitrukahitesh.asrik.models.ChatInfo;
import com.mitrukahitesh.asrik.models.Message;
import com.mitrukahitesh.asrik.utility.Constants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ChatList extends Fragment {

    private final List<ChatInfo> chatInfoList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ChatListAdapter adapter;
    private TextView noChats;

    public ChatList() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (recyclerView != null) {
            adapter = (ChatListAdapter) recyclerView.getAdapter();
        } else {
            adapter = new ChatListAdapter(requireContext(), chatInfoList, Navigation.findNavController(view));
            fetchChats();
        }
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
    }

    private void fetchChats() {
        if (FirebaseAuth.getInstance().getUid() == null)
            return;
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