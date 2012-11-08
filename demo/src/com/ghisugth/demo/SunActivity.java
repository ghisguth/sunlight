package com.ghisugth.demo;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.opengl.GLSurfaceView;
import android.os.Bundle;


public class SunActivity extends Activity implements ActionBar.TabListener {
    private GLSurfaceView glSurfaceView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Tab tab;
        ActionBar actionBar = getActionBar();

        actionBar.setTitle(R.string.title);
        actionBar.setSubtitle(R.string.subtitle);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        tab = actionBar.newTab();
        tab.setTabListener(this);
        tab.setText(R.string.tab_test);
        tab.setTag(new Test(this));
        actionBar.addTab(tab);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (glSurfaceView != null) {
            glSurfaceView.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (glSurfaceView != null) {
            glSurfaceView.onResume();
        }
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction fragmentTransaction) {
        glSurfaceView = (GLSurfaceView) tab.getTag();
        setContentView(glSurfaceView);
    }
}
