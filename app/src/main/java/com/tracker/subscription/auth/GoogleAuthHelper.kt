package com.tracker.subscription.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class GoogleAuthHelper(
    private val context: Context
) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val test = GoogleSignIn.getLastSignedInAccount(context)
    private val googleSignInClient: GoogleSignInClient by lazy {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1018938809495-94vl84bed6egqbbulmh42covjnkse5th.apps.googleusercontent.com")
            .requestEmail()
            .build()

        GoogleSignIn.getClient(context, options)
    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    suspend fun signInWithIntent(intent: Intent): FirebaseUser? {
        val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
        val account = task.getResult(ApiException::class.java)

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        val result = auth.signInWithCredential(credential).await()

        return result.user
    }

    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser
}