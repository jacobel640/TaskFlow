package com.jel.taskflow.di

import android.app.Application
import androidx.credentials.CredentialManager
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.jel.taskflow.R
import com.jel.taskflow.auth.data.repository.AuthRepositoryImpl
import com.jel.taskflow.auth.domain.repository.AuthRepository
import com.jel.taskflow.auth.domain.use_case.AuthUseCases
import com.jel.taskflow.auth.domain.use_case.DeleteAccount
import com.jel.taskflow.auth.domain.use_case.GetCurrentUser
import com.jel.taskflow.auth.domain.use_case.SignInWithGoogle
import com.jel.taskflow.auth.domain.use_case.SignOut
import com.jel.taskflow.core.data.AppDatabase
import com.jel.taskflow.tasks.data.TaskDao
import com.jel.taskflow.tasks.data.remote.TaskRemoteDataSource
import com.jel.taskflow.tasks.data.repository.TaskRepositoryImpl
import com.jel.taskflow.tasks.data.sync.TaskSyncManager
import com.jel.taskflow.tasks.domain.repository.TaskRepository
import com.jel.taskflow.tasks.domain.repository.UserPreferencesRepository
import com.jel.taskflow.tasks.domain.use_case.DeleteTask
import com.jel.taskflow.tasks.domain.use_case.GetFilteredTasks
import com.jel.taskflow.tasks.domain.use_case.GetTask
import com.jel.taskflow.tasks.domain.use_case.GetTasksCount
import com.jel.taskflow.tasks.domain.use_case.InsertTask
import com.jel.taskflow.tasks.domain.use_case.UpdateTask
import com.jel.taskflow.tasks.domain.use_case.TaskUseCases
import com.jel.taskflow.tasks.framework.workers.SyncScheduler
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
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_4_5)
            .build()

    @Provides
    @Singleton
    fun provideTaskDao(appDatabase: AppDatabase): TaskDao = appDatabase.taskDao

    @Provides
    @Singleton
    fun provideTaskRepository(
        app: Application,
        taskDao: TaskDao,
        firebaseAuth: FirebaseAuth
    ): TaskRepository =
        TaskRepositoryImpl(taskDao) {
            SyncScheduler.requestSync(app, requireNetwork = firebaseAuth.currentUser != null)
        }

    @Provides
    @Singleton
    fun provideTaskUseCases(repository: TaskRepository): TaskUseCases =
        TaskUseCases(
            getFilteredTasks = GetFilteredTasks(repository),
            getTask = GetTask(repository),
            insertTask = InsertTask(repository),
            updateTask = UpdateTask(repository),
            deleteTask = DeleteTask(repository),
            getTasksCount = GetTasksCount(repository)
        )

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore =
        FirebaseFirestore.getInstance().apply {
            // Room already is the offline cache, no need for a second persistent copy
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                .build()
        }

    @Provides
    @Singleton
    fun provideCredentialManager(app: Application): CredentialManager = CredentialManager.create(app)

    @Provides
    @Singleton
    fun provideTaskRemoteDataSource(firestore: FirebaseFirestore): TaskRemoteDataSource =
        TaskRemoteDataSource(firestore)

    @Provides
    @Singleton
    fun provideTaskSyncManager(
        taskDao: TaskDao,
        remoteDataSource: TaskRemoteDataSource,
        preferencesRepository: UserPreferencesRepository
    ): TaskSyncManager = TaskSyncManager(taskDao, remoteDataSource, preferencesRepository)

    @Provides
    @Singleton
    fun provideAuthRepository(
        app: Application,
        firebaseAuth: FirebaseAuth,
        credentialManager: CredentialManager,
        taskDao: TaskDao,
        remoteDataSource: TaskRemoteDataSource,
        preferencesRepository: UserPreferencesRepository
    ): AuthRepository =
        AuthRepositoryImpl(
            appContext = app,
            firebaseAuth = firebaseAuth,
            credentialManager = credentialManager,
            webClientId = app.getString(R.string.default_web_client_id),
            taskDao = taskDao,
            remoteDataSource = remoteDataSource,
            preferencesRepository = preferencesRepository
        )

    @Provides
    @Singleton
    fun provideAuthUseCases(repository: AuthRepository): AuthUseCases =
        AuthUseCases(
            getCurrentUser = GetCurrentUser(repository),
            signInWithGoogle = SignInWithGoogle(repository),
            signOut = SignOut(repository),
            deleteAccount = DeleteAccount(repository)
        )
}
