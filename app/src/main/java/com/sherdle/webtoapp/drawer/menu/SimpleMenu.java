package com.sherdle.webtoapp.drawer.menu;

import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

/**
 * This file is part of the Web2App template
 * For license information, please check the LICENSE
 * file in the root of this project
 *
 * @author Sherdle
 * Copyright 2016
 */
public class SimpleMenu extends SimpleAbstractMenu {

    public SimpleMenu(Menu menu, MenuItemCallback callback){
        super();
        this.menu = menu;
        this.callback = callback;
    }

    public MenuItem add(String title, int drawable, Action action) {
        return add(menu, title, drawable, action);
    }

}
