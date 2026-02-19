package com.jel.taskflow.di

import android.app.Application
import androidx.room.Room
import com.jel.taskflow.core.data.AppDatabase
import com.jel.taskflow.tasks.data.repository.TaskRepositoryImpl
import com.jel.taskflow.tasks.domain.repository.TaskRepository
import com.jel.taskflow.tasks.domain.use_case.DeleteTask
import com.jel.taskflow.tasks.domain.use_case.GetFilteredTasks
import com.jel.taskflow.tasks.domain.use_case.GetTask
import com.jel.taskflow.tasks.domain.use_case.GetTasksCount
import com.jel.taskflow.tasks.domain.use_case.InsertTask
import com.jel.taskflow.tasks.domain.use_case.TaskUseCases
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
    fun provideTaskDatabase(app: Application): AppDatabase =
        Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()

    @Provides
    @Singleton
    fun provideTaskRepository(appDatabase: AppDatabase): TaskRepository =
        TaskRepositoryImpl(appDatabase.taskDao)

    @Provides
    @Singleton
    fun provideTaskUseCases(repository: TaskRepository): TaskUseCases =
        TaskUseCases(
            getFilteredTasks = GetFilteredTasks(repository),
            getTask = GetTask(repository),
            insertTask = InsertTask(repository),
            deleteTask = DeleteTask(repository),
            getTasksCount = GetTasksCount(repository)
        )
}