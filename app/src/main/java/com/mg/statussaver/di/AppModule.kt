package com.mg.statussaver.di

import android.content.Context
import com.mg.statussaver.utils.PermissionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * App module for providing dependencies that don't require the interface/implementation pattern
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    /**
     * The StatusRepository dependency is now provided by RepositoryModule using @Binds
     * instead of @Provides since we're now using interface/implementation pattern
     */

    @Provides
    @Singleton
    fun providePermissionManager(
        @ApplicationContext context: Context
    ): PermissionManager {
        return PermissionManager(context)
    }
}
