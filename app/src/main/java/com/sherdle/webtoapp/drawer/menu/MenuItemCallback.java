package com.sherdle.webtoapp.drawer.menu;

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
public interface MenuItemCallback {

    void menuItemClicked(Action action, MenuItem item);
}
