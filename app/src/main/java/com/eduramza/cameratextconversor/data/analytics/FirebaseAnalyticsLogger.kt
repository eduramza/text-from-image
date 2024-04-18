package com.eduramza.cameratextconversor.data.analytics

import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface FirebaseAnalyticsLogger {
    suspend fun trackScreenView(screenName: String, area: String)
    suspend fun trackSelectContent(id: String, itemName: String, contentType: String, area: String)
    suspend fun trackADImpression(id: String, itemName: String, contentType: String, area: String)
}

class FirebaseAnalyticsLoggerImpl(
    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics
) : FirebaseAnalyticsLogger {
    override suspend fun trackScreenView(screenName: String, area: String) =
        withContext(Dispatchers.IO) {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                param(ConstantsAnalytics.PARAM_AREA, area)
            }
        }

    override suspend fun trackSelectContent(
        id: String,
        itemName: String,
        contentType: String,
        area: String
    ) = withContext(Dispatchers.IO) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
            param(FirebaseAnalytics.Param.ITEM_ID, id)
            param(FirebaseAnalytics.Param.ITEM_NAME, itemName)
            param(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
            param(ConstantsAnalytics.PARAM_AREA, area)
        }
    }

    override suspend fun trackADImpression(
        id: String,
        itemName: String,
        contentType: String,
        area: String
    ) = withContext(Dispatchers.IO) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION) {
            param(FirebaseAnalytics.Param.ITEM_ID, id)
            param(FirebaseAnalytics.Param.ITEM_NAME, itemName)
            param(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
            param(ConstantsAnalytics.PARAM_AREA, area)
        }
    }
}