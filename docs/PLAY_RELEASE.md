# מדריך שחרור ל-Google Play

המדריך עובר על כל מה שצריך כדי לבנות גרסת פרודקשן של TaskFlow ולפרסם אותה ב-Google Play.

---

## 1. הקמת Firebase

1. היכנס ל-[Firebase Console](https://console.firebase.google.com) וצור פרויקט חדש (אפשר לכבות את Google Analytics).
2. **Add app → Android**, עם ה-package name: `com.jel.taskflow`.
3. הוסף את טביעות האצבע (SHA-1 ו-SHA-256) של **כל** מפתחות החתימה:
   - **מפתח debug** (לפיתוח):
     ```bash
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```
   - **מפתח ההעלאה (upload key)**: ראה סעיף 3.
   - **מפתח החתימה של Play (App signing key)**: אחרי ההעלאה הראשונה, ב-Play Console:
     **Test and release → Setup → App signing** → העתק את SHA-1 ו-SHA-256.
     > ⚠️ בלי טביעת האצבע הזו ההתחברות עם Google **לא תעבוד** בגרסה שמותקנת מהחנות.
4. הורד את `google-services.json` ושים אותו ב-`app/google-services.json`.
   הקובץ מופיע ב-`.gitignore`, ובלעדיו הבנייה נכשלת.
5. **Authentication → Sign-in method → Google → Enable**. בחר אימייל תמיכה ושמור.
   אחרי ההפעלה, הורד שוב את `google-services.json`. הוא צריך לכלול `oauth_client` מסוג 3 (Web client).
6. **Firestore Database → Create database** במצב **production**, ובחר אזור (למשל `eur3` / `europe-west`).
7. פרוס את חוקי האבטחה מהריפו:
   ```bash
   npm install -g firebase-tools
   firebase login
   firebase use --add            # בחר את הפרויקט
   firebase deploy --only firestore:rules
   ```
   החוקים (`firestore.rules`) מאפשרים לכל משתמש לקרוא ולכתוב **רק** את המשימות שלו ב-`users/{uid}/tasks`.

## 2. מדיניות פרטיות

ל-Play חובה לספק כתובת URL ציבורית של מדיניות פרטיות.
1. ערוך את `PRIVACY_POLICY.md` ומלא את השדות המסומנים `FILL IN` (תאריך ואימייל ליצירת קשר).
2. פרסם אותו בכתובת ציבורית, למשל דרך GitHub Pages או ישירות מהקובץ בריפו הציבורי.
3. עדכן את `privacy_policy_url` ב-`app/src/main/res/values/strings.xml` אם הכתובת שונה.

## 3. מפתח העלאה (Upload key)

```bash
keytool -genkeypair -v -keystore taskflow-upload.jks -alias upload \
        -keyalg RSA -keysize 2048 -validity 10000
```

צור קובץ `keystore.properties` בשורש הפרויקט. הוא ב-`.gitignore`, **אסור להעלות אותו לגיט**:

```properties
storeFile=taskflow-upload.jks
storePassword=********
keyAlias=upload
keyPassword=********
```

אפשר גם להגדיר משתני סביבה (ל-CI): `TASKFLOW_KEYSTORE_FILE`, `TASKFLOW_KEYSTORE_PASSWORD`,
`TASKFLOW_KEY_ALIAS`, `TASKFLOW_KEY_PASSWORD`.

> שמור גיבוי של קובץ ה-`.jks` ושל הסיסמאות במקום בטוח. אם הם הולכים לאיבוד אפשר לבקש איפוס מפתח ההעלאה מ-Play, אבל זה תהליך איטי.

## 4. בניית ה-App Bundle

1. העלה את `versionCode` (וגם את `versionName`) ב-`app/build.gradle.kts` לפני **כל** העלאה.
2. בנה:
   ```bash
   ./gradlew bundleRelease
   ```
   הקובץ ייווצר ב-`app/build/outputs/bundle/release/app-release.aab`.
3. גרסת release נבנית עם R8 (כיווץ וערפול). קובץ המיפוי לפענוח קריסות נמצא ב-
   `app/build/outputs/mapping/release/mapping.txt`. Play Console מקבל אותו אוטומטית מתוך ה-AAB.

## 5. Play Console

1. **Create app** → שם, שפת ברירת מחדל, App, Free.
2. **Test and release → Testing → Internal testing**: העלה את ה-AAB. התחל עם בדיקה פנימית.
   חשבונות אישיים חדשים צריכים גם **בדיקה סגורה של 12 בודקים לפחות למשך 14 יום** לפני פרודקשן.
3. אשר את **Play App Signing**, ואז הוסף את טביעות האצבע שלו ל-Firebase (סעיף 1.3).
4. **App content**:
   - **Privacy policy**: הכתובת מסעיף 2.
   - **App access**: כל הפונקציונליות זמינה בלי התחברות ("Continue without an account"), אז אין צורך בפרטי גישה.
   - **Ads**: אין פרסומות.
   - **Content rating**: מלא את השאלון (אפליקציית פרודוקטיביות, בלי תוכן רגיש).
   - **Target audience**: 13+ (או 18+).
   - **Data safety**:
     | שאלה | תשובה |
     |------|-------|
     | האם האפליקציה אוספת או משתפת נתונים? | כן (רק למשתמשים שמתחברים) |
     | Personal info → Name, Email address, User IDs | נאסף, לא משותף. מטרה: App functionality, Account management. אופציונלי (רק עם התחברות) |
     | App activity → Other user-generated content (תוכן המשימות) | נאסף, לא משותף. מטרה: App functionality. אופציונלי |
     | מוצפן בתעבורה? | כן |
     | אפשר לבקש מחיקה? | כן, במסך החשבון ("מחק חשבון") ובכתובת המייל שבמדיניות הפרטיות |
   - **Account deletion**: Play דורש **כתובת URL** שבה אפשר לבקש מחיקת חשבון. אפשר להפנות לסעיף "Deleting your data" במדיניות הפרטיות.
5. **Store listing**: תיאור קצר ומלא, אייקון 512×512, Feature graphic בגודל 1024×500, וצילומי מסך מתיקיית `screenshots/`.
6. **Production → Create new release** → העלה את ה-AAB → שלח לבדיקה.

## 6. בדיקה לפני שחרור (Checklist)

- [ ] מצב אורח: הוספה, עריכה, מחיקה ו"בטל" עובדים בלי אינטרנט.
- [ ] התחברות עם Google מהמסך הראשון ומ"מסך החשבון".
- [ ] משימות שנוצרו כאורח מופיעות ב-Firestore Console אחרי ההתחברות.
- [ ] שינוי במכשיר אחד מופיע במכשיר אחר אחרי פתיחה מחדש של האפליקציה (או "גבה עכשיו").
- [ ] התנתקות: המשימות נשארות במכשיר.
- [ ] התקנה מחדש והתחברות: המשימות משוחזרות.
- [ ] מחיקת חשבון: המסמכים נמחקים מ-Firestore והחשבון נמחק מ-Authentication.
- [ ] ההתראה היומית מגיעה (בחלון של עד כ-10 דקות מהשעה שנבחרה).
- [ ] שדרוג מהגרסה הקודמת (מסד נתונים v4) שומר את כל המשימות.
- [ ] בדיקה של גרסת **release** (ולא רק debug) מתוך מסלול הבדיקה הפנימית ב-Play.
