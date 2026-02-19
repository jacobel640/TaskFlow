package com.jel.taskflow.di

import android.app.Application
import androidx.room.Room
import com.jel.taskflow.core.data.AppDatabase
import com.jel.taskflow.tasks.data.repository.TaskRepositoryImpl
import com.jel.taskflow.tasks.domain.TaskRepository
import com.jel.taskflow.tasks.domain.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTaskDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
//            .addMigrations(TaskDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun provideTaskRepository(appDatabase: AppDatabase): TaskRepository {
        return TaskRepositoryImpl(appDatabase.taskDao)
    }

    // provide use cases
}