package im.socks.yysk;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Created by cole on 2017/10/24.
 */

public class EventBus {

    private Map<String, List<IListener<?>>> listeners = new HashMap<>();
    //private ExecutorService executor;
    private Object lock = new Object();
    private Handler handler = null;

    public EventBus() {
        handler = new Handler(Looper.getMainLooper());
    }

    /**
     * @param name     事件名，“*”表示监听全部
     * @param listener
     */

    public <T> void on(String name, IListener<T> listener) {
        synchronized (lock) {
            List<IListener<?>> list = listeners.get(name);
            if (list == null) {
                list = new ArrayList<>();
                listeners.put(name, list);
            }

            if (!list.contains(listener)) {
                list.add(listener);
            }

        }
    }

    public void un(String name, IListener<?> listener) {
        synchronized (lock) {
            List<IListener<?>> list = listeners.get(name);
            if (list != null) {
                list.remove(listener);
            }
        }
    }

    /**
     * 总是在主线程中执行
     *
     * @param name
     * @param data
     * @param async true表示总是异步执行(在主线程)，false表示如果当前为主线程，直接执行，否则扔到主线程执行
     */
    public <T> void emit(final String name, final T data, boolean async) {
        if (async || Looper.getMainLooper() != Looper.myLooper()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    notifyListeners(name, data);
                }
            });
        } else {
            notifyListeners(name, data);
        }

    }

    /**
     * 在指定的executor中执行事件
     *
     * @param name
     * @param data
     * @param executor
     * @param <T>
     */
    public <T> void emit(final String name, final T data, Executor executor) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                notifyListeners(name, data);
            }
        });
    }

    private void notifyListeners(final String name, final Object data) {
        List<IListener<?>> list = new ArrayList<>();
        synchronized (lock) {
            List<IListener<?>> list1 = listeners.get(name);
            List<IListener<?>> list2 = listeners.get("*");// 监听全部的事件
            if (list1 != null) {
                list.addAll(list1);
            }
            if (list2 != null) {
                list.addAll(list2);
            }
        }

        for (IListener listener : list) {
            try {
                listener.onEvent(name, data);
            } catch (Exception e) {
                MyLog.e(e);
            }
        }
    }

    public void destroy() {

    }

    private static <T> T cast(Object obj) {
        return (T) obj;
    }

    public interface IListener<T> {
        void onEvent(String name, T data) throws Exception;
    }

}
