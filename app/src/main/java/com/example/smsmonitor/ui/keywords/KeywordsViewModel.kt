package com.example.smsmonitor.ui.keywords

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.smsmonitor.SmsMonitorApp
import com.example.smsmonitor.data.model.Keyword
import kotlinx.coroutines.launch

/**
 * 关键词管理 ViewModel
 */
class KeywordsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SmsMonitorApp

    val keywords: LiveData<List<Keyword>> = app.keywordRepository.allKeywords

    fun addKeyword(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            app.keywordRepository.insert(Keyword(text = text.trim()))
        }
    }

    fun deleteKeyword(keyword: Keyword) {
        viewModelScope.launch {
            app.keywordRepository.delete(keyword)
        }
    }

    fun toggleKeyword(keyword: Keyword) {
        viewModelScope.launch {
            app.keywordRepository.update(keyword.copy(isEnabled = !keyword.isEnabled))
        }
    }

    fun deleteAllKeywords() {
        viewModelScope.launch {
            app.keywordRepository.deleteAll()
        }
    }
}
