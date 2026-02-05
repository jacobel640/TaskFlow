package com.jel.taskflow.di

import android.app.Application
import androidx.room.Room
import com.jel.taskflow.tasks.data.TaskDatabase
import com.jel.taskflow.tasks.data.repository.TaskRepositoryImpl
import com.jel.taskflow.tasks.repository.TaskRepository
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
    fun provideTaskDatabase(app: Application): TaskDatabase {
        return Room.databaseBuilder(
            app,
            TaskDatabase::class.java,
            TaskDatabase.DATABASE_NAME
        )
//            .addMigrations(TaskDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun provideTaskRepository(taskDatabase: TaskDatabase): TaskRepository {
        return TaskRepositoryImpl(taskDatabase.taskDao)
    }

    // provide use cases
}