package com.example.eggrun.classes.pet;

import com.example.eggrun.classes.RunSession;

import java.util.List;

public interface Pet {
    List<RunSession> getRunSessionList();
    int getImage();
}
