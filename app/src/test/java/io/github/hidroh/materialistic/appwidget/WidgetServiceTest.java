/*
 * Copyright (c) 2016 Ha Duy Trung
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

package io.github.hidroh.materialistic.appwidget;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.widget.RemoteViewsService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.github.hidroh.materialistic.test.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import io.github.hidroh.materialistic.data.Item;
import io.github.hidroh.materialistic.data.ItemManager;
import io.github.hidroh.materialistic.data.TestHnItem;

import static junit.framework.Assert.assertNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressLint("NewApi")
@RunWith(RobolectricGradleTestRunner.class)
public class WidgetServiceTest {
    private RemoteViewsService.RemoteViewsFactory viewFactory;
    private ItemManager itemManager = mock(ItemManager.class);

    @Before
    public void setUp() {
        reset(itemManager);
        viewFactory = new WidgetService.ListRemoteViewsFactory(RuntimeEnvironment.application,
                itemManager, ItemManager.TOP_FETCH_MODE, false);
        viewFactory.onCreate();
    }

    @Test
    public void testGetViewFactory() {
        assertNotNull(new WidgetService().onGetViewFactory(new Intent()
                .putExtra(WidgetService.EXTRA_SECTION, ItemManager.BEST_FETCH_MODE)));
        assertNotNull(new WidgetService().onGetViewFactory(new Intent()
                .putExtra(WidgetService.EXTRA_SECTION, ItemManager.NEW_FETCH_MODE)));
    }

    @Test
    public void testAdapter() {
        when(itemManager.getStories(anyString(), anyInt())).thenReturn(new Item[]{new TestHnItem(1L)});
        when(itemManager.getItem(anyString(), anyInt())).thenReturn(new TestHnItem(1L) {
            @Override
            public String getDisplayedTitle() {
                return "title";
            }

            @Override
            public int getScore() {
                return 100;
            }
        });
        viewFactory.onDataSetChanged();
        verify(itemManager).getStories(anyString(), anyInt());
        assertThat(viewFactory.hasStableIds()).isTrue();
        assertThat(viewFactory.getCount()).isEqualTo(1);
        assertThat(viewFactory.getLoadingView()).isNotNull();
        assertThat(viewFactory.getViewTypeCount()).isEqualTo(1);
        assertThat(viewFactory.getItemId(0)).isEqualTo(1L);
        assertThat(viewFactory.getViewAt(0)).isNotNull();
        verify(itemManager).getItem(eq("1"), anyInt());
    }

    @Test
    public void testEmpty() {
        viewFactory.onDataSetChanged();
        verify(itemManager).getStories(anyString(), anyInt());
        assertThat(viewFactory.getCount()).isEqualTo(0);
        assertThat(viewFactory.getItemId(0)).isEqualTo(0L);
        assertThat(viewFactory.getViewAt(0)).isNotNull();
        verify(itemManager, never()).getItem(anyString(), anyInt());
    }

    @After
    public void tearDown() {
        viewFactory.onDestroy();
    }
}
