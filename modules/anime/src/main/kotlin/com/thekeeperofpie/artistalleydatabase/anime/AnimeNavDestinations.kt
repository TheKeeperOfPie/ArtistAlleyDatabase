package com.thekeeperofpie.artistalleydatabase.anime

enum class AnimeNavDestinations(val id: String) {

    HOME("anime_home"),
    SEARCH("anime_search"),
    SEARCH_MEDIA("anime_search_media"),
    USER("anime_user"),
    USER_LIST("anime_user_list"),
    MEDIA_DETAILS("anime_media_details"),
    CHARACTER_DETAILS("anime_character_details"),
    STAFF_DETAILS("anime_staff_details"),
    IGNORED("anime_ignored"),
    AIRING_SCHEDULE("anime_airing_schedule"),
    SEASONAL("anime_seasonal"),
    NEWS("anime_news"),
    ACTIVITY("anime_activity"),
    NOTIFICATIONS("anime_notifications"),
    MEDIA_CHARACTERS("anime_media_characters"),
    MEDIA_REVIEWS("anime_media_reviews"),
    REVIEW_DETAILS("anime_review_details"),
    MEDIA_RECOMMENDATIONS("anime_media_recommendations"),
    MEDIA_ACTIVITIES("anime_media_activities"),
    CHARACTER_MEDIAS("anime_character_medias"),
    STAFF_CHARACTERS("anime_staff_characters"),
    STUDIO_MEDIAS("anime_studio_medias"),
    ACTIVITY_DETAILS("anime_activity_details"),
    FEATURE_TIERS("anime_feature_tiers"),

    FORUM("anime_forum"),
    FORUM_SEARCH("anime_forum_search"),
    FORUM_THREAD("anime_forum_thread"),
    FORUM_THREAD_COMMENT("anime_forum_thread_comment"),

    MEDIA_HISTORY("anime_media_history"),

    REVIEWS("anime_reviews"),
    RECOMMENDATIONS("anime_recommendations"),

    USER_FOLLOWING("anime_user_following"),
    USER_FOLLOWERS("anime_user_followers"),
    USER_FAVORITE_MEDIA("anime_user_favorite_media"),
    USER_FAVORITE_CHARACTERS("anime_user_favorite_characters"),
    USER_FAVORITE_STAFF("anime_user_favorite_staff"),
    USER_FAVORITE_STUDIOS("anime_user_favorite_studios"),
}
