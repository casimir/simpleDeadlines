package com.casimirlab.simpleDeadlines.provider;

import android.net.Uri;

public final class DeadlinesContract {
    public static final String AUTHORITY = "com.casimirlab.simpleDeadlines.provider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
    @Deprecated
    public static final String GROUPS_PATH = "groups";

    public static final class Count {
        /**
         * Not instantiable.
         */
        private Count() {
        }

        protected static String TABLE_NAME = "count";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TABLE_NAME);
    }

    protected interface CountColumns {
        String TODAY = "today";
        String URGENT = "urgent";
        String WORRYING = "worrying";
        String NICE = "nice";
        String[] ALL = {TODAY, URGENT, WORRYING, NICE};
    }

    public static final class Deadlines implements DeadlinesColumns {
        /**
         * Not instantiable.
         */
        private Deadlines() {
        }

        protected static String TABLE_NAME = "deadlines";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TABLE_NAME);
        public static final String FILTER_ARCHIVED = "archived";
        public static final String FILTER_GROUP = "group";
        public static final int STATE_NOT_DONE = 0;
        public static final int STATE_DONE = 1;
        public static final int TYPE_ARCHIVED = 0;
        public static final int TYPE_IN_PROGRESS = 1;
    }

    protected interface DeadlinesColumns {
        String ID = "_id";
        String LABEL = "label";
        String GROUP = "groupname";
        String DUE_DATE = "due_date";
        String DONE = "done";
        String[] ALL = {ID, LABEL, GROUP, DUE_DATE, DONE};
    }

    public static final class Groups {
        /**
         * Not instantiable.
         */
        private Groups() {
        }

        protected static String TABLE_NAME = "groups";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TABLE_NAME);
    }
}
