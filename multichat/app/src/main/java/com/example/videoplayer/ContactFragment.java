package com.example.videoplayer;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactFragment extends Fragment {

    private  View ContactView;
    private RecyclerView myContactList;
    private DatabaseReference contactsRef, userRef;
    private FirebaseAuth mAuth;
    private String currentUserId;



    public ContactFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactView =  inflater.inflate(R.layout.fragment_contact, container, false);
        myContactList = ContactView.findViewById(R.id.contact_recycler_view);
        myContactList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("USERS");

        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);


        return ContactView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ContactRecylerViewHolder> adapter  =new FirebaseRecyclerAdapter<Contacts, ContactRecylerViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactRecylerViewHolder holder, int position, @NonNull Contacts model) {

                String userIds = getRef(position).getKey();
                userRef.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                       if(dataSnapshot.exists())
                       {


                           if (dataSnapshot.child("userState").hasChild("state"))
                           {
                               String state = dataSnapshot.child("userState").child("state").getValue().toString();
                               String date = dataSnapshot.child("userState").child("date").getValue().toString();
                               String time = dataSnapshot.child("userState").child("time").getValue().toString();

                               if (state.equals("online"))
                               {
                                   holder.onlineIcon.setVisibility(View.VISIBLE);
                                   Toast.makeText(getContext(), "Online User: ", Toast.LENGTH_SHORT).show();
                               }
                               else if (state.equals("offline"))
                               {
                                   holder.onlineIcon.setVisibility(View.INVISIBLE);
                               }
                           }
                           else
                           {
                               holder.onlineIcon.setVisibility(View.INVISIBLE);
                           }

                           if(dataSnapshot.hasChild("Image"))
                           {

                               String p_image = dataSnapshot.child("Image").getValue().toString();
                               String p_name = dataSnapshot.child("Name").getValue().toString();
                               String p_status = dataSnapshot.child("Status").getValue().toString();

                               holder.visitorName.setText(p_name);
                               holder.visitorStatus.setText(p_status);
                               Picasso.get().load(p_image).placeholder(R.drawable.index).into(holder.visitorProfileImg);
                           }else
                           {

                               String p_name = dataSnapshot.child("Name").getValue().toString();
                               String p_status = dataSnapshot.child("Status").getValue().toString();

                               holder.visitorName.setText(p_name);
                               holder.visitorStatus.setText(p_status);

                           }

                       }else
                       {
                           Toast.makeText(getContext(), "No contact exists", Toast.LENGTH_SHORT).show();
                       }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ContactRecylerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent,false);
                ContactRecylerViewHolder viewHolder = new ContactRecylerViewHolder(view);
                return viewHolder;
            }
        };
        myContactList.setAdapter(adapter);
        adapter.startListening();
    }


    public static  class ContactRecylerViewHolder extends  RecyclerView.ViewHolder{


        TextView visitorName, visitorStatus;
        CircleImageView visitorProfileImg;
        ImageView onlineIcon;

        public ContactRecylerViewHolder(@NonNull View itemView) {
            super(itemView);

            visitorName = itemView.findViewById(R.id.user_profile_name);
            visitorStatus = itemView.findViewById(R.id.user_profile_status);
            visitorProfileImg = itemView.findViewById(R.id.users_profile_image);
            onlineIcon = itemView.findViewById(R.id.user_online_icon);




        }
    }
}
