package com.mitrukahitesh.asrik.fragments.chatfragments;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.adapters.ChatBotAdapter;
import com.mitrukahitesh.asrik.apis.RetrofitAccessObject;
import com.mitrukahitesh.asrik.helpers.Constants;
import com.mitrukahitesh.asrik.models.BotReply;
import com.mitrukahitesh.asrik.models.Message;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatBot extends Fragment {

    private ImageButton send;
    private EditText message;
    private RecyclerView recyclerView;
    private ChatBotAdapter adapter;
    private String WELCOME_MESSAGE = "asrik welcome";

    public ChatBot() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_bot, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(view).popBackStack();
            }
        });
        adapter = new ChatBotAdapter(ChatList.botChat);
        recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        message = view.findViewById(R.id.message);
        send = view.findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (message.getText().toString().trim().equals(""))
                    return;
                Message m = new Message();
                m.setSender(FirebaseAuth.getInstance().getUid());
                m.setMessage(message.getText().toString());
                m.setTime(System.currentTimeMillis());
                ChatList.botChat.add(m);
                adapter.notifyItemInserted(ChatList.botChat.size() - 1);
                recyclerView.scrollToPosition(ChatList.botChat.size() - 1);
                sendMessage(message.getText().toString().trim());
                message.setText("");
            }
        });
        if (ChatList.botChat.size() == 0)
            sendMessage(WELCOME_MESSAGE);
    }

    private void sendMessage(String m) {
        RetrofitAccessObject.getRetrofitAccessObject()
                .getBotReply(m)
                .enqueue(new Callback<BotReply>() {
                    @Override
                    public void onResponse(Call<BotReply> call, Response<BotReply> response) {
                        Message message = new Message();
                        message.setTime(System.currentTimeMillis());
                        if (response.code() != 200) {
                            Log.i("Asrik: Bot", response.code() + "");
                            message.setMessage(Constants.DEFAULT_BOT_MESSAGE);
                        } else {
                            BotReply reply = response.body();
                            message.setMessage(reply != null ? reply.getReply() : Constants.DEFAULT_BOT_MESSAGE);
                        }
                        message.setSender("");
                        ChatList.botChat.add(message);
                        adapter.notifyItemInserted(ChatList.botChat.size() - 1);
                        recyclerView.scrollToPosition(ChatList.botChat.size() - 1);
                    }

                    @Override
                    public void onFailure(Call<BotReply> call, Throwable t) {
                        Log.i("Asrik: Bot Reply", t.getMessage());
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().findViewById(R.id.bottomAppBar).setVisibility(View.GONE);
        changeStatusBarColor(R.color.theme_color_light);
    }

    @Override
    public void onStop() {
        super.onStop();
        requireActivity().findViewById(R.id.bottomAppBar).setVisibility(View.VISIBLE);
        changeStatusBarColor(R.color.white);
    }

    private void changeStatusBarColor(int color) {
        Window window = requireActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(color, null));
    }

}