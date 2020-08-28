# Sherlock

## Project Overview

*This app is my playground where I implement what I consider to be the
best possible design choices for a modern application.*

Sherlock is a mobile app that aims to help families find their
lost/kidnapped children.

## Key Concepts

- Clean Architecture.
- MVVM.
- Modularization.
- Functional Programming using Arrow-kt.
- Repository Pattern.
- OOP.
- Functional Reactive Programming.
- Dagger2.
- AAC (Room, Navigation, ViewModel, SavedState, LiveData).
- AndroidX Fragments.
- Firebase (Firestore, Storage, Authentication).
- Authentication with (Email, Google, Facebook and Twitter).
- RxJava.
- KotlinX Serialization.
- Glide.
- Fragment navigation pattern (Single Activity, multiple fragments).
- Reactive Event Bus.
- ViewBinding.
- Services.
- Backdrop.
- Timber.
- Home screen widgets.
- ADT driven error handling.
- Bundlizer and NoCopy.

## Whiteboard:

- LiveQuery (allowing users to register their search parameters for live
  updates and sending them notifications when a child with high
  likelihood for a match is found).
- Firebase Cloud Functions.
- The id should never be exposed to the ui layer
- Encrypt everything
- Refactor all views and viewmodels, remove all logic from the former to the latter
- Firebase Crashlytics?
  
- unsafeOf
- RxBinding
- Use Monads to abstract over data types (RxJava, Coroutines,...) in
  data modules.
- Replace RxJava with Kotlin Coroutines and Flow.
- Improve UI, maybe wait for Compose? It's a mess. We also need many
  spinners.
- Testing.
- Package per feature?
- Use AndroidX's App Startup
- Anonymous authentication as a firestore fallback strategy when the
  user is signed in but not with Firebase.
- Timeline (shows a live feed of newly missing or found children near
  you)
- Go Multiplatform (kotlin-inject, sqldelight, gitlive firebase,...).
- Redux? MVI? No?
