# Smart Pantry Manager

A Java Android app that helps reduce food waste. The user tracks the ingredients
they actually have at home, and the app suggests recipes they can cook using
strictly those ingredients, with no shopping trip required.

## Features
- Pantry management: add, edit and delete ingredients (name, quantity, unit, optional expiry date)
- Pantry list screen using a RecyclerView with a custom adapter
- 20 pre-loaded recipes, seeded into the database on first run
- Suggested Recipes screen using a strict-matching rule: a recipe is only shown if
  EVERY ingredient is in the pantry in at least the required quantity
- Matching is robust to singular/plural names ("tomato" vs "tomatoes") and unit
  differences (e.g. 1 kg vs 1000 g)
- Recipe detail screen with the full ingredient list and method
- Settings screen (expiring-soon highlight, default unit)
- Input validation on the ingredient form and a friendly message when no recipes match

## Database choice: SQLite (SQLiteOpenHelper)
I chose SQLite because the app is entirely about the user's own device data. It needs
no internet connection or backend, data persists between app launches, and it keeps
the project simple to set up and run.

## Setup / Run
1. Install Android Studio.
2. Clone this repository: `git clone https://github.com/sello-bot/SmartPantryManager.git`
3. Open the project in Android Studio and wait for the Gradle sync to finish.
4. Run the app on an emulator or a physical Android device (Android 7.0 / API 24 or higher).

## Tech
Java, Android Studio, SQLite, RecyclerView, Material Components.
