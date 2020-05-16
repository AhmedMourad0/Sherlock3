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

- Improve UI, maybe wait for Compose? It's a mess. We also need many
  spinners.
- Testing.
- Firebase Crashlytics.
- Package per feature?
- Anonymous authentication as a firestore fallback strategy when the
  user is signed but not with Firebase.
- Use Monads to abstract over data types (RxJava, Coroutines,...) in
  data modules.
- Replace RxJava with Kotlin Coroutines and Flow.
- Firebase Cloud Functions.
- LiveQuery (allowing users to register their search parameters for live
  updates and sending them notifications when a child with high
  likelihood for a match is found).
- Timeline (shows a live feed of newly missing or found children near
  you)
- Go Multiplatform (hopefully Dagger2 will be ported by then, I need
  compile-time dependency graph validation).
- Redux? MVI?
