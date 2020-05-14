package com.sherdle.webtoapp.drawer.menu;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * This file is part of the Web2App template
 * For license information, please check the LICENSE
 * file in the root of this project
 *
 * @author Sherdle
 * Copyright 2016
 */
public class Action implements Serializable{

    public String name;
    public String url;

    public Action(String name, String url){
        this.name = name;
        this.url = url;
    }

}
