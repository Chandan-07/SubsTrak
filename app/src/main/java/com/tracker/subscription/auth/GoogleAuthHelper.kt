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
import com.tracker.subscription.data.AuthUser
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
            .requestProfile()
            .build()

        GoogleSignIn.getClient(context, options)
    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    suspend fun signInWithIntent(data: Intent): AuthUser {

        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        val account = task.getResult(ApiException::class.java)

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        val authResult = FirebaseAuth.getInstance()
            .signInWithCredential(credential)
            .await()

        val user = authResult.user!!

        return AuthUser(
            uid = user.uid,
            name = account.displayName, // 🔥 IMPORTANT
            email = account.email,
            photo = account.photoUrl?.toString()
        )
    }

    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser
}