package im.socks.yysk;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cole on 2017/11/2.
 */

public class FragmentStack {
    private FragmentManager fm;
    private Activity activity;
    private List<Fragment> fragments = new ArrayList<>();

    public FragmentStack(Activity activity, FragmentManager fm) {
        this.activity = activity;
        this.fm = fm;
    }

    /**
     * 执行返回，和按下返回键的处理一致，会检查当前的fragment是否实现了OnBackListener
     *
     * @return 返回true表示已经处理，返回false表示没有处理
     */
    public boolean back() {
        return back(true);
    }

    /**
     * 执行返回，不检查OnBackListener，直接关闭当前的fragment
     *
     * @param checkListener true表示检查OnBackListener
     * @return 返回true表示已经处理，返回false表示没有处理
     */
    public boolean back(boolean checkListener) {
        if (checkListener && fragments.size() > 0) {
            Fragment fragment = fragments.get(fragments.size() - 1);
            if (fragment instanceof OnBackListener) {
                OnBackListener listener = (OnBackListener) fragment;
                if (listener.onBack()) {
                    return true;
                }
            }
        }

        hideKeyboard();
        if (fragments.size() > 1) {
            FragmentTransaction ft = fm.beginTransaction();
            Fragment fragment = fragments.remove(fragments.size() - 1);
            ft.remove(fragment);
            ft.show(fragments.get(fragments.size() - 1));
            ft.commit();
            return true;
        } else {
            //如果只有1个或者没有
            return false;
        }
    }


    /**
     * 显示指定的fragment
     *
     * @param fragment
     * @param tag       可以为null，如果不为null，且对应的fragment，就显示对应的fragment
     * @param isReplace true表示替代当前的fragment，false表示添加到堆栈后面
     */
    public void show(Fragment fragment, String tag, boolean isReplace) {
        //Log.e("Yysk","MainActivity.showFragment="+tag+",");
        hideKeyboard();
        FragmentTransaction ft = fm.beginTransaction();
        if (tag != null) {
            MyLog.d("showFragment,tag=%s,fragment=%s",tag,fm.findFragmentByTag(tag));
            Fragment existFragment = fm.findFragmentByTag(tag);
            if (existFragment != null) {
                int index = fragments.indexOf(existFragment);
                if (index == -1) {
                    //不存在，表示直接使用了fm.add()，而不是通过FragmentStack的方法
                    MyLog.e("编程错误，使用了其它方式添加fragment=%s",tag);
                    return;
                }

                for (int i = 0; i < index; i++) {
                    if (fragments.get(i).isVisible()) {
                        ft.hide(fragments.get(i));
                    }
                }
                for (int i = index + 1; i < fragments.size(); i++) {
                    ft.remove(fragments.get(i));
                }

                ft.show(existFragment);
                ft.commit();
                fragments = fragments.subList(0, index + 1);
                return;
            }
        }

        if (fragment != null) {

            for (int i = 0; i < fragments.size(); i++) {
                if (fragments.get(i).isVisible()) {
                    ft.hide(fragments.get(i));
                }
            }
            if (isReplace) {
                //删除最后一个
                Fragment lastFragment = fragments.remove(fragments.size() - 1);
                ft.remove(lastFragment);
            } else {

            }
            //tag可以为null
            ft.add(R.id.rootLayout, fragment, tag);
            ft.show(fragment);
            ft.commit();

            fragments.add(fragment);
        }

    }


    private void hideKeyboard() {
        View view = activity.findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getRootView().getWindowToken(), 0);
        }
    }
}
