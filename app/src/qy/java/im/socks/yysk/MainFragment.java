package im.socks.yysk;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;

/**
 * Created by cole on 2017/10/23.
 */

public class MainFragment extends Fragment {

    /**主页*/
    private Fragment homeFragment;
    /**我的*/
    private Fragment myFragment;
    /**我的*/
    private Fragment buyFragment;

    private BottomNavigationView navigationView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_dz, container, false);
        navigationView = view.findViewById(R.id.bottomNavigationView);
        navigationView.setVisibility(View.VISIBLE);
        disableShiftMode(navigationView);

        navigationView.setSelectedItemId(R.id.navigation_home);

        showFragment(R.id.navigation_home);

        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                showFragment(menuItem.getItemId());
                return true;
            }
        });
        navigationView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem menuItem) {

            }
        });

        return view;
    }

    private void showFragment(int id) {

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_NONE);
        Fragment currentFragment = null;
        if (id == R.id.navigation_home) {
            if (homeFragment == null) {
                homeFragment = HomeFragment.newInstance();
            }
            currentFragment = homeFragment;
        } else if (id == R.id.navigation_my) {
            if (myFragment == null) {
                myFragment = MyFragment.newInstance();
            }
            currentFragment = myFragment;
        }else if(id == R.id.navigation_buy){
            if (buyFragment == null) {
                buyFragment = PayFragment.newInstance();
            }
            currentFragment = buyFragment;
        }
        Fragment[] fragments = new Fragment[]{homeFragment, buyFragment, myFragment};
        for (Fragment f : fragments) {
            if (f != currentFragment && f != null) {
                ft.hide(f);
            }
        }
        if (!currentFragment.isAdded()) {
            ft.add(R.id.frameLayout, currentFragment);
        }
        if (currentFragment.isHidden()) {
            ft.show(currentFragment);
        }

        ft.commit();

    }


    @SuppressLint("RestrictedApi")
    private static void disableShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                //noinspection RestrictedApi
                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                //noinspection RestrictedApi
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            MyLog.e(e);
        } catch (IllegalAccessException e) {
            MyLog.e(e);
        }
    }


    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }
}
