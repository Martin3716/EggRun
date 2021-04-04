package com.example.eggrun.ui;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.eggrun.R;
import com.example.eggrun.classes.Player;
import com.example.eggrun.classes.RunSession;
import com.example.eggrun.classes.pet.Pet;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class RunSessionAdapter extends RecyclerView.Adapter<RunSessionAdapter.ViewHolder>{
    private static final String TAG = "RunSessionAdapter";
    private Context mContext;
    private Pet mPet;

    public RunSessionAdapter(Pet pet){
        Log.d(TAG, "creating new PetAdapter");
        mPet = pet;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        mContext = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.recycler_run_session_layout, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NotNull ViewHolder viewHolder, int position){
        ArrayList<RunSession> runSessionList = (ArrayList<RunSession>) mPet.getRunSessionList();
        RunSession runSession = runSessionList.get(position);
        viewHolder.textButton.setOnClickListener(v -> {
            /*
            Log.d(TAG, "Opening view_pet_activity.");
            Intent intent = new Intent(mContext.getApplicationContext(), ViewPetActivity.class);
            intent.putExtra("position", position);
            mContext.startActivity(intent);

             */
        });
    }

    @Override
    public int getItemCount(){
        return mPet.getRunSessionList().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView textView;
        public Button textButton;

        public ViewHolder(View view){
            super(view);
            textView = view.findViewById(R.id.pet_View);
            textButton = view.findViewById(R.id.view_button);
        }
    }
}
