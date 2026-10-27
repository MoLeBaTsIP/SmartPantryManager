==============================================================================
 SMART PANTRY MANAGER
 Mobile App Development 700 (Richfield) - Practical Assignment
==============================================================================

Student name   : [your name]
Student number : [your student number]
GitHub link    : [your repository URL]
Package        : SmartPantry_updated_project.zip


------------------------------------------------------------------------------
 1. WHAT THE APP DOES
------------------------------------------------------------------------------

Smart Pantry Manager is a Java Android app that helps reduce food waste.
The user records the ingredients they already have at home (their "pantry")
and the app suggests only the recipes they can cook RIGHT NOW. A recipe is
shown only when every ingredient it needs is in the pantry, in a large
enough quantity. No shopping trip is required.

Example: with 2 eggs, 1 tomato and 5 ml of oil in the pantry, the app
suggests "Tomato Egg Scramble".


------------------------------------------------------------------------------
 2. FEATURES
------------------------------------------------------------------------------

* Pantry List
    All ingredients are shown in a RecyclerView (custom adapter), loaded
    from SQLite. A card at the top shows how many recipes can be cooked
    right now and opens the recipe list when tapped.

* Add / Edit Ingredient
    Name, quantity, unit (drop-down) and an optional expiry date (date
    picker). Validation messages appear under each field. The name field
    suggests the ingredient names the recipes use, and the unit is
    pre-selected from the name (egg = piece, milk = ml, garlic = clove,
    bread = slice). After saving, a message says how many recipes can now
    be cooked. Ingredients can be deleted from the list or the edit screen.

* Suggested Recipes
    Runs the strict matching rule and shows only recipes that can be made.
    A helpful message is shown when nothing qualifies.

* Recipe Detail
    Full ingredient list, numbered method, and a status line confirming
    the pantry still covers the recipe.

* Settings (SharedPreferences)
    - Highlight items that are expiring soon or expired
    - Ask before deleting an ingredient

* Navigation
    Toolbar menu (My Pantry / Suggested Recipes / Settings), Up arrow, and
    explicit Intents that pass record IDs between screens.


------------------------------------------------------------------------------
 3. HOW RECIPE MATCHING WORKS
------------------------------------------------------------------------------

The rule lives in RecipeMatcher.java:

  A recipe is suggested ONLY when EVERY required ingredient is in the
  pantry in at least the required quantity. A missing ingredient or too
  small a quantity excludes the recipe. There are no "almost there" results.

