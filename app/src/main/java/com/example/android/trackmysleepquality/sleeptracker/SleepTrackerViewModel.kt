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
import androidx.lifecycle.MutableLiveData
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
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

    init {
        initializeTonight()
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
            // call the database function to do the work.
            var night = database.getTonight()
            // If start and end times differ, the night has already been completed, so return null. Otherwise, return the night
            if (night?.endTimeMilli != night?.startTimeMilli) {
                night = null
            }
            night
        }
    }

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

    // when viewModel is destroyed, jobs are cancelled
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}

