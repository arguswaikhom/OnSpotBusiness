package com.crown.onspotbusiness.utils;

import androidx.annotation.NonNull;

public class WeekDayHelper {
    public static String toTitleCase(String str) {
        if (str == null) return null;

        boolean space = true;
        StringBuilder builder = new StringBuilder(str);
        final int len = builder.length();

        for (int i = 0; i < len; ++i) {
            char c = builder.charAt(i);
            if (space) {
                if (!Character.isWhitespace(c)) {
                    // Convert to title case and switch out of whitespace mode.
                    builder.setCharAt(i, Character.toTitleCase(c));
                    space = false;
                }
            } else if (Character.isWhitespace(c)) {
                space = true;
            } else {
                builder.setCharAt(i, Character.toLowerCase(c));
            }
        }

        return builder.toString();
    }

    public String[] getWeekDays() {
        return new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    }

    public String[] decodeDays(String days) {
        String[] decodedDays = new String[days.length()];
        for (int i = 0; i < days.length(); i++) {
            decodedDays[i] = getDayName(Integer.parseInt(String.valueOf(days.charAt(i))));
        }
        return decodedDays;
    }

    public String getWeekDaysCode(String[] days) {
        StringBuilder code = new StringBuilder();
        for (String day : days) code.append(getDayCode(day));
        return code.toString();
    }

    public int getDayCode(String day) {
        switch (day.toLowerCase()) {
            case "sunday":
                return WeekDay.SUNDAY.getValue();
            case "monday":
                return WeekDay.MONDAY.getValue();
            case "tuesday":
                return WeekDay.TUESDAY.getValue();
            case "wednesday":
                return WeekDay.WEDNESDAY.getValue();
            case "thursday":
                return WeekDay.THURSDAY.getValue();
            case "friday":
                return WeekDay.FRIDAY.getValue();
            case "saturday":
                return WeekDay.SATURDAY.getValue();
            default:
                return -1;
        }
    }

    public String getDayName(int code) {
        switch (code) {
            case 0:
                return "Sunday";
            case 1:
                return "Monday";
            case 2:
                return "Tuesday";
            case 3:
                return "Wednesday";
            case 4:
                return "Thursday";
            case 5:
                return "Friday";
            case 6:
                return "Saturday";
            default:
                return null;
        }
    }

    public enum WeekDay {
        SUNDAY(0), MONDAY(1), TUESDAY(2), WEDNESDAY(3), THURSDAY(4), FRIDAY(5), SATURDAY(6);
        private final int value;

        WeekDay(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        @NonNull
        @Override
        public String toString() {
            return toTitleCase(this.name());
        }
    }
}