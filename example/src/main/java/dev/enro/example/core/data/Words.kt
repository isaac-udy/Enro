package dev.enro.example.core.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface Word : Parcelable

@JvmInline
@Parcelize
value class Adverb(val value: String) : Word {
    override fun toString(): String {
        return value
    }
}

@JvmInline
@Parcelize
value class Adjective(val value: String) : Word {
    override fun toString(): String {
        return value
    }
}

@JvmInline
@Parcelize
value class Noun(val value: String) : Word {
    override fun toString(): String {
        return value
    }
}

val Word.typeName: String
    get() = when(this) {
        is Adjective -> "Adjective"
        is Adverb -> "Adverb"
        is Noun -> "Noun"
    }

object Words {
    val adverbs = listOf(
        "Abnormally",
        "Absentmindedly",
        "Accidentally",
        "Actually",
        "Adventurously",
        "Afterwards",
        "Almost",
        "Always",
        "Annually",
        "Anxiously",
        "Arrogantly",
        "Awkwardly",
        "Bashfully",
        "Beautifully",
        "Bitterly",
        "Bleakly",
        "Blindly",
        "Blissfully",
        "Boastfully",
        "Boldly",
        "Bravely",
        "Briefly",
        "Brightly",
        "Briskly",
        "Broadly",
        "Busily",
        "Calmly",
        "Carefully",
        "Carelessly",
        "Cautiously",
        "Certainly",
        "Cheerfully",
        "Clearly",
        "Cleverly",
        "Closely",
        "Coaxingly",
        "Colorfully",
        "Commonly",
        "Continually",
        "Coolly",
        "Correctly",
        "Courageously",
        "Crossly",
        "Cruelly",
        "Curiously",
        "Daily",
        "Daintily",
        "Dearly",
        "Deceivingly",
        "Deeply",
        "Defiantly",
        "Deliberately",
        "Delightfully",
        "Diligently",
        "Dimly",
        "Doubtfully",
        "Dreamily",
        "Easily",
        "Elegantly",
        "Energetically",
        "Enormously",
        "Enthusiastically",
        "Equally",
        "Especially",
        "Even",
        "Evenly",
        "Eventually",
        "Exactly",
        "Excitedly",
        "Extremely",
        "Fairly",
        "Faithfully",
        "Famously",
        "Far",
        "Fast",
        "Fatally",
        "Ferociously",
        "Fervently",
        "Fiercely",
        "Fondly",
        "Foolishly",
        "Fortunately",
        "Frankly",
        "Frantically",
        "Freely",
        "Frenetically",
        "Frightfully",
        "Fully",
        "Furiously",
        "Generally",
        "Generously",
        "Gently",
        "Gladly",
        "Gleefully",
        "Gracefully",
        "Gratefully",
        "Greatly",
        "Greedily",
        "Happily",
        "Hastily",
        "Healthily",
        "Heavily",
        "Helpfully",
        "Helplessly",
        "Highly",
        "Honestly",
        "Hopelessly",
        "Hourly",
        "Hungrily",
        "Immediately",
        "Innocently",
        "Inquisitively",
        "Instantly",
        "Intensely",
        "Intently",
        "Interestingly",
        "Inwardly",
        "Irritably",
        "Jaggedly",
        "Jealously",
        "Jovially",
        "Joyfully",
        "Joyously",
        "Jubilantly",
        "Judgmentally",
        "Justly",
        "Keenly",
        "Kiddingly",
        "Kindheartedly",
        "Kindly",
        "Knavishly",
        "Knowingly",
        "Knowledgeably",
        "Kookily",
        "Lazily",
        "Les",
        "Lightly",
        "Likely",
        "Limply",
        "Lively",
        "Loftily",
        "Longingly",
        "Loosely",
        "Loudly",
        "Lovingly",
        "Loyally",
        "Madly",
        "Majestically",
        "Meaningfully",
        "Mechanically",
        "Merrily",
        "Miserably",
        "Mockingly",
        "Monthly",
        "More",
        "Mortally",
        "Mostly",
        "Mysteriously",
        "Naturally",
        "Hopelessly",
        "Hourly",
        "Hungrily",
        "Immediately",
        "Innocently",
        "Inquisitively",
        "Instantly",
        "Intensely",
        "Intently",
        "Interestingly",
        "Inwardly",
        "Irritably",
        "Jaggedly",
        "Jealously",
        "Jovially",
        "Joyfully",
        "Joyously",
        "Jubilantly",
        "Judgmentally",
        "Justly",
        "Keenly",
        "Kiddingly",
        "Kindheartedly",
        "Kindly",
        "Knavishly",
        "Knowingly",
        "Knowledgeably",
        "Kookily",
        "Lazily",
        "Less",
        "Lightly",
        "Likely",
        "Limply",
        "Lively",
        "Loftily",
        "Longingly",
        "Loosely",
        "Loudly",
        "Lovingly",
        "Loyally",
        "Madly",
        "Majestically",
        "Meaningfully",
        "Mechanically",
        "Merrily",
        "Miserably",
        "Mockingly",
        "Monthly",
        "More",
        "Mortally",
        "Mostly",
        "Mysteriously",
        "Naturally",
        "Nearly",
        "Neatly",
        "Nervously",
        "Never",
        "Nicely",
        "Noisily",
        "Not",
        "Obediently",
        "Obnoxiously",
        "Oddly",
        "Offensively",
        "Officially",
        "Often",
        "Only",
        "Openly",
        "Optimistically",
        "Overconfidently",
        "Painfully",
        "Partially",
        "Patiently",
        "Perfectly",
        "Physically",
        "Playfully",
        "Politely",
        "Poorly",
        "Positively",
        "Potentially",
        "Powerfully",
        "Promptly",
        "Properly",
        "Punctually",
        "Quaintly",
        "Queasily",
        "Queerly",
        "Questionably",
        "Quicker",
        "Quickly",
        "Quietly",
        "Quirkily",
        "Quizzically",
        "Randomly",
        "Rapidly",
        "Rarely",
        "Readily",
        "Really",
        "Reassuringly",
        "Recklessly",
        "Regularly",
        "Reluctantly",
        "Repeatedly",
        "Reproachfully",
        "Restfully",
        "Righteously",
        "Rightfully",
        "Rigidly",
        "Roughly",
        "Rudely",
        "Safely",
        "Scarcely",
        "Scarily",
        "Searchingly",
        "Sedately",
        "Seemingly",
        "Seldom",
        "Selfishly",
        "Separately",
        "Seriously",
        "Shakily",
        "Sharply",
        "Sheepishly",
        "Shrilly",
        "Shyly",
        "Silently",
        "Sleepily",
        "Slowly",
        "Smoothly",
        "Softly",
        "Solemnly",
        "Solidly",
        "Sometimes",
        "Soon",
        "Speedily",
        "Stealthily",
        "Sternly",
        "Strictly",
        "Successfully",
        "Suddenly",
        "Supposedly",
        "Surprisingly",
        "Suspiciously",
        "Sweetly",
        "Swiftly",
        "Sympathetically",
        "Tenderly",
        "Tensely",
        "Terribly",
        "Thankfully",
        "Thoroughly",
        "Thoughtfully",
        "Tightly",
        "Tomorrow",
        "Too",
        "Tremendously",
        "Triumphantly",
        "Truly",
        "Truthfully",
        "Rightfully",
        "Scarcely",
        "Searchingly",
        "Sedately",
        "Seemingly",
        "Selfishly",
        "Separately",
        "Seriously",
        "Sheepishly",
        "Smoothly",
        "Solemnly",
        "Sometimes",
        "Speedily",
        "Stealthily",
        "Successfully",
        "Suddenly",
        "Supposedly",
        "Surprisingly",
        "Suspiciously",
        "Sympathetically",
        "Tenderly",
        "Thankfully",
        "Thoroughly",
        "Thoughtfully",
        "Tomorrow",
        "Tremendously",
        "Triumphantly",
        "Truthfully",
        "Ultimately",
        "Unabashedly",
        "Unaccountably",
        "Unbearably",
        "Unethically",
        "Unexpectedly",
        "Unfortunately",
        "Unimpressively",
        "Unnaturally",
        "Unnecessarily",
        "Upbeat",
        "Upright",
        "Upside down",
        "Upward",
        "Urgently",
        "Usefully",
        "Uselessly",
        "Usually",
        "Utterly",
        "Vacantly",
        "Vaguely",
        "Vainly",
        "Valiantly",
        "Vastly",
        "Verbally",
        "Very",
        "Viciously",
        "Victoriously",
        "Violently",
        "Vivaciously",
        "Voluntarily",
        "Warmly",
        "Weakly",
        "Wearily",
        "Well",
        "Wetly",
        "Wholly",
        "Wildly",
        "Willfully",
        "Wisely",
        "Woefully",
        "Wonderfully",
        "Worriedly",
        "Wrongly",
        "Yawningly",
        "Yearly",
        "Yearningly",
        "Yesterday",
        "Yieldingly",
        "Youthfully",
        "Zealously",
        "Zestfully",
        "Zestily",
    ).sorted()
        .map { Adverb(it) }

