# Smart Pantry Manager

A Java Android app (Android Studio) that helps reduce food waste. The user records the
ingredients they already have at home (their "pantry") and the app suggests only the recipes
they can cook **right now** - every ingredient must be present, in a large enough quantity.
No shopping trip required.

Module: Mobile App Development 700 (Richfield) - Practical Assignment.

## Features

- **Pantry List** - all ingredients in a RecyclerView with a custom adapter, loaded from SQLite.
- **Add / Edit Ingredient** - name, quantity, unit (drop-down) and optional expiry date (date picker),
  with validation messages next to each field. Delete is available on the list and on the edit screen.
- **Suggested Recipes** - runs the strict-matching rule; shows a helpful message when nothing qualifies.
- **Recipe Detail** - full ingredient list and numbered method for the chosen recipe.
- **Settings** - two saved preferences (SharedPreferences): highlight items that are expiring soon,
  and ask before deleting.
- **Navigation** - toolbar menu (My Pantry / Suggested Recipes / Settings), Up arrow, and explicit
  Intents that pass record IDs between screens.

## Strict matching rule

`RecipeMatcher.java` suggests a recipe only when **every** required ingredient is in the pantry in at
least the required quantity. A missing ingredient or an insufficient quantity excludes the recipe;
there are no "almost there" results. It copes with messy input by:

- normalising names (`Tomatoes` = `tomato`, `EGGS` = `egg`),
- converting units before comparing (`1 kg` satisfies `500 g`; `1 l` satisfies `200 ml`),
- ignoring describing words (`Olive oil` = `oil`, `Whole milk` = `milk`),
- comparing common counted items by weight (`1 egg` is about `50 g`) and treating water as always available,
- adding up several pantry rows of the same ingredient,
- never treating mass, volume and counted items (pieces / slices / cloves) as interchangeable -
  if units cannot be compared safely the recipe is excluded.

## Database choice: SQLite (SQLiteOpenHelper)

SQLite is built into Android, needs no server, account or internet connection, and the data stays on
the device after the app is closed. The data is small and relational (a recipe has many required
ingredients), which suits SQL tables and foreign keys. `SQLiteOpenHelper` is the approach taught in
the module's persistent-data chapter.

| Table | Columns |
|---|---|
| `ingredients` | id (PK), name, quantity, unit, expiry_date (optional) |
| `recipes` | id (PK), name (unique), instructions |
| `recipe_ingredients` | id (PK), recipe_id (FK -> recipes.id, ON DELETE CASCADE), ingredient_name, quantity, unit |

20 recipes are seeded inside `onCreate()`, which only runs the first time the database file is
created, so recipes are never duplicated.

## Setup

Requirements: Android Studio (Hedgehog or newer) with the Android SDK (API 34) installed.

1. Clone the repository and open the project folder in Android Studio (**File > Open**).
2. Android Studio creates `local.properties` automatically with your own SDK path, for example
   `sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk`. This file is listed in `.gitignore`
   and must not be committed. If you see *"SDK location not found"*, open **File > Project Structure >
   SDK Location** (or install the SDK from **Tools > SDK Manager**) and Android Studio rewrites the file.
3. Wait for **Gradle sync** to finish (internet needed the first time).

## Run

1. Create or start an emulator (**Tools > Device Manager**, API 24 or higher) or connect a phone.
2. Press **Run** (green triangle). The app opens on the Pantry List.

## Run the unit tests

In the Project view open `app/src/test/java/com/example/smartpantry/`, right-click
`RecipeMatcherTest` (or the whole folder) and choose **Run Tests**. They cover missing ingredients,
insufficient quantities, name normalisation, unit conversion and conservative failure on unsafe units.

## Project structure

```
app/src/main/java/com/example/smartpantry/
  MainActivity.java                 Pantry List
  AddEditIngredientActivity.java    Add / Edit Ingredient (+ validation)
  SuggestedRecipesActivity.java     Suggested Recipes (strict matches only)
  RecipeDetailActivity.java         Recipe Detail
  SettingsActivity.java             Settings (SharedPreferences)
  BaseActivity.java                 Shared toolbar menu / navigation
  DatabaseHelper.java               SQLiteOpenHelper: schema, seeding, CRUD, queries
  RecipeMatcher.java                Strict matching algorithm
  UnitConverter.java                Unit conversion (mass, volume, counted items)
  ExpiryHelper.java                 Expiry date parsing / expiring-soon logic
  IngredientAdapter.java, RecipeAdapter.java   RecyclerView adapters
  Ingredient.java, Recipe.java, RecipeIngredient.java   Models
```

Out of scope (per the brief): Google Maps / GPS, payments, Play Store publishing.