To cope with real-world input, the matcher:

  1. Normalises names: "Tomatoes" = "tomato", "EGGS" = "egg".
  2. Ignores describing words: "Olive oil" = "oil", "Whole milk" = "milk",
     "Red onion" = "onion". (Different foods stay different: "Peanut
     butter" is NOT "butter", "Cheddar cheese" is NOT "cheese".)
  3. Converts units before comparing: 1 kg satisfies 500 g, 1 l satisfies
     200 ml, 1 cup is 240 ml.
  4. Adds up several pantry rows of the same ingredient.
  5. Compares common counted items by weight, so the unit does not have to
     match exactly. Approximate weights used:
        egg 50 g, tomato 120 g, onion 110 g, banana 120 g, apple 180 g,
        potato 170 g, carrot 70 g, garlic bulb 40 g, garlic clove 5 g,
        bread slice 30 g.
     Example: 150 g of eggs covers "2 eggs".
  6. Treats water as always available.
  7. Fails safely: mass, volume and counted units that cannot be compared
     (for example 5 pieces of flour against 50 g of flour) are never
     treated as interchangeable, and unknown units exclude the recipe.

Supported units: g, kg, oz, lb, ml, l, tsp, tbsp, cup, piece, slice, clove.

Tip: enter eggs, tomatoes, onions, bananas and apples in "piece", oil and
milk in "ml" or "l", and bread in "slice". The app pre-selects these for you.


------------------------------------------------------------------------------
 4. DATABASE (SQLite / SQLiteOpenHelper)
------------------------------------------------------------------------------

SQLite is built into Android, needs no server or internet connection, and
keeps the data on the device after the app is closed.

  Table               Columns
  ------------------  -------------------------------------------------------
  ingredients         id (PK), name, quantity, unit, expiry_date (optional)
  recipes             id (PK), name (unique), instructions
  recipe_ingredients  id (PK), recipe_id (FK -> recipes.id, ON DELETE CASCADE),
                      ingredient_name, quantity, unit

20 recipes are created inside onCreate(), which runs only the first time the
database file is created, so recipes are never duplicated.


------------------------------------------------------------------------------
 5. REQUIREMENTS
------------------------------------------------------------------------------

* Android Studio Hedgehog (2023.1.1) or newer
* Android SDK Platform 34 (installed through Tools > SDK Manager)
* Android Gradle Plugin 8.2.2, Gradle 8.2 (downloaded automatically)
* Minimum Android version: API 24 (Android 7.0)
* Internet connection for the first Gradle sync


------------------------------------------------------------------------------
 6. HOW TO RUN THE APP
------------------------------------------------------------------------------

 1. Unzip SmartPantry_updated_project.zip.
 2. In Android Studio choose File > Open and select the "SmartPantry" folder.
 3. Wait for Gradle sync to finish.
      If you see "SDK location not found", open File > Project Structure >
      SDK Location (or install the SDK from Tools > SDK Manager). Android
      Studio then creates local.properties with your own SDK path. Do not
      commit local.properties.
 4. Run Build > Clean Project, then Build > Rebuild Project.
 5. Create or start an emulator (Tools > Device Manager, API 24 or higher),
    or connect a phone with USB debugging on.
 6. Press Run (green triangle). The app opens on the Pantry List.

Quick test: add Egg (2 piece), Tomato (1 piece) and Oil (5 ml). The home
card should say "1 recipe ready to cook" and "Tomato Egg Scramble" appears
under Recipes.


------------------------------------------------------------------------------
 7. HOW TO RUN THE UNIT TESTS
------------------------------------------------------------------------------

In the Project view open:
    app/src/test/java/com/example/smartpantry/
Right-click RecipeMatcherTest (or the whole folder) and choose Run Tests.

The tests cover: missing ingredients, insufficient quantities, name
normalisation, unit conversion, unsafe unit combinations, water, describing
words, eggs compared by weight, and the automatic unit suggestion.
ExpiryHelperTest covers the expiry date logic.


------------------------------------------------------------------------------
 8. PROJECT STRUCTURE
------------------------------------------------------------------------------

app/src/main/java/com/example/smartpantry/
  MainActivity.java                 Pantry List (launcher screen)
  AddEditIngredientActivity.java    Add / Edit Ingredient, validation
  SuggestedRecipesActivity.java     Suggested Recipes (strict matches only)
  RecipeDetailActivity.java         Recipe Detail
  SettingsActivity.java             Settings (SharedPreferences)
  BaseActivity.java                 Shared toolbar menu and navigation
  DatabaseHelper.java               SQLiteOpenHelper: schema, seeding, CRUD
  RecipeMatcher.java                Strict matching algorithm
  IngredientKnowledge.java          Describing words, item weights, water,
                                    unit suggestions
  UnitConverter.java                Unit conversion (mass, volume, counted)
  ExpiryHelper.java                 Expiry date parsing / expiring-soon logic
  IngredientAdapter.java            RecyclerView adapter for the pantry
  RecipeAdapter.java                RecyclerView adapter for recipes
  Ingredient.java, Recipe.java, RecipeIngredient.java   Data models

app/src/main/res/
  layout/    Screen and list-item layouts
  values/    Colours, strings, themes
  drawable/  Logo, icons (vector drawables) and small background shapes
  mipmap-*/  Launcher icon (adaptive XML + WebP for older phones)

app/src/test/java/com/example/smartpantry/
  RecipeMatcherTest.java, ExpiryHelperTest.java


------------------------------------------------------------------------------
 9. WHAT CHANGED IN THIS UPDATED VERSION
------------------------------------------------------------------------------

Recipe suggestions
  - Eggs, tomatoes and similar items entered in grams or kilograms now
    match recipes that list them in pieces.
  - "Olive oil", "Whole milk" and similar names match the plain ingredient.
  - Water no longer has to be added to the pantry.
  - The unit is pre-selected from the ingredient name, and the name field
    suggests the names the recipes use.
  - After saving, the app says how many recipes can be cooked, and the home
    screen shows a "recipes ready" card.

Design
  - One green accent on white and soft grey; red is used only for expiry
    warnings and the delete icon.
  - New app logo (a pantry jar with a leaf) as an adaptive launcher icon,
    with a themed (monochrome) version for Android 13+ and WebP versions for
    older phones. The logo also appears in the Pantry screen header.
  - Icons for Pantry (jar), Recipes (fork and knife) and Settings (gear) in
    the toolbar, on the buttons and in the screen headers.
  - Ingredient rows show a round letter badge, a quantity label, and Edit
    (pencil) and Delete (bin) icon buttons instead of text buttons.
  - Recipe rows have a recipe icon and an arrow; the "recipes ready" card on
    the home screen uses a soft green gradient.
  - The ingredient name field is larger (taller box, bigger text) so longer
    names are easy to read, and the drop-down lists use larger text.
  - Rounded cards with thin borders, outlined input fields and simple
    buttons on every screen.

The database structure did not change, so existing saved data is kept.


------------------------------------------------------------------------------
 10. TROUBLESHOOTING
------------------------------------------------------------------------------

Problem: "I added ingredients but no recipe appears."
  - Check that every ingredient of a recipe is in the pantry in enough
    quantity (Tomato Egg Scramble needs 2 eggs, 1 tomato, 5 ml oil).
  - Check the units. Oil and milk need a volume unit (ml, l, tsp, tbsp,
    cup), not grams. Eggs should be "piece".
  - Run Build > Clean Project and run the app again.
  - If the app was installed from an older version, uninstall it from the
    emulator or phone (or choose Clear storage) and run it again.

Problem: "SDK location not found"
  - See step 3 in section 6.

Problem: Gradle sync fails
  - Check your internet connection and use File > Sync Project with
    Gradle Files.


------------------------------------------------------------------------------
 11. SCOPE
------------------------------------------------------------------------------

Out of scope (per the assignment brief): Google Maps / GPS, payments and
Play Store publishing.

==============================================================================
