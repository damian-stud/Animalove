package pl.devnowak.animalove.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import pl.devnowak.animalove.R
import pl.devnowak.animalove.databinding.FragmentAppointmentBinding


class AppointmentFragment : Fragment() {

    private var _binding: FragmentAppointmentBinding? = null
    private val binding get() = _binding!!
    lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentAppointmentBinding.inflate(inflater, container, false)
        val view = binding.root
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            findNavController().navigate(R.id.logInBeforeFragment)
        }

        (requireActivity() as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        (requireActivity() as AppCompatActivity).supportActionBar!!.setHomeButtonEnabled(false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout = binding.appointmentFragmentTabLayout
        val viewPager2 = binding.chatPager
        val fragmentManager = childFragmentManager
        val adapter = ViewPagerAdapter(fragmentManager, lifecycle)

        viewPager2.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            if (position == 0) {
                tab.setIcon(R.drawable.ic_baseline_email_24)
                tab.text = "Umów wizytę"
            }
            if (position == 1) {
                tab.setIcon(R.drawable.ic_baseline_people_alt_24)
                tab.text = "Wszystkie wizyty"
            }
        }.attach()

        tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager2.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })


    }

    class ViewPagerAdapter(
        fm: FragmentManager, lifecycle: Lifecycle
    ) : FragmentStateAdapter(fm, lifecycle) {

        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            when (position) {
                1 -> return AllAppointmentsFragment()
            }
            return CreateAppointmentFragment()
        }
    }


}