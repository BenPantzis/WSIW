---
name: add-image-loading
description: Load remote images using the RemoteImage composable from core-ui, backed by Coil.
metadata:
  author: com.template.android
  last-updated: '2026-06-01'
  keywords: [Coil, image loading, AsyncImage, RemoteImage, Compose]
---

## Overview

`RemoteImage` in `:core:core-ui` wraps Coil's `AsyncImage`. Use it in any composable — no additional setup required since Coil is already a dependency of `core-ui`.

## Usage

```kotlin
RemoteImage(
    url = item.imageUrl,
    contentDescription = item.title,
    modifier = Modifier
        .fillMaxWidth()
        .height(200.dp),
    contentScale = ContentScale.Crop,
)
```

## Adding a placeholder or error image

Pass a `model` object with `ImageRequest` for advanced options:

```kotlin
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(item.imageUrl)
        .crossfade(true)
        .build(),
    contentDescription = item.title,
    placeholder = painterResource(R.drawable.ic_placeholder),
    error = painterResource(R.drawable.ic_error),
    modifier = modifier,
)
```

Import `coil.compose.AsyncImage` and `coil.request.ImageRequest` directly when you need options beyond what `RemoteImage` exposes.

## Dependency

Coil is declared in `gradle/libs.versions.toml` as `coil-compose` and is added to `:core:core-ui` — no extra dependency needed in feature modules.
