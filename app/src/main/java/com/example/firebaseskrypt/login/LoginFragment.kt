package com.example.firebaseskrypt.login

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.firebaseskrypt.MainActivity
import com.example.firebaseskrypt.R
import com.example.firebaseskrypt.databinding.LoginFragmentBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginFragment : Fragment() {
    private lateinit var viewModel: LoginViewModel
    private lateinit var binding: LoginFragmentBinding
    private val fbAuth = FirebaseAuth.getInstance()
    private lateinit var signInClient: GoogleSignInClient
    private val TAG = "Login"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = LoginFragmentBinding.inflate(inflater, container, false)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        signInClient = GoogleSignIn.getClient(requireContext(), gso)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLoginOnClick()
    }

    private fun setupLoginOnClick() {
        with(binding){
            loginBtn.setOnClickListener{
                val email = binding.editTextTextLogin.text?.trim().toString()
                val password = binding.editTextTextPassword.text?.trim().toString()
                fbAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { authRes ->
                        if(authRes.user != null){
                            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            }
                            startActivity(intent)
                        }
                    }
                    .addOnFailureListener{ exc ->
                        Snackbar.make(requireView(), "Coś poszło nie tak...", Snackbar.LENGTH_SHORT).show()
                        Log.d("SING IN", exc.message.toString())
                    }
            }
            googleLoginBtn.setOnClickListener{
                val signInIntent = signInClient.signInIntent
                startActivityForResult(signInIntent, 9001)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 9001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct?.id)
        val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
        fbAuth.signInWithCredential(credential)
            .addOnSuccessListener(requireActivity()) {
                // If sign in succeeds the auth state listener will be notified and logic to
                // handle the signed in user can be handled in the listener.
                Log.d(TAG, "signInWithCredential:success")
                startActivity(Intent(requireActivity(), MainActivity::class.java))
//                requireActivity().finish()
            }
            .addOnFailureListener(requireActivity()) { e -> // If sign in fails, display a message to the user.
                Log.w(TAG, "signInWithCredential", e)
                Toast.makeText(
                    requireActivity(), "Authentication failed.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}