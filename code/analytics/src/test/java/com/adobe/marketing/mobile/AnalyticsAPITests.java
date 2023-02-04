/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.adobe.marketing.mobile.analytics.internal.AnalyticsExtension;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AnalyticsAPITests {

    @SuppressWarnings("rawtypes")
    @Test
    public void testRegisterExtension() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            // mock MobileCore.registerExtension()
            ArgumentCaptor<Class> extensionClassCaptor = ArgumentCaptor.forClass(Class.class);
            ArgumentCaptor<ExtensionErrorCallback> callbackCaptor =
                    ArgumentCaptor.forClass(ExtensionErrorCallback.class);
            mobileCoreMockedStatic
                    .when(
                            () ->
                                    MobileCore.registerExtension(
                                            extensionClassCaptor.capture(),
                                            callbackCaptor.capture()))
                    .thenReturn(true);
            // call registerExtension() API
            Analytics.registerExtension();
            // verify: happy
            assertNotNull(callbackCaptor.getValue());
            assertEquals(AnalyticsExtension.class, extensionClassCaptor.getValue());
            // verify: not exception when error callback was called
            callbackCaptor.getValue().error(ExtensionError.UNEXPECTED_ERROR);
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testRegisterExtensionWithoutError() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            // mock MobileCore.registerExtension()
            ArgumentCaptor<Class> extensionClassCaptor = ArgumentCaptor.forClass(Class.class);
            ArgumentCaptor<ExtensionErrorCallback> callbackCaptor =
                    ArgumentCaptor.forClass(ExtensionErrorCallback.class);
            mobileCoreMockedStatic
                    .when(
                            () ->
                                    MobileCore.registerExtension(
                                            extensionClassCaptor.capture(),
                                            callbackCaptor.capture()))
                    .thenReturn(true);
            // call registerExtension() API
            Analytics.registerExtension();
            // verify: happy
            assertNotNull(callbackCaptor.getValue());
            assertEquals(AnalyticsExtension.class, extensionClassCaptor.getValue());
            // verify: not exception when error callback was called
            callbackCaptor.getValue().error(null);
        }
    }

    @Test
    public void testClearQueue() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            Analytics.clearQueue();
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            mobileCoreMockedStatic.verify(() -> MobileCore.dispatchEvent(eventCaptor.capture()));

            Event event = eventCaptor.getValue();
            assertNotNull(event);
            assertEquals("ClearHitsQueue", event.getName());
            assertEquals("com.adobe.eventType.analytics", event.getType());
            assertEquals("com.adobe.eventSource.requestContent", event.getSource());
            assertEquals(
                    new HashMap<String, Object>() {
                        {
                            put("clearhitsqueue", true);
                        }
                    },
                    event.getEventData());
        }
    }

    @Test
    public void testSendQueuedHits() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            Analytics.sendQueuedHits();
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            mobileCoreMockedStatic.verify(() -> MobileCore.dispatchEvent(eventCaptor.capture()));

            Event event = eventCaptor.getValue();
            assertNotNull(event);
            assertEquals("ForceKickHits", event.getName());
            assertEquals("com.adobe.eventType.analytics", event.getType());
            assertEquals("com.adobe.eventSource.requestContent", event.getSource());
            assertEquals(
                    new HashMap<String, Object>() {
                        {
                            put("forcekick", true);
                        }
                    },
                    event.getEventData());
        }
    }

    @Test
    public void testGetQueueSizeWithNullCallback() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            Analytics.getQueueSize(null);
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            mobileCoreMockedStatic.verify(
                    () -> MobileCore.dispatchEvent(eventCaptor.capture()), never());
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testGetQueueSize() throws InterruptedException {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            Analytics.getQueueSize(
                    new AdobeCallbackWithError<Long>() {
                        @Override
                        public void fail(AdobeError adobeError) {}

                        @Override
                        public void call(Long size) {
                            assertTrue(5 == size);
                            countDownLatch.countDown();
                        }
                    });
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            ArgumentCaptor<AdobeCallbackWithError> callbackCaptor =
                    ArgumentCaptor.forClass(AdobeCallbackWithError.class);
            mobileCoreMockedStatic.verify(
                    () ->
                            MobileCore.dispatchEventWithResponseCallback(
                                    eventCaptor.capture(), anyLong(), callbackCaptor.capture()),
                    times(1));
            AdobeCallbackWithError callback = callbackCaptor.getValue();
            callback.call(
                    new Event.Builder("", "", "")
                            .setEventData(Collections.singletonMap("queuesize", 5))
                            .build());
            countDownLatch.await();
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testGetQueueSizeWithIncorrectResponse() throws InterruptedException {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            Analytics.getQueueSize(
                    new AdobeCallbackWithError<Long>() {
                        @Override
                        public void fail(AdobeError adobeError) {}

                        @Override
                        public void call(Long size) {
                            assertTrue(0 == size);
                            countDownLatch.countDown();
                        }
                    });
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            ArgumentCaptor<AdobeCallbackWithError> callbackCaptor =
                    ArgumentCaptor.forClass(AdobeCallbackWithError.class);
            mobileCoreMockedStatic.verify(
                    () ->
                            MobileCore.dispatchEventWithResponseCallback(
                                    eventCaptor.capture(), anyLong(), callbackCaptor.capture()),
                    times(1));
            AdobeCallbackWithError callback = callbackCaptor.getValue();
            callback.call(new Event.Builder("", "", "").build());
            countDownLatch.await();
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testGetQueueSizeWithTimeoutError() throws InterruptedException {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            Analytics.getQueueSize(
                    new AdobeCallbackWithError<Long>() {
                        @Override
                        public void fail(AdobeError adobeError) {
                            assertEquals(AdobeError.CALLBACK_TIMEOUT, adobeError);
                            countDownLatch.countDown();
                        }

                        @Override
                        public void call(Long size) {}
                    });
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            ArgumentCaptor<AdobeCallbackWithError> callbackCaptor =
                    ArgumentCaptor.forClass(AdobeCallbackWithError.class);
            mobileCoreMockedStatic.verify(
                    () ->
                            MobileCore.dispatchEventWithResponseCallback(
                                    eventCaptor.capture(), anyLong(), callbackCaptor.capture()),
                    times(1));
            AdobeCallbackWithError callback = callbackCaptor.getValue();
            callback.fail(AdobeError.CALLBACK_TIMEOUT);
            countDownLatch.await();
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testGetTrackingIdentifier() throws InterruptedException {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            Analytics.getTrackingIdentifier(
                    new AdobeCallbackWithError<String>() {
                        @Override
                        public void fail(AdobeError adobeError) {}

                        @Override
                        public void call(String s) {
                            assertEquals("aid_1", s);
                            countDownLatch.countDown();
                        }
                    });
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            ArgumentCaptor<AdobeCallbackWithError> callbackCaptor =
                    ArgumentCaptor.forClass(AdobeCallbackWithError.class);
            mobileCoreMockedStatic.verify(
                    () ->
                            MobileCore.dispatchEventWithResponseCallback(
                                    eventCaptor.capture(), anyLong(), callbackCaptor.capture()),
                    times(1));
            AdobeCallbackWithError callback = callbackCaptor.getValue();
            callback.call(
                    new Event.Builder("", "", "")
                            .setEventData(Collections.singletonMap("aid", "aid_1"))
                            .build());
            countDownLatch.await();
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testGetTrackingIdentifierWithBadResponse() throws InterruptedException {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            Analytics.getTrackingIdentifier(
                    new AdobeCallbackWithError<String>() {
                        @Override
                        public void fail(AdobeError adobeError) {}

                        @Override
                        public void call(String s) {
                            assertNull(s);
                            countDownLatch.countDown();
                        }
                    });
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            ArgumentCaptor<AdobeCallbackWithError> callbackCaptor =
                    ArgumentCaptor.forClass(AdobeCallbackWithError.class);
            mobileCoreMockedStatic.verify(
                    () ->
                            MobileCore.dispatchEventWithResponseCallback(
                                    eventCaptor.capture(), anyLong(), callbackCaptor.capture()),
                    times(1));
            AdobeCallbackWithError callback = callbackCaptor.getValue();
            callback.call(
                    new Event.Builder("", "", "")
                            .setEventData(Collections.singletonMap("x", "aid_1"))
                            .build());
            countDownLatch.await();
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testGetTrackingIdentifierReturnError() throws InterruptedException {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            Analytics.getTrackingIdentifier(
                    new AdobeCallbackWithError<String>() {
                        @Override
                        public void fail(AdobeError adobeError) {
                            assertEquals(AdobeError.UNEXPECTED_ERROR, adobeError);
                            countDownLatch.countDown();
                        }

                        @Override
                        public void call(String s) {}
                    });
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            ArgumentCaptor<AdobeCallbackWithError> callbackCaptor =
                    ArgumentCaptor.forClass(AdobeCallbackWithError.class);
            mobileCoreMockedStatic.verify(
                    () ->
                            MobileCore.dispatchEventWithResponseCallback(
                                    eventCaptor.capture(), anyLong(), callbackCaptor.capture()),
                    times(1));
            AdobeCallbackWithError callback = callbackCaptor.getValue();
            callback.fail(AdobeError.UNEXPECTED_ERROR);
            countDownLatch.await();
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testGetVisitorIdentifier() throws InterruptedException {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            Analytics.getVisitorIdentifier(
                    new AdobeCallbackWithError<String>() {
                        @Override
                        public void fail(AdobeError adobeError) {}

                        @Override
                        public void call(String s) {
                            assertEquals("vid_1", s);
                            countDownLatch.countDown();
                        }
                    });
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            ArgumentCaptor<AdobeCallbackWithError> callbackCaptor =
                    ArgumentCaptor.forClass(AdobeCallbackWithError.class);
            mobileCoreMockedStatic.verify(
                    () ->
                            MobileCore.dispatchEventWithResponseCallback(
                                    eventCaptor.capture(), anyLong(), callbackCaptor.capture()),
                    times(1));
            AdobeCallbackWithError callback = callbackCaptor.getValue();
            callback.call(
                    new Event.Builder("", "", "")
                            .setEventData(Collections.singletonMap("vid", "vid_1"))
                            .build());
            countDownLatch.await();
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testGetVisitorIdentifierWithBadResponse() throws InterruptedException {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            Analytics.getVisitorIdentifier(
                    new AdobeCallbackWithError<String>() {
                        @Override
                        public void fail(AdobeError adobeError) {}

                        @Override
                        public void call(String s) {
                            assertNull(s);
                            countDownLatch.countDown();
                        }
                    });
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            ArgumentCaptor<AdobeCallbackWithError> callbackCaptor =
                    ArgumentCaptor.forClass(AdobeCallbackWithError.class);
            mobileCoreMockedStatic.verify(
                    () ->
                            MobileCore.dispatchEventWithResponseCallback(
                                    eventCaptor.capture(), anyLong(), callbackCaptor.capture()),
                    times(1));
            AdobeCallbackWithError callback = callbackCaptor.getValue();
            callback.call(
                    new Event.Builder("", "", "")
                            .setEventData(Collections.singletonMap("x", "vid_1"))
                            .build());
            countDownLatch.await();
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testGetVisitorIdentifierReturnError() throws InterruptedException {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            Analytics.getVisitorIdentifier(
                    new AdobeCallbackWithError<String>() {
                        @Override
                        public void fail(AdobeError adobeError) {
                            assertEquals(AdobeError.UNEXPECTED_ERROR, adobeError);
                            countDownLatch.countDown();
                        }

                        @Override
                        public void call(String s) {}
                    });
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            ArgumentCaptor<AdobeCallbackWithError> callbackCaptor =
                    ArgumentCaptor.forClass(AdobeCallbackWithError.class);
            mobileCoreMockedStatic.verify(
                    () ->
                            MobileCore.dispatchEventWithResponseCallback(
                                    eventCaptor.capture(), anyLong(), callbackCaptor.capture()),
                    times(1));
            AdobeCallbackWithError callback = callbackCaptor.getValue();
            callback.fail(AdobeError.UNEXPECTED_ERROR);
            countDownLatch.await();
        }
    }

    @Test
    public void testSetVisitorIdentifier() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            Analytics.setVisitorIdentifier("vid_1");
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            mobileCoreMockedStatic.verify(() -> MobileCore.dispatchEvent(eventCaptor.capture()));

            Event event = eventCaptor.getValue();
            assertNotNull(event);
            assertEquals("UpdateVisitorIdentifier", event.getName());
            assertEquals("com.adobe.eventType.analytics", event.getType());
            assertEquals("com.adobe.eventSource.requestIdentity", event.getSource());
            assertEquals(
                    new HashMap<String, Object>() {
                        {
                            put("vid", "vid_1");
                        }
                    },
                    event.getEventData());
        }
    }

    @Test
    public void callbackIsNullForPublicAPIs() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            Analytics.getVisitorIdentifier(null);
            Analytics.getTrackingIdentifier(null);
            Analytics.getQueueSize(null);
            mobileCoreMockedStatic.verify(
                    () -> MobileCore.dispatchEventWithResponseCallback(any(), anyLong(), any()),
                    never());
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void callbackWithoutErrorHandling() throws InterruptedException {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            CountDownLatch countDownLatch = new CountDownLatch(3);
            Analytics.getVisitorIdentifier(s -> countDownLatch.countDown());
            Analytics.getTrackingIdentifier(s -> countDownLatch.countDown());
            Analytics.getQueueSize(s -> countDownLatch.countDown());
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            ArgumentCaptor<AdobeCallbackWithError> callbackCaptor =
                    ArgumentCaptor.forClass(AdobeCallbackWithError.class);
            mobileCoreMockedStatic.verify(
                    () ->
                            MobileCore.dispatchEventWithResponseCallback(
                                    eventCaptor.capture(), anyLong(), callbackCaptor.capture()),
                    times(3));
            AdobeCallbackWithError callback = callbackCaptor.getValue();
            callback.fail(AdobeError.UNEXPECTED_ERROR);
            assertFalse(countDownLatch.await(500, TimeUnit.MILLISECONDS));
        }
    }
}
