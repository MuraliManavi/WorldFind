package com.murali.worldfind.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "worldfind_prefs")

class PreferenceManager(private val context: Context) {

    companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val CART_ITEMS = stringPreferencesKey("cart_items")
        val WISHLIST_ITEMS = stringPreferencesKey("wishlist_items")
        val ADDRESSES = stringPreferencesKey("addresses")
        val ORDERS = stringPreferencesKey("orders")
    }

    val cartItemsJson: Flow<String?> = context.dataStore.data.map { it[CART_ITEMS] }
    val wishlistItemsJson: Flow<String?> = context.dataStore.data.map { it[WISHLIST_ITEMS] }
    val addressesJson: Flow<String?> = context.dataStore.data.map { it[ADDRESSES] }
    val ordersJson: Flow<String?> = context.dataStore.data.map { it[ORDERS] }

    suspend fun saveCartItems(json: String) {
        context.dataStore.edit { it[CART_ITEMS] = json }
    }

    suspend fun saveWishlistItems(json: String) {
        context.dataStore.edit { it[WISHLIST_ITEMS] = json }
    }

    suspend fun saveAddresses(json: String) {
        context.dataStore.edit { it[ADDRESSES] = json }
    }

    suspend fun saveOrders(json: String) {
        context.dataStore.edit { it[ORDERS] = json }
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ONBOARDING_COMPLETED] ?: false
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    val themeMode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_MODE] ?: "dark"
        }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    val isNotificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[NOTIFICATIONS_ENABLED] ?: true
        }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }
}