    val adjectives = listOf(
        "Abrupt",
        "Acidic",
        "Adorable",
        "Amiable",
        "Amused",
        "Appalling",
        "Appetizing",
        "Average",
        "Batty",
        "Blushing",
        "Bored",
        "Brave",
        "Bright",
        "Broad",
        "Bulky",
        "Burly",
        "Charming",
        "Cheeky",
        "Cheerful",
        "Chubby",
        "Clean",
        "Clear",
        "Cloudy",
        "Clueless",
        "Clumsy",
        "Creepy",
        "Crooked",
        "Cruel",
        "Cumbersome",
        "Curved",
        "Cynical",
        "Dangerous",
        "Dashing",
        "Decayed",
        "Deceitful",
        "Deep",
        "Defeated",
        "Defiant",
        "Delicious",
        "Disturbed",
        "Dizzy",
        "Drab",
        "Drained",
        "Dull",
        "Eager",
        "Ecstatic",
        "Elated",
        "Elegant",
        "Emaciated",
        "Embarrassed",
        "Enchanting",
        "Energetic",
        "Enormous",
        "Extensive",
        "Exuberant",
        "Fancy",
        "Fantastic",
        "Fierce",
        "Filthy",
        "Flat",
        "Floppy",
        "Fluttering",
        "Foolish",
        "Frantic",
        "Fresh",
        "Friendly",
        "Frightened",
        "Frothy",
        "Funny",
        "Fuzzy",
        "Gaudy",
        "Gentle",
        "Ghastly",
        "Giddy",
        "Gigantic",
        "Glamorous",
        "Gleaming",
        "Glorious",
        "Gorgeous",
        "Graceful",
        "Greasy",
        "Grieving",
        "Gritty",
        "Grotesque",
        "Grubby",
        "Grumpy",
        "Handsome",
        "Happy",
        "Healthy",
        "Helpful",
        "Helpless",
        "High",
        "Hollow",
        "Homely",
        "Horrific",
        "Huge",
        "Hungry",
        "Hurt",
        "Icy",
        "Ideal",
        "Irritable",
        "Itchy",
        "Jealous",
        "Jittery",
        "Jolly",
        "Icy",
        "Ideal",
        "Intrigued",
        "Irate",
        "Irritable",
        "Itchy",
        "Jealous",
        "Jittery",
        "Jolly",
        "Joyous",
        "Juicy",
        "Jumpy",
        "Kind",
        "Lethal",
        "Little",
        "Lively",
        "Livid",
        "Lonely",
        "Lovely",
        "Lucky",
        "Ludicrous",
        "Macho",
        "Narrow",
        "Nasty",
        "Naughty",
        "Nervous",
        "Nutty",
        "Perfect",
        "Perplexed",
        "Petite",
        "Petty",
        "Plain",
        "Pleasant",
        "Poised",
        "Pompous",
        "Precious",
        "Prickly",
        "Proud",
        "Pungent",
        "Puny",
        "Quaint",
        "Reassured",
        "Relieved",
        "Repulsive",
        "Responsive",
        "Ripe",
        "Robust",
        "Rotten",
        "Rotund",
        "Rough",
        "Round",
        "Salty",
        "Sarcastic",
        "Scant",
        "Scary",
        "Scattered",
        "Scrawny",
        "Selfish",
        "Shaggy",
        "Shaky",
        "Shallow",
        "Sharp",
        "Shiny",
        "Short",
        "Silky",
        "Silly",
        "Skinny",
        "Slimy",
        "Slippery",
        "Small",
        "Sweet",
        "Tart",
        "Tasty",
        "Teeny",
        "Tender",
        "Tense",
        "Terrible",
        "Testy",
        "Thankful",
        "Thick",
        "Tight",
        "Timely",
        "Tricky",
        "Trite",
        "Uneven",
        "Upset",
        "Uptight",
        "Vast",
        "Vexed",
        "Vivid",
        "Wacky",
        "Weary",
        "Zany",
        "Zealous",
        "Zippy"
    ).sorted()
        .map { Adjective(it) }

