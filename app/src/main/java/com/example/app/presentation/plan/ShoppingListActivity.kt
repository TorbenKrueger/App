package com.example.app.presentation.plan

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.app.R

class ShoppingListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_list)

        val text = intent.getStringExtra(EXTRA_LIST) ?: ""
        findViewById<TextView>(R.id.list_text).text = text
    }

    companion object {
        const val EXTRA_LIST = "list"
    }
}
