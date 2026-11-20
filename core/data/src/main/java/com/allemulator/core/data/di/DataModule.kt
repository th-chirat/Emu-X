/*
 * DataModule.kt
 * -----------------------------------------------------------------------------
 * บอก Hilt (ระบบ Dependency Injection) ว่าจะสร้างฐานข้อมูลยังไง
 * คลาสไหนต้องการ GameDao แค่ใส่ไว้ใน constructor แล้ว Hilt จะส่งให้เอง
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.data.di

import android.content.Context
import androidx.room.Room
import com.allemulator.core.data.db.AppDatabase
import com.allemulator.core.data.db.GameDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "all_emulator.db").build()

    @Provides
    fun provideGameDao(db: AppDatabase): GameDao = db.gameDao()
}
