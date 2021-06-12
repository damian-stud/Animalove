package pl.devnowak.animalove.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import pl.devnowak.animalove.R

class AboutFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.fragment_about, rootKey)

    }

}