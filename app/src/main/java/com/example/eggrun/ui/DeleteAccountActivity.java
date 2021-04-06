package com.example.eggrun.ui;

import androidx.fragment.app.Fragment;

import java.io.File;

public class DeleteAccountActivity extends SingleFragmentActivity {
    private static final String TAG = "DeleteAccountActivity";
    private DeleteAccountFragment mDeleteAccountFragment;

    @Override
    protected Fragment createFragment(){
        if (mDeleteAccountFragment == null){
            File mDirectory = (File) getIntent().getSerializableExtra("directory");
            mDeleteAccountFragment = new DeleteAccountFragment(mDirectory);
        }
        return mDeleteAccountFragment;
    }
}