/*
 * Copyright (C) 2015 Pedro Vicente Gomez Sanchez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.pedrovgs.nox;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import com.github.pedrovgs.nox.imageloader.ImageLoader;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Observable;

/**
 * Processes NoxItem instances to download the images associated to a set of NoxItem instances
 * asynchronously. Notifies when a new bitmap is ready to be used to the class clients.
 *
 * @author Pedro Vicente Gomez Sanchez.
 */
class NoxItemCatalog extends Observable {

  private final List<NoxItem> noxItems;
  private final int noxItemSize;
  private final ImageLoader imageLoader;
  private final WeakReference<Bitmap>[] bitmaps;
  private final WeakReference<Drawable>[] placeholders;
  private final boolean[] loading;
  private ImageLoader.Listener[] listeners;
  private Drawable placeholder;

  NoxItemCatalog(List<NoxItem> noxItems, int noxItemSize, ImageLoader imageLoader) {
    validateNoxItems(noxItems);
    this.noxItems = noxItems;
    this.noxItemSize = noxItemSize;
    this.imageLoader = imageLoader;
    this.bitmaps = new WeakReference[noxItems.size()];
    this.placeholders = new WeakReference[noxItems.size()];
    this.loading = new boolean[noxItems.size()];
    this.listeners = new ImageLoader.Listener[noxItems.size()];
  }

  int size() {
    return noxItems.size();
  }

  boolean isBitmapReady(int position) {
    return bitmaps[position] != null && bitmaps[position].get() != null;
  }

  boolean isPlaceholderReady(int position) {
    return (placeholders[position] != null && placeholders[position].get() != null)
        || placeholder != null;
  }

  Bitmap getBitmap(int position) {
    return bitmaps[position] != null ? bitmaps[position].get() : null;
  }

  Drawable getPlaceholder(int position) {
    Drawable placeholder = null;
    if (placeholders[position] != null) {
      placeholder = placeholders[position].get();
    }
    if (placeholder == null) {
      placeholder = this.placeholder;
    }
    return placeholder;
  }

  void setPlaceholder(Drawable placeholder) {
    this.placeholder = placeholder;
  }

  void load(int position) {
    if (!isBitmapReady(position) && !isDownloading(position)) {
      loading[position] = true;
      NoxItem noxItem = noxItems.get(position);
      loadNoxItem(position, noxItem);
    }
  }

  void resume() {
    imageLoader.resume();
  }

  void pause() {
    imageLoader.pause();
  }

  void notifyNoxItemReady(int position) {
    setChanged();
    notifyObservers(position);
  }

  void setBitmap(int position, Bitmap image) {
    bitmaps[position] = new WeakReference<Bitmap>(image);
  }

  void setLoading(int position, boolean isLoading) {
    loading[position] = isLoading;
  }

  void setPlaceholder(int position, Drawable placeholder) {
    placeholders[position] = new WeakReference<Drawable>(placeholder);
  }

  private void loadNoxItem(final int position, NoxItem noxItem) {
    imageLoader.load(noxItem.getResourceId())
        .load(noxItem.getUrl())
        .withPlaceholder(noxItem.getPlaceholderId())
        .size(noxItemSize)
        .useCircularTransformation()
        .notify(getImageLoaderListener(position));
  }

  private ImageLoader.Listener getImageLoaderListener(final int position) {
    if (listeners[position] == null) {
      listeners[position] = new NoxItemCatalogImageLoaderListener(position, this);
    }
    return listeners[position];
  }

  private boolean isDownloading(int position) {
    return loading[position];
  }

  private void validateNoxItems(List<NoxItem> noxItems) {
    if (noxItems == null) {
      throw new NullPointerException("The list of NoxItem can't be null");
    }
  }
}
