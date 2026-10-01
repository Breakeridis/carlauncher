package com.carlauncher.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.carlauncher.R
import com.carlauncher.data.AppEntry
import com.carlauncher.data.AppPrefs
import com.carlauncher.data.AppsRepository
import com.carlauncher.databinding.DialogAppDrawerBinding

/**
 * Full-screen application drawer: instant search, alphabetical grid,
 * tap to launch, long-press to pin to the dock. Dismiss via close button,
 * tap on the header or swipe down.
 */
class AppDrawerFragment : DialogFragment() {

    private var _binding: DialogAppDrawerBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: AppPrefs

    private var touchDownY = 0f
    private var touchDownTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_CarLauncher_Drawer)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogAppDrawerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = AppPrefs(requireContext().applicationContext)
        val repo = AppsRepository(requireContext())

        val adapter = AppDrawerAdapter(
            onItemClick = { entry ->
                repo.launchPackage(entry.packageName)
                dismiss()
            },
            onItemLongClick = { entry -> pinToDock(entry) },
        )
        binding.appsGrid.layoutManager = GridLayoutManager(requireContext(), 6)
        binding.appsGrid.adapter = adapter
        adapter.submit(repo.installedApps())

        binding.searchInput.doAfterTextChanged { adapter.filter(it?.toString().orEmpty()) }
        binding.closeButton.setOnClickListener { dismiss() }
        binding.drawerRoot.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    touchDownY = event.rawY
                    touchDownTime = System.currentTimeMillis()
                }
                MotionEvent.ACTION_UP -> {
                    val dy = touchDownY - event.rawY
                    val dt = System.currentTimeMillis() - touchDownTime
                    if (dy > 150f * resources.displayMetrics.density && dt < 300L) dismiss()
                }
            }
            false
        }
    }

    private fun pinToDock(entry: AppEntry) {
        val pinned = prefs.pinnedApps
        when {
            pinned.contains(entry.packageName) ->
                Toast.makeText(requireContext(), R.string.dock_already_pinned, Toast.LENGTH_SHORT).show()
            pinned.size >= MAX_DOCK_SLOTS ->
                Toast.makeText(requireContext(), R.string.dock_full, Toast.LENGTH_SHORT).show()
            else -> {
                prefs.pinnedApps = pinned + entry.packageName
                Toast.makeText(requireContext(), R.string.dock_pinned, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "app_drawer"
        private const val MAX_DOCK_SLOTS = 4
    }
}
