package com.example.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object Localizations {
    private val _selectedLanguage = MutableStateFlow("en")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    fun setLanguage(langCode: String) {
        if (langCode in listOf("en", "hi", "te")) {
            _selectedLanguage.value = langCode
        }
    }

    private val translations = mapOf(
        "en" to mapOf(
            "app_title" to "BANDHAM (बंधम)",
            "app_subtitle" to "Your Secure Private Family Vault",
            "vault_title" to "%s's Home",
            "nav_home" to "Home",
            "nav_tree" to "Family Tree",
            "nav_reminders" to "Reminders",
            "nav_ledger" to "Ledger",
            "nav_security" to "Security",
            "welcome" to "Namaste,",
            "home_desc" to "Family relationships, quick stats, reminders, and ledger in one private space.",
            "members_title" to "Family Members & Contacts",
            "add_member" to "Add Member",
            "search_at" to "Search relatives or relationships...",
            "upcoming_reminders" to "Upcoming Reminders",
            "birthdays" to "Birthdays",
            "anniversaries" to "Anniversaries",
            "pending_tx" to "Pending Items",
            "upcoming_occasions" to "Upcoming Occasions",
            "todays_reminders" to "Today's Reminders",
            "lent" to "Lent",
            "borrowed" to "Borrowed",
            "you_are_owed" to "You are Owed",
            "you_owe" to "You Owe",
            "members_count" to "Members",
            "empty_reminders" to "No upcoming reminders!",
            "empty_members" to "No family members found.",
            "empty_ledger" to "No transactions logged yet.",
            "relation" to "Relation",
            "father" to "Father",
            "mother" to "Mother",
            "husband" to "Husband",
            "wife" to "Wife",
            "son" to "Son",
            "daughter" to "Daughter",
            "brother" to "Brother",
            "sister" to "Sister",
            "grandfather" to "Grandfather",
            "grandmother" to "Grandmother",
            "grandchildren" to "Grandchildren",
            "quick_reminders" to "Quick Reminders Dashboard",
            "total_members" to "Total Members",
            "add_new_relative" to "Add New Relative",
            "security_settings" to "Security & Passcode",
            "change_pin" to "Change Lock Pin",
            "pin_desc" to "Access is restricted with active local military-grade device-level encryption PIN codes."
        ),
        "hi" to mapOf(
            "app_title" to "बंधम (BANDHAM)",
            "app_subtitle" to "आपका सुरक्षित निजी पारिवारिक होम",
            "vault_title" to "%s का घर",
            "nav_home" to "होम",
            "nav_tree" to "पारिवारिक पेड़",
            "nav_reminders" to "अनुस्मारक",
            "nav_ledger" to "बहीखाता",
            "nav_security" to "सुरक्षा",
            "welcome" to "नमस्ते,",
            "home_desc" to "एक ही निजी स्थान पर पारिवारिक रिश्ते, त्वरित आँकड़े, अनुस्मारक और बहीखाता।",
            "members_title" to "परिवार के सदस्य और संपर्क",
            "add_member" to "सदस्य जोड़ें",
            "search_at" to "रिश्तेदारों या रिश्तों को खोजें...",
            "upcoming_reminders" to "आगामी याद दिलाना",
            "birthdays" to "जन्मदिन",
            "anniversaries" to "वर्षगाँठ",
            "pending_tx" to "लंबित लेनदेन",
            "upcoming_occasions" to "आगामी अवसर",
            "todays_reminders" to "आज के अनुस्मारक",
            "lent" to "उधार दिया",
            "borrowed" to "उधार लिया",
            "you_are_owed" to "आपको मिलना है",
            "you_owe" to "आपको देना है",
            "members_count" to "सदस्य",
            "empty_reminders" to "कोई आगामी अनुस्मारक नहीं!",
            "empty_members" to "कोई पारिवारिक सदस्य नहीं मिला।",
            "empty_ledger" to "अभी तक कोई लेनदेन दर्ज नहीं किया गया है।",
            "relation" to "रिश्ता",
            "father" to "पिता",
            "mother" to "माता",
            "husband" to "पति",
            "wife" to "पत्नी",
            "son" to "बेटा",
            "daughter" to "बेटी",
            "brother" to "भाई",
            "sister" to "बहन",
            "grandfather" to "दादा/नाना",
            "grandmother" to "दादी/नानी",
            "grandchildren" to "पोते-पोतियां/नाती-नातिन",
            "quick_reminders" to "त्वरित अनुस्मारक डैशबोर्ड",
            "total_members" to "कुल सदस्य",
            "add_new_relative" to "नया रिश्तेदार जोड़ें",
            "security_settings" to "सुरक्षा और पासकोड",
            "change_pin" to "लॉक पिन बदलें",
            "pin_desc" to "पहुंच सक्रिय स्थानीय सैन्य-ग्रेड डिवाइस-स्तरीय एन्क्रिप्शन पिन कोड के साथ सीमित है।"
        ),
        "te" to mapOf(
            "app_title" to "బంధం (BANDHAM)",
            "app_subtitle" to "మీ సురక్షిత ప్రైవేట్ కుటుంబ నిధి",
            "vault_title" to "%s ఇల్లు",
            "nav_home" to "హోమ్",
            "nav_tree" to "कुटुంబ వృక్షం (వృక్షం)",
            "nav_reminders" to "రిమైండర్లు",
            "nav_ledger" to "లెడ్జర్",
            "nav_security" to "భద్రత",
            "welcome" to "నమస్తే,",
            "home_desc" to "ఒకే ప్రైవేట్ స్థలంలో కుటుంబ సంబంధాలు, శీఘ్ర గణాంకాలు, రిమైండర్‌లు మరియు లెడ్జర్.",
            "members_title" to "కుటుంబ సభ్యులు & పరిచయాలు",
            "add_member" to "సభ్యుడిని చేర్చు",
            "search_at" to "బంధువులు లేదా సంబంధాల కోసం వెతకండి...",
            "upcoming_reminders" to "రాబోయే గుర్తుచేసేవి",
            "birthdays" to "పుట్టినరోజులు",
            "anniversaries" to "వివాహ వార్షికోత్సవాలు",
            "pending_tx" to "పెండింగ్ లావాదేవీలు",
            "upcoming_occasions" to "రాబోయే సందర్భాలు",
            "todays_reminders" to "ఈరోజు రిమైండర్లు",
            "lent" to "ఇచ్చినవి",
            "borrowed" to "తీసుకున్నవి",
            "you_are_owed" to "మీకు రావలసినవి",
            "you_owe" to "మీరు ఇవ్వవలసినవి",
            "members_count" to "సభ్యులు",
            "empty_reminders" to "రాబోయే గుర్తుచేసేవి ఏవీ లేవు!",
            "empty_members" to "కుటుంబ సభ్యులు ఎవరూ కనుగొనబడలేదు.",
            "empty_ledger" to "ఇంకా లావాదేవీల వివరాలు ఏవీ లేవు.",
            "relation" to "సంబంధం",
            "father" to "తండ్రి",
            "mother" to "తల్లి",
            "husband" to "భర్త",
            "wife" to "భార్య",
            "son" to "కుమారుడు",
            "daughter" to "కుమార్తె",
            "brother" to "సహోదరుడు",
            "sister" to "సహోదరి",
            "grandfather" to "తాతయ్య",
            "grandmother" to "నానమ్మ/అమ్మమ్మ",
            "grandchildren" to "మనుమలు/మనుమరాళ్ళు",
            "quick_reminders" to "శీఘ్ర రిమైండర్ల డాష్‌బోర్డ్",
            "total_members" to "మొత్తం సభ్యులు",
            "add_new_relative" to "కొత్త బంధువును చేర్చు",
            "security_settings" to "భద్రత & పాస్‌కోడ్",
            "change_pin" to "పిన్ లాక్ మార్చండి",
            "pin_desc" to "యాక్సెస్ సక్రియ లోకల్ మిలిటరీ-గ్రేడ్ పరికర-స్థాయి ఎన్‌క్రిప్షన్ పిన్ కోడ్‌లతో పరిమితం చేయబడింది."
        )
    )

    fun string(key: String, vararg args: Any): String {
        val lang = _selectedLanguage.value
        val raw = translations[lang]?.get(key) ?: translations["en"]?.get(key) ?: key
        return try {
            if (args.isEmpty()) raw else String.format(raw, *args)
        } catch (e: Exception) {
            raw
        }
    }
}
