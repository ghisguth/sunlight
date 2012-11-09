package com.ghisugth.demo;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.opengl.GLSurfaceView;
import android.os.Bundle;


public class SunActivity extends Activity {
    private GLSurfaceView glSurfaceView;
    private TabHandler tabHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int currentApiVersion = android.os.Build.VERSION.SDK_INT;

        if (currentApiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            Tab tab;
            ActionBar actionBar = getActionBar();

            actionBar.setTitle(R.string.title);
            actionBar.setSubtitle(R.string.subtitle);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            tabHandler = new TabHandler(this);

            tab = actionBar.newTab();
            tab.setTabListener(tabHandler);
            tab.setText(R.string.tab_test);
            tab.setTag(new Test(this));
            actionBar.addTab(tab);
        } else {
            glSurfaceView = new Test(this);
            setContentView(glSurfaceView);
        }

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

    public void setSurfaceView(GLSurfaceView glSurfaceView) {
        this.glSurfaceView = glSurfaceView;
        setContentView(glSurfaceView);
    }

    private class TabHandler implements ActionBar.TabListener {
        private SunActivity activity;

        public TabHandler(SunActivity activity) {
            this.activity = activity;
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction fragmentTransaction) {
            GLSurfaceView glSurfaceView = (GLSurfaceView) tab.getTag();
            activity.setSurfaceView(glSurfaceView);
        }
    }


}
