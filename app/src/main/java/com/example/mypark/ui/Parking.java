package com.example.mypark.ui;

public class Parking {

        // 1. הגדרת המשתנים (Fields) - המידע שכל חניה "תזכור"
        private String name;
        private double lat;
        private double lng;
        private String time;

        // 2. הבנאי (Constructor) - כאן כנראה הייתה השגיאה!
        // הוא חייב להיות Public, ללא סוג החזרה (בלי void), ושמו חייב להיות זהה לשם המחלקה.
        public Parking(String name, double lat, double lng, String time) {
            this.name = name;
            this.lat = lat;
            this.lng = lng;
            this.time = time;
        }

        // 3. פונקציות ה-Getters
        // אלו פונקציות רגילות, לכן הן חייבות return type (כמו String או double)
        public String getName() {
            return name;
        }

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }

        public String getTime() {
            return time;
        }
    }

