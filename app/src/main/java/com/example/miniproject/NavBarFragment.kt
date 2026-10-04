package com.example.miniproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment

class NavBarFragment : Fragment() {

    private var currentTabIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.nav, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navHomeBtn = view.findViewById<LinearLayout>(R.id.nav_home_btn)
        val navHistoryBtn = view.findViewById<LinearLayout>(R.id.nav_history_btn)
        val navSettingsBtn = view.findViewById<LinearLayout>(R.id.nav_settings_btn)
        val fabAddTransaction = view.findViewById<View>(R.id.fab_add_transaction)

        navHomeBtn.setOnClickListener {
            swapScreen(HomeFragment(),0)
        }

        navHistoryBtn.setOnClickListener {
            swapScreen(HistoryFragment(),1)
        }

        navSettingsBtn.setOnClickListener {
            swapScreen(SettingsFragment(),2)
        }

        // 3. Keep your FAB working
        fabAddTransaction.setOnClickListener {
            val dialog = TransactionDialogFragment.newInstance()
            dialog.show(parentFragmentManager, "TransactionDialog")
        }
    }

    // This function does the heavy lifting of changing the screen
    private fun swapScreen(fragment: Fragment ,newTabIndex: Int) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.main_content_container, fragment) // Make sure this matches your activity_main.xml container ID!
            .commit()
        if (newTabIndex == currentTabIndex) return

        val transaction = requireActivity().supportFragmentManager.beginTransaction()

        // Apply directional animations
        if (newTabIndex > currentTabIndex) {
            // Moving Right -> Swipe Left
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
        } else {
            // Moving Left -> Swipe Right
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
        }
        transaction.replace(R.id.main_content_container, fragment)
        transaction.commit()

        // Update the tracker
        currentTabIndex = newTabIndex
    }
}