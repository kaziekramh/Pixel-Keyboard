# পিক্সেল বাংলা কীবোর্ড (Android 7+)

অভ্র ফোনেটিক + ইংরেজি, ক্লিপবোর্ড, সাজেশন, অভিধান, ইমোজী, ৫টি থীম।
কীবোর্ডের UI হলো `app/src/main/assets/keyboard.html` (WebView-এ চলে), আর `KeyboardService.kt` সেটাকে সিস্টেম কীবোর্ড বানায়।

## APK বানানো
**ক) GitHub Actions (Android Studio ছাড়া)**
1. github.com-এ নতুন repository খুলে এই ফোল্ডারের সব ফাইল আপলোড করুন (`.github/workflows/build.yml` সহ; ফোন থেকে `.github` ফোল্ডার না গেলে "Add file → Create new file"-এ ওই পাথ লিখে YAML পেস্ট করুন)।
2. Actions ট্যাব → "Build APK" → Run workflow।
3. শেষ হলে নিচে Artifacts থেকে `pixel-keyboard-apk` নামিয়ে unzip করে `app-debug.apk` ইনস্টল করুন।

**খ) Android Studio:** File → Open → এই ফোল্ডার → Sync → Build → Build APK(s)।

## চালু করা
1. APK ইনস্টল করুন (Unknown sources-এর অনুমতি দিতে হতে পারে)।
2. "পিক্সেল কীবোর্ড" অ্যাপ খুলে **১. কীবোর্ড চালু করুন** → তালিকায় অন করুন।
3. যেকোনো টেক্সট বক্সে ট্যাপ করে **২. কীবোর্ড বেছে নিন** থেকে সিলেক্ট করুন।

## দরকারি
- Android 7-এ Play Store থেকে "Android System WebView" আপডেট রাখুন (কমপক্ষে Chrome 60-এর সমতুল্য)।
- লং-প্রেস করলে কীর ডানদিকের সংখ্যা/চিহ্ন টাইপ হয়।
