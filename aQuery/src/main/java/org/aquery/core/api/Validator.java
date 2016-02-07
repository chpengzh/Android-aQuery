package org.aquery.core.api;

import org.aquery.core.exception.IllegalDataError;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chpengzh chpengzh@foxmail.com
 */
public abstract class Validator {

    /***
     * Get a new Validator Instance
     *
     * @return Validator instance
     */
    public static Validator newInstance() {
        return new Impl();
    }

    /***
     * append a non-fulfillment
     *
     * @param condition unfulfilled condition
     * @return Validator instance
     */
    public abstract Validator filter(boolean condition);

    /***
     * append a non-fulfillment with error message
     *
     * @param condition unfulfilled condition
     * @param message   error message
     * @return Validator instance
     */
    public abstract Validator filter(boolean condition, String message);

    /***
     * append a non-fulfillment  with message and data clean action
     *
     * @param condition unfulfilled condition
     * @param message   error message
     * @param cleaner   data clean action if necessary
     * @return Validator instance
     */
    public abstract Validator filter(boolean condition, String message, DataCleaner cleaner);

    /***
     * append a fulfillment
     *
     * @param condition fulfilled condition
     * @return Validator instance
     */
    public abstract Validator fulfill(boolean condition);

    /***
     * append a fulfillment and error message
     *
     * @param condition fulfilled condition
     * @param message   error message if unfulfilled
     * @return Validator instance
     */
    public abstract Validator fulfill(boolean condition, String message);

    /***
     * append a fulfillment with error message and data clean action
     *
     * @param condition fulfilled condition
     * @param message   error message if unfulfilled
     * @param cleaner   data clean action
     * @return Validator instance
     */
    public abstract Validator fulfill(boolean condition, String message, DataCleaner cleaner);

    /***
     * append a fulfillment checker property
     *
     * @param checker checker property instance
     * @return Validator instance
     */
    public abstract Validator fulfill(Checker checker);

    /***
     * wash a checker list
     *
     * @param list list to be washed
     * @return Validator instance
     */
    public abstract Validator washList(List<Checker> list);

    /***
     * check a condition, which error message will be add to warning if unfulfilled
     *
     * @param condition condition to be fulfilled
     * @param message   error message
     * @return Validator instance
     */
    public abstract Validator checkWarning(boolean condition, String message);

    /***
     * check a validator follow its validate condition principles
     *
     * @return whether it has any validate warning
     */
    public abstract boolean validate() throws IllegalDataError;

    public abstract List<String> getWarning();

    public interface DataCleaner {
        void onClean();
    }

    private static class Impl extends Validator {

        ArrayList<Boolean> conditions = new ArrayList<>();
        ArrayList<String> messages = new ArrayList<>();
        ArrayList<DataCleaner> cleaners = new ArrayList<>();
        ArrayList<String> warning = new ArrayList<>();

        private static String getDefault() {
            StackTraceElement[] cause = Thread.currentThread().getStackTrace();
            String tag = "Validator";
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
        public Validator filter(boolean condition) {
            return filter(condition, null, null);
        }

        @Override
        public Validator filter(boolean condition, String message) {
            return filter(condition, message, null);
        }

        @Override
        public Validator filter(boolean condition, String message, DataCleaner cleaner) {
            conditions.add(!condition);
            messages.add(message);
            cleaners.add(cleaner);
            return this;
        }

        @Override
        public Validator fulfill(boolean condition) {
            return fulfill(condition, null, null);
        }

        @Override
        public Validator fulfill(boolean condition, String message) {
            return fulfill(condition, message, null);
        }

        @Override
        public Validator fulfill(boolean condition, String message, DataCleaner cleaner) {
            conditions.add(condition);
            messages.add(message);
            cleaners.add(cleaner);
            return this;
        }

        @Override
        public Validator fulfill(final Checker checker) {
            try {
                return fulfill(checker.getValidator().validate(), null, new DataCleaner() {
                    @Override
                    public void onClean() {
                        warning.addAll(checker.getValidator().getWarning());
                    }
                });
            } catch (IllegalDataError illegalDataError) {
                return fulfill(false, illegalDataError.getMessage(), null);
            }
        }

        @Override
        public Validator washList(final List<Checker> list) {
            for (final Checker checker : list) {
                try {
                    fulfill(checker.getValidator().validate(), null, new DataCleaner() {
                        @Override
                        public void onClean() {
                            warning.addAll(checker.getValidator().getWarning());
                        }
                    });
                } catch (final IllegalDataError illegalDataError) {
                    fulfill(false, null, new DataCleaner() {
                        @Override
                        public void onClean() {
                            warning.addAll(checker.getValidator().getWarning());
                            warning.add(illegalDataError.getMessage());
                            list.remove(checker);
                        }
                    });
                }
            }
            return this;
        }

        @Override
        public Validator checkWarning(boolean condition, String message) {
            if (!condition) warning.add(message);
            return this;
        }

        @Override
        synchronized public boolean validate() throws IllegalDataError {
            warning.clear();
            for (int i = 0; i < conditions.size(); i++) {
                if (!conditions.get(i) && cleaners.get(i) == null) {
                    throw new IllegalDataError(messages.get(i) == null ? getDefault() : messages.get(i));
                } else if (!conditions.get(i)) {
                    cleaners.get(i).onClean();
                    warning.add(messages.get(i) == null ? getDefault() : messages.get(i));
                }
            }
            return warning.size() == 0;
        }

        @Override
        public List<String> getWarning() {
            return warning;
        }
    }
}
