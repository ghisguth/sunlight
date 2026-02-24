package com.ghisguth.demo;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.tabs.TabLayout;

public class SunActivity extends AppCompatActivity {
    private GLSurfaceView glSurfaceView_;
    private LinearLayout root_;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        root_ = new LinearLayout(this);
        root_.setOrientation(LinearLayout.VERTICAL);

        TabLayout tabLayout = new TabLayout(this);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_cubes).setTag(new Cubes(this)));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_grid).setTag(new Grid(this)));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_lines).setTag(new Lines(this)));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_test).setTag(new Test(this)));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_sunv1).setTag(new SunV1(this)));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_sunv2).setTag(new SunV2(this)));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_sunv3).setTag(new SunV3(this)));

        tabLayout.addOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        setSurfaceView((GLSurfaceView) tab.getTag());
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {}

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {}
                });

        root_.addView(
                tabLayout,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        setContentView(root_);

        tabLayout.selectTab(tabLayout.getTabAt(0));
    }

    public void setSurfaceView(GLSurfaceView newSurfaceView) {
        if (glSurfaceView_ != null) {
            glSurfaceView_.onPause();
            root_.removeView(glSurfaceView_);
        }
        glSurfaceView_ = newSurfaceView;
        if (glSurfaceView_ != null) {
            root_.addView(
                    glSurfaceView_,
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
            glSurfaceView_.onResume();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (glSurfaceView_ != null) {
            glSurfaceView_.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (glSurfaceView_ != null) {
            glSurfaceView_.onPause();
        }
    }
}
