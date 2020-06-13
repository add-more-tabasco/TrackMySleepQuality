/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    // allows you to cancel coroutines started by this view model when it is no longer used
    private var viewModelJob = Job()

    // typically, use Dispatchers.Main for coroutines started in the viewModel;
    // coroutines in the uiScope will run on the main thread
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var tonight = MutableLiveData<SleepNight?>()

    private var nights = database.getAllNights()

    val nightsString = Transformations.map(nights) {nights ->
        formatNights(nights, application.resources) //defined in Util.kt
    }

    // Start button should be enabled when tonight is null
    val startButtonVisible = Transformations.map(tonight) {
        it == null
    }
    // Stop button enabled when tonight is not null
    val stopButtonVisible = Transformations.map(tonight) {
        it != null
    }
    // clear button enabled when nights exist
    val clearButtonVisible = Transformations.map(nights) {
        it?.isNotEmpty()
    }

    // encapsulated event for snackbar display:
    private var _showSnackbarEvent = MutableLiveData<Boolean>()
    val showSnackBarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    init {
        initializeTonight()
    }

    //LiveData that changes when you want the app to navigate to a different destination
    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()
    val navigateToSleepQuality: LiveData<SleepNight>
        get() = _navigateToSleepQuality
    // reset the navigation variable
    fun doneNavigating() {
        _navigateToSleepQuality.value = null
    }

    private fun initializeTonight() {
        //coroutine step 1:
        //Launch a coroutine that runs on the main or UI thread, because the result affects the UI.
        uiScope.launch {
            //step 2:
            //Call a suspend function to do the long-running work, so that you don't block the UI thread while waiting for the result
            tonight.value = getTonightFromDatabase()
        }
    }
    //step 3:
    //Switch to the I/O context, so that the work can run in a thread pool that's optimized and set aside for these kinds of operations
    private suspend fun getTonightFromDatabase(): SleepNight? {
        return withContext(Dispatchers.IO) {
            // step 4: call the database function to do the work.
            var night = database.getTonight()
            // If start and end times differ, the night has already been completed, so return null. Otherwise, return the night
            if (night?.endTimeMilli != night?.startTimeMilli) {
                night = null
            }
            night
        }
    }

    //same 4 steps for click handlers that trigger db ops:

    //click handler for start button
    fun onStartTracking() {
        uiScope.launch {
            val newNight = SleepNight()
            insert(newNight)// not the Dao insert method; defined below:
            tonight.value = getTonightFromDatabase()
        }
    }
    private suspend fun insert(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.insert(night)
        }
    }

    //click handler for stop button
    fun onStopTracking() {
        uiScope.launch {
            val oldNight = tonight.value ?: return@launch
            oldNight.endTimeMilli = System.currentTimeMillis()
            update(oldNight)
            //trigger navigation to sleep quality screen for this night
            _navigateToSleepQuality.value = oldNight
        }
    }
    private suspend fun update(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.update(night)
        }
    }

    //click handler for clear button
    fun onClear() {
        uiScope.launch {
            clear()
            tonight.value = null
            _showSnackbarEvent.value = true
        }
    }
    suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()
        }
    }

    // snackbar event completion:
    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = false
    }

    // when viewModel is destroyed, jobs are cancelled
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}

