package com.example.app.presentation.home

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.app.R

/** Activity hosting the main screens using ViewPager2. */
class HomeActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        viewPager = findViewById(R.id.view_pager)
        viewPager.adapter = HomePagerAdapter(this)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateNavButtons(position)
            }
        })

        // start on Recipes page (index 1)
        setCurrentPage(1, false)

        findViewById<Button>(R.id.nav_recipes).setOnClickListener { setCurrentPage(1, true) }
        findViewById<Button>(R.id.nav_plan).setOnClickListener { setCurrentPage(0, true) }
        findViewById<Button>(R.id.nav_shopping).setOnClickListener { setCurrentPage(2, true) }
    }

    fun setCurrentPage(index: Int, smooth: Boolean = true) {
        viewPager.setCurrentItem(index, smooth)
        updateNavButtons(index)
    }

    private fun updateNavButtons(index: Int) {
        val recipes = findViewById<Button>(R.id.nav_recipes)
        val plan = findViewById<Button>(R.id.nav_plan)
        val shopping = findViewById<Button>(R.id.nav_shopping)
        recipes.isEnabled = index != 1
        plan.isEnabled = index != 0
        shopping.isEnabled = index != 2
    }
}