    val nouns = listOf(
        "Actor",
        "Gold",
        "Painting",
        "Advertisement",
        "Grass",
        "Parrot",
        "Afternoon",
        "Greece",
        "Pencil",
        "Airport",
        "Guitar",
        "Piano",
        "Ambulance",
        "Hair",
        "Pillow",
        "Animal",
        "Hamburger",
        "Pizza",
        "Answer",
        "Helicopter",
        "Planet",
        "Apple",
        "Helmet",
        "Plastic",
        "Army",
        "Holiday",
        "Portugal",
        "Australia",
        "Honey",
        "Potato",
        "Balloon",
        "Horse",
        "Queen",
        "Banana",
        "Hospital",
        "Quill",
        "Battery",
        "House",
        "Rain",
        "Beach",
        "Hydrogen",
        "Rainbow",
        "Beard",
        "Ice",
        "Raincoat",
        "Bed",
        "Insect",
        "Refrigerator",
        "Belgium",
        "Insurance",
        "Restaurant",
        "Boy",
        "Iron",
        "River",
        "Branch",
        "Island",
        "Rocket",
        "Breakfast",
        "Jackal",
        "Room",
        "Brother",
        "Jelly",
        "Rose",
        "Camera",
        "Jewellery",
        "Russia",
        "Candle",
        "Jordan",
        "Sandwich",
        "Car",
        "Juice",
        "School",
        "Caravan",
        "Kangaroo",
        "Scooter",
        "Carpet",
        "King",
        "Shampoo",
        "Cartoon",
        "Kitchen",
        "Shoe",
        "China",
        "Kite",
        "Soccer",
        "Church",
        "Knife",
        "Spoon",
        "Crayon",
        "Lamp",
        "Stone",
        "Crowd",
        "Lawyer",
        "Sugar",
        "Daughter",
        "Leather",
        "Sweden",
        "Death",
        "Library",
        "Teacher",
        "Denmark",
        "Lighter",
        "Telephone",
        "Diamond",
        "Lion",
        "Television",
        "Dinner",
        "Lizard",
        "Tent",
        "Disease",
        "Lock",
        "Thailand",
        "Doctor",
        "London",
        "Tomato",
        "Dog",
        "Lunch",
        "Toothbrush",
        "Dream",
        "Machine",
        "Traffic",
        "Dress",
        "Magazine",
        "Train",
        "Easter",
        "Magician",
        "Truck",
        "Egg",
        "Manchester",
        "Uganda",
        "Eggplant",
        "Market",
        "Umbrella",
        "Egypt",
        "Match",
        "Van",
        "Elephant",
        "Microphone",
        "Vase",
        "Energy",
        "Monkey",
        "Vegetable",
        "Engine",
        "Morning",
        "Vulture",
        "England",
        "Motorcycle",
        "Wall",
        "Evening",
        "Nail",
        "Whale",
        "Eye",
        "Napkin",
        "Window",
        "Family",
        "Needle",
        "Wire",
        "Finland",
        "Nest",
        "Xylophone",
        "Fish",
        "Nigeria",
        "Yacht",
        "Flag",
        "Night",
        "Yak",
        "Flower",
        "Notebook",
        "Zebra",
        "Football",
        "Ocean",
        "Zoo",
        "Forest",
        "Oil",
        "Garden",
        "Fountain",
        "Orange",
        "Gas",
        "France",
        "Oxygen",
        "Girl",
        "Furniture",
        "Oyster",
        "Glass",
        "Garage",
    ).sorted()
        .map { Noun(it) }

    val all = (adverbs + nouns + adjectives)
        .sortedBy { it.toString() }
}

