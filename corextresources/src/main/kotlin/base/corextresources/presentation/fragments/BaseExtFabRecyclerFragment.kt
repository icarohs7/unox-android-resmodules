package base.corextresources.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import base.corextresources.R
import base.corextresources.databinding.FragmentExtFabRecyclerBinding
import base.corextresources.presentation._baseclasses.BaseBindingFragment

abstract class BaseExtFabRecyclerFragment : BaseBindingFragment<FragmentExtFabRecyclerBinding>() {
    override fun onBindingCreated(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) {
        super.onBindingCreated(inflater, container, savedInstanceState)
        binding.stateView.hideStates()
    }

    fun displayState(stateTag: String) {
        binding.stateView.displayState(stateTag)
    }

    override fun getLayout(): Int {
        return R.layout.fragment_ext_fab_recycler
    }
}