package com.moveoftoday.walkorwait.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.moveoftoday.walkorwait.PreferenceManager
import com.moveoftoday.walkorwait.UserDataRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun providePreferenceManager(
        @ApplicationContext context: Context
    ): PreferenceManager {
        return PreferenceManager(context)
    }

    @Provides
    @Singleton
    fun provideUserDataRepository(
        @ApplicationContext context: Context,
        auth: FirebaseAuth
    ): UserDataRepository {
        return UserDataRepository(context, auth)
    }
}
