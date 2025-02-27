package com.app.imagevideoactivity.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.app.imagevideoactivity.databinding.FragmentSignOutBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes


class SettingFragment : Fragment() {

    private var _binding: FragmentSignOutBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var googleSignInClient: GoogleSignInClient?= null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentSignOutBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signOut()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun signOut() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE)) // Request Google Drive access
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        googleSignInClient!!.signOut().addOnCompleteListener {
            Toast.makeText(requireContext(), "Logged out!", Toast.LENGTH_SHORT).show()
        }
    }



}