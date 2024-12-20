package com.dicoding.view.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.R
import com.dicoding.databinding.ActivityMainBinding
import com.dicoding.view.StoryAdapterPaging
import com.dicoding.view.ViewModelFactory
import com.dicoding.view.login.LoginActivity
import com.dicoding.view.maps.MapsActivity
import com.dicoding.view.upload.UploadActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding

    private val storyAdapter = StoryAdapterPaging()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRv()
        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {

                setupView()

                setupData()
            }
        }


    }

    private fun setupView() {

        storyAdapter.addLoadStateListener { loadState ->
            val isLoading = loadState.refresh is LoadState.Loading
            binding.progrssBar.visibility =  if(isLoading) View.VISIBLE else View.GONE

        }
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

    }

    private fun setupRv() {

        binding.recyclerView.apply {
            adapter = storyAdapter
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)

        }

    }

    private fun setupData() {
        lifecycleScope.launch {
            viewModel.getStoriesPager.observe(this@MainActivity) { resultStories ->
                storyAdapter.submitData(lifecycle, resultStories)
                binding.recyclerView.scrollToPosition(0)

            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.recyclerView.scrollToPosition(0)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                viewModel.logout()
                true
            }

            R.id.action_maps -> {
                startActivity(Intent(this, MapsActivity::class.java))
                true
            }

            R.id.upload -> {
                startActivity(Intent(this, UploadActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


}