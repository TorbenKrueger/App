package com.example.app.presentation.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app.R
import com.example.app.ServiceLocator
import com.example.app.domain.usecase.GetRecipesUseCase
import com.example.app.presentation.add.AddRecipeActivity
import com.example.app.presentation.detail.RecipeDetailActivity
import com.example.app.presentation.list.MainViewModel
import com.example.app.presentation.list.RecipeListAdapter

/** Fragment showing the list of recipes. */
class RecipesFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val useCase = GetRecipesUseCase(ServiceLocator.recipeRepository)
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(useCase) as T
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_recipes, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recipe_list)
        val adapter = RecipeListAdapter { recipe ->
            val intent = Intent(requireContext(), RecipeDetailActivity::class.java)
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        view.findViewById<Button>(R.id.add_recipe_button).setOnClickListener {
            startActivity(Intent(requireContext(), AddRecipeActivity::class.java))
        }
        view.findViewById<Button>(R.id.back_button).setOnClickListener { requireActivity().finish() }

        viewModel.recipes.observe(viewLifecycleOwner) { adapter.submitList(it) }
        viewModel.loadRecipes()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadRecipes()
    }
}
