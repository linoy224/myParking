package com.example.mypark.ui;

// Bundle משמש להעברת מידע בין מצבים (כמו שמירת מצב המסך)
import android.os.Bundle;

// מחלקת בסיס לפעילות (Activity) עם תמיכה בעיצוב מודרני
import androidx.appcompat.app.AppCompatActivity;

// Fragment – רכיב מסך שניתן להחליף בתוך Activity
import androidx.fragment.app.Fragment;

// קובץ משאבים (layouts, ids וכו')
import com.example.mypark.R;

// רכיב טאבים (לשוניות)
import com.google.android.material.tabs.TabLayout;

 //* Activity ראשי של האפליקציה
 //* אחראי על:
 //* - יצירת הטאבים (Explore, Settings)
 //* - מעבר בין Fragmentים

public class MainActivity extends AppCompatActivity {

    //פונקציה שמופעלת כשנוצר ה-Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // קובע איזה XML יוצג (layout של המסך הראשי)
        setContentView(R.layout.activity_mainpage);

        // מציאת רכיב ה-TabLayout מתוך ה-XML
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        //הוספת טאבים (לשוניות)
        tabLayout.addTab(tabLayout.newTab()
                .setText("Explore") // טקסט שיוצג
                .setIcon(android.R.drawable.ic_menu_compass)); // אייקון

        tabLayout.addTab(tabLayout.newTab()
                .setText("Settings") // טקסט
                .setIcon(android.R.drawable.ic_menu_preferences)); // אייקון

        //טעינת Fragment ברירת מחדל
        if (savedInstanceState == null) {
            // אם זה הפעלה ראשונה (ולא סיבוב מסך למשל)
            loadFragment(new HomeFragment()); // מציג מסך ראשי
        }

        //מאזין לבחירת טאבים
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            //כאשר המשתמש בוחר טאב
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                Fragment fragment; // משתנה שיחזיק את המסך הבא

                // 0/1 בדיקה איזה טאב נבחר לפי המיקום שלו
                if (tab.getPosition() == 1) {
                    // אם זה הטאב השני → Settings
                    fragment = new SettingsFragment();
                } else {
                    // אחרת → Home (Explore)
                    fragment = new HomeFragment();
                }

                loadFragment(fragment); // החלפת המסך
            }

            //כאשר טאב מפסיק להיות נבחר(לא משתמשים כאן)
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            //כאשר לוחצים שוב על אותו טאב(לא משתמשים כאן)
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    //פונקציה שמחליפה Fragment בתוך המסך
    private void loadFragment(Fragment fragment) {

        getSupportFragmentManager() // מנהל ה-Fragmentים
                .beginTransaction() // התחלת פעולה
                .replace(R.id.fragmentContainer, fragment)
                // מחליף את ה-Fragment שמוצג בתוך container
                .commit(); // ביצוע הפעולה
    }
}