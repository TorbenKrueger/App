package com.example.app.presentation.home

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

/** Adapter for the HomeActivity ViewPager. */
class HomePagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> MealPlanFragment()
        1 -> RecipesFragment()
        else -> ShoppingListFragment()
    }
}
