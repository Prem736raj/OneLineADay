package com.onelineaday.journal.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.onelineaday.journal.MainActivity
import com.onelineaday.journal.R
import com.onelineaday.journal.data.JournalDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Home screen widget for One Line A Day
 * Shows today's entry or a prompt to write
 */
class JournalWidget : AppWidgetProvider() {
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    override fun onEnabled(context: Context) {
        // Widget is added to home screen
    }
    
    override fun onDisabled(context: Context) {
        // Last widget instance removed
    }
    
    companion object {
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_journal)
            
            // Set click intent to open app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            
            // Load today's entry
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = JournalDatabase.getDatabase(context)
                    val entry = database.journalDao().getEntryByDate(LocalDate.now())
                    
                    CoroutineScope(Dispatchers.Main).launch {
                        if (entry != null) {
                            views.setTextViewText(R.id.widget_date, "Today")
                            views.setTextViewText(R.id.widget_mood, entry.mood.emoji)
                            views.setTextViewText(R.id.widget_content, entry.content)
                        } else {
                            val today = LocalDate.now().format(
                                DateTimeFormatter.ofPattern("EEEE, MMM d")
                            )
                            views.setTextViewText(R.id.widget_date, today)
                            views.setTextViewText(R.id.widget_mood, "✏️")
                            views.setTextViewText(R.id.widget_content, "Tap to write your line for today...")
                        }
                        
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetComponent = ComponentName(context, JournalWidget::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)
            
            for (widgetId in widgetIds) {
                updateWidget(context, appWidgetManager, widgetId)
            }
        }
    }
}
