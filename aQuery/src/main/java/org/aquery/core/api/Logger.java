package org.aquery.core.api;

import android.util.Log;

import org.aquery.core.AQuery;

/**
 * @author chpengzh chpengzh@foxmail.com
 */
public interface Logger {

    /***
     * debug log
     *
     * @param s message as String
     */
    void d(String s, Object... args);

    /***
     * debug log
     *
     * @param e error as Throwable
     */
    void d(Object e);

    /***
     * info log
     *
     * @param s message as String
     */
    void i(String s, Object... args);

    /***
     * info log
     *
     * @param e error as Throwable
     */
    void i(Object e);

    /***
     * error log
     *
     * @param s message as String
     */
    void e(String s, Object... args);

    /***
     * error log
     *
     * @param e message as Throwable
     */
    void e(Object e);

    class Impl implements Logger {

        private AQuery $;

        public Impl(AQuery $) {
            this.$ = $;
        }

        private static String getTag() {
            StackTraceElement[] cause = Thread.currentThread().getStackTrace();
            String tag = "Logging";
            for (int i = 1; i < cause.length; i++) {
                if (cause[i - 1].getClassName().equals(Impl.class.getName())
                        && !cause[i].getClassName().equals(Impl.class.getName())) {
                    tag = String.format("%s(%d)", cause[i].getClassName().replaceAll("^.*\\.", ""),
                            cause[i].getLineNumber());
                    break;
                }
            }
            return tag;
        }

        @Override
        synchronized public void d(String s, Object... args) {
            if (s == null || s.trim().length() == 0) return;
            if ($.config.DEBUG_LOG) {
                String tag = getTag();
                for (String line : (args.length == 0 ? s : String.format(s, args)).split("\\n")) {
                    Log.d(tag, line);
                }
            }
        }

        @Override
        synchronized public void d(Object e) {
            String tag = getTag();
            if (e != null && e instanceof Throwable) {
                if ($.config.DEBUG_LOG) {
                    Log.d(tag, e.toString());
                    for (StackTraceElement element : ((Throwable) e).getStackTrace()) {
                        Log.d(tag, element.toString());
                    }
                }
            } else {
                Log.d(tag, String.valueOf(e));
            }
        }

        @Override
        synchronized public void i(String s, Object... args) {
            if (s == null || s.trim().length() == 0) return;
            if ($.config.DEBUG_LOG) {
                for (String line : (args.length == 0 ? s : String.format(s, args)).split("\\n")) {
                    Log.i(getTag(), line);
                }
            }
        }

        @Override
        synchronized public void i(Object e) {
            String tag = getTag();
            if (e != null && e instanceof Throwable) {
                if ($.config.DEBUG_LOG) {
                    Log.i(tag, e.toString());
                    for (StackTraceElement element : ((Throwable) e).getStackTrace()) {
                        Log.i(tag, element.toString());
                    }
                }
            } else {
                Log.i(tag, String.valueOf(e));
            }
        }

        @Override
        synchronized public void e(String s, Object... args) {
            if (s == null || s.trim().length() == 0) return;
            if ($.config.DEBUG_LOG) {
                for (String line : (args.length == 0 ? s : String.format(s, args)).split("\\n")) {
                    Log.e(getTag(), line);
                }
            }
        }

        @Override
        synchronized public void e(Object e) {
            String tag = getTag();
            if (e != null && e instanceof Throwable) {
                if ($.config.DEBUG_LOG) {
                    Log.e(tag, e.toString());
                    for (StackTraceElement element : ((Throwable) e).getStackTrace()) {
                        Log.e(tag, element.toString());
                    }
                }
            } else {
                Log.e(tag, String.valueOf(e));
            }
        }
    }
}
