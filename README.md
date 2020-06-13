
Introduction
------------

TrackMySleepQuality is an app for recording sleep data. 
You can record a start and stop time for each night, assign a quality rating, and clear the database. 

## Purpose

In this codelab, working from [the starter app](https://github.com/google-developer-training/android-kotlin-fundamentals-starter-apps/tree/master/TrackMySleepQualityStates-Starter), I implemented the Room database that holds the sleep data. 
As a coding challenge, I then wrote instrumented tests to verify that this backend works. 
I followed the remaining steps to learn how to use Room databases in an Android app.

This app demonstrates the following applied techniques:

* Setting up and applying CRUD operations using a Room database and DAO
* Writing and running tests using JUnit4
* Multithreading and coroutines for database operations
* Using transformation maps to efficiently format or update views based on state changes
* Data Binding in layout files
* MVVM architecture using ViewModel, ViewModelFactory and Fragment for each screen
* Using Backing Properties to protect MutableLiveData
* Using observable LiveData variables to trigger navigation
* Navigating between fragments and using Safe Args (a Gradle plugin) to pass data between fragments.


Getting Started
---------------

1. Download and run the app.

License
-------

Copyright 2019 Google, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
