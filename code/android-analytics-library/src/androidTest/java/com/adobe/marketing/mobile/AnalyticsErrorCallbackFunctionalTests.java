///* ************************************************************************
// * ADOBE CONFIDENTIAL
// * ___________________
// *
// * Copyright 2020 Adobe
// * All Rights Reserved.
// *
// * NOTICE: All information contained herein is, and remains
// * the property of Adobe and its suppliers, if any. The intellectual
// * and technical concepts contained herein are proprietary to Adobe
// * and its suppliers and are protected by all applicable intellectual
// * property laws, including trade secret and copyright laws.
// * Dissemination of this information or reproduction of this material
// * is strictly forbidden unless prior written permission is obtained
// * from Adobe.
// **************************************************************************/
//
//package com.adobe.marketing.mobile;
//
//import android.support.test.runner.AndroidJUnit4;
//
//import org.junit.After;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertNull;
//import static org.junit.Assert.assertTrue;
//
//@RunWith(AndroidJUnit4.class)
//public class AnalyticsErrorCallbackFunctionalTests extends AbstractE2ETest {
//	private static final int CALLBACK_TIMEOUT_MILLIS = 5000;
//	private static final int WAIT_TIMEOUT_MILLIS = 500;
//	private AsyncHelper asyncHelper = new AsyncHelper();
//
//	@Before
//	public void setUp() {
//		super.setUp();
//		MobileCore.setLogLevel(LoggingMode.VERBOSE);
//		MobileCore.setApplication(defaultApplication);
//		testableNetworkService.resetTestableNetworkService();
//	}
//
//	@After
//	public void tearDown() {
//		super.tearDown();
//		resetCore();
//		Identity.resetIdentityCore();
//	}
//
//	@Test
//	public void test_getQueueSize_whenAnalyticsExtensionNotRegistered_returnsCallbackError() throws
//		InterruptedException {
//		final AdobeError[] storedAdobeError = new AdobeError[1];
//		final CountDownLatch latch = new CountDownLatch(1);
//		final boolean[] wasCalled = new boolean[1];
//		AdobeCallbackWithError<Long> callback = new AdobeCallbackWithError<Long>() {
//			@Override
//			public void call(final Long data) {
//				wasCalled[0] = true;
//				Assert.fail("Success callback was invoked unexpectedly");
//			}
//
//			@Override
//			public void fail(final AdobeError adobeError) {
//				storedAdobeError[0] = adobeError;
//				latch.countDown();
//			}
//		};
//
//		// test
//		startCore();
//		Analytics.getQueueSize(callback);
//		int waitTimeout = WAIT_TIMEOUT_MILLIS;
//		boolean await = latch.await(waitTimeout, TimeUnit.MILLISECONDS);
//
//		// verify
//		assertTrue(String.format("Timed out waiting for getQueueSize callback after (%d) ms", waitTimeout), await);
//		assertFalse(wasCalled[0]);
//		assertEquals(AdobeError.EXTENSION_NOT_INITIALIZED, storedAdobeError[0]);
//	}
//
//	@Test
//	public void test_getTrackingIdentifier_whenAnalyticsExtensionNotRegistered_returnsCallbackError() throws
//		InterruptedException {
//		final AdobeError[] storedAdobeError = new AdobeError[1];
//		final CountDownLatch latch = new CountDownLatch(1);
//		final boolean[] wasCalled = new boolean[1];
//		AdobeCallbackWithError<String> callback = new AdobeCallbackWithError<String>() {
//			@Override
//			public void call(final String data) {
//				wasCalled[0] = true;
//				Assert.fail("Success callback was invoked unexpectedly");
//			}
//
//			@Override
//			public void fail(final AdobeError adobeError) {
//				storedAdobeError[0] = adobeError;
//				latch.countDown();
//			}
//		};
//
//		// test
//		startCore();
//		Analytics.getTrackingIdentifier(callback);
//		int waitTimeout = WAIT_TIMEOUT_MILLIS;
//		boolean await = latch.await(waitTimeout, TimeUnit.MILLISECONDS);
//
//		// verify
//		assertTrue(String.format("Timed out waiting for getTrackingIdentifier callback after (%d) ms", waitTimeout), await);
//		assertFalse(wasCalled[0]);
//		assertEquals(AdobeError.EXTENSION_NOT_INITIALIZED, storedAdobeError[0]);
//	}
//
//	@Test
//	public void test_getVisitorIdentifier_whenAnalyticsExtensionNotRegistered_returnsCallbackError() throws
//		InterruptedException {
//		final AdobeError[] storedAdobeError = new AdobeError[1];
//		final CountDownLatch latch = new CountDownLatch(1);
//		final boolean[] wasCalled = new boolean[1];
//		AdobeCallbackWithError<String> callback = new AdobeCallbackWithError<String>() {
//			@Override
//			public void call(final String data) {
//				wasCalled[0] = true;
//				Assert.fail("Success callback was invoked unexpectedly");
//			}
//
//			@Override
//			public void fail(final AdobeError adobeError) {
//				storedAdobeError[0] = adobeError;
//				latch.countDown();
//			}
//		};
//
//
//		// test
//		startCore();
//		Analytics.getVisitorIdentifier(callback);
//		int waitTimeout = WAIT_TIMEOUT_MILLIS;
//		boolean await = latch.await(waitTimeout, TimeUnit.MILLISECONDS);
//
//		// verify
//		assertTrue(String.format("Timed out waiting for getVisitorIdentifier callback after (%d) ms", waitTimeout), await);
//		assertFalse(wasCalled[0]);
//		assertEquals(AdobeError.EXTENSION_NOT_INITIALIZED, storedAdobeError[0]);
//	}
//
//	@Test
//	public void test_getQueueSize_whenAnalyticsExtensionRegistered_invalidConfig_returnsCallbackWithoutError() throws
//		InterruptedException {
//		final AdobeError[] storedAdobeError = new AdobeError[1];
//		final CountDownLatch latch = new CountDownLatch(1);
//		final boolean[] wasCalled = new boolean[1];
//		AdobeCallbackWithError<Long> callback = new AdobeCallbackWithError<Long>() {
//			@Override
//			public void call(final Long data) {
//				wasCalled[0] = true;
//				latch.countDown();
//			}
//
//			@Override
//			public void fail(final AdobeError adobeError) {
//				storedAdobeError[0] = adobeError;
//				Assert.fail("Error callback was invoked unexpectedly");
//			}
//		};
//
//		// test
//		registerAnalyticsAndStartCore();
//		Analytics.getQueueSize(callback);
//		int waitTimeout = CALLBACK_TIMEOUT_MILLIS + WAIT_TIMEOUT_MILLIS;
//		boolean await = latch.await(waitTimeout, TimeUnit.MILLISECONDS);
//
//		// verify
//		assertTrue(String.format("Timed out waiting for getQueueSize callback after (%d) ms", waitTimeout), await);
//		assertTrue(wasCalled[0]);
//		assertNull(storedAdobeError[0]);
//	}
//
//	@Test
//	public void test_getTrackingIdentifier_whenAnalyticsExtensionRegistered_invalidConfig_returnsCallbackWithTimeoutError()
//	throws
//		InterruptedException {
//		final AdobeError[] storedAdobeError = new AdobeError[1];
//		final CountDownLatch latch = new CountDownLatch(1);
//		final boolean[] wasCalled = new boolean[1];
//		AdobeCallbackWithError<String> callback = new AdobeCallbackWithError<String>() {
//			@Override
//			public void call(final String data) {
//				wasCalled[0] = true;
//				Assert.fail("Success callback was invoked unexpectedly");
//			}
//
//			@Override
//			public void fail(final AdobeError adobeError) {
//				storedAdobeError[0] = adobeError;
//				latch.countDown();
//			}
//		};
//
//		// test
//		registerAnalyticsAndStartCore();
//		Analytics.getTrackingIdentifier(callback);
//		int waitTimeout = CALLBACK_TIMEOUT_MILLIS + WAIT_TIMEOUT_MILLIS;
//		boolean await = latch.await(waitTimeout, TimeUnit.MILLISECONDS);
//
//		// verify
//		assertTrue(String.format("Timed out waiting for getTrackingIdentifier callback after (%d) ms", waitTimeout), await);
//		assertFalse(wasCalled[0]);
//		assertEquals(AdobeError.CALLBACK_TIMEOUT, storedAdobeError[0]);
//	}
//
//	@Test
//	public void test_getVisitorIdentifier_whenAnalyticsExtensionRegistered_invalidConfig_returnsCallbackWithTimeoutError()
//	throws
//		InterruptedException {
//		final AdobeError[] storedAdobeError = new AdobeError[1];
//		final CountDownLatch latch = new CountDownLatch(1);
//		final boolean[] wasCalled = new boolean[1];
//		AdobeCallbackWithError<String> callback = new AdobeCallbackWithError<String>() {
//			@Override
//			public void call(final String data) {
//				wasCalled[0] = true;
//				Assert.fail("Success callback was invoked unexpectedly");
//			}
//
//			@Override
//			public void fail(final AdobeError adobeError) {
//				storedAdobeError[0] = adobeError;
//				latch.countDown();
//			}
//		};
//
//		// test
//		registerAnalyticsAndStartCore();
//		Analytics.getVisitorIdentifier(callback);
//		int waitTimeout = CALLBACK_TIMEOUT_MILLIS + WAIT_TIMEOUT_MILLIS;
//		boolean await = latch.await(waitTimeout, TimeUnit.MILLISECONDS);
//
//		// verify
//		assertTrue(String.format("Timed out waiting for getVisitorIdentifier callback after (%d) ms", waitTimeout), await);
//		assertFalse(wasCalled[0]);
//		assertEquals(AdobeError.CALLBACK_TIMEOUT, storedAdobeError[0]);
//	}
//
//	private void registerAnalyticsAndStartCore() {
//		try {
//			Analytics.registerExtension();
//		} catch (InvalidInitException e) {
//			Assert.fail("Analytics extension initialization failed");
//			e.printStackTrace();
//		}
//
//		MobileCore.setLogLevel(LoggingMode.VERBOSE);
//		MobileCore.start(null);
//		asyncHelper.waitForAppThreads(1000, true);
//	}
//
//	private void startCore() {
//		// skip registration part
//		MobileCore.setLogLevel(LoggingMode.VERBOSE);
//		MobileCore.start(null);
//	}
//
//	private void resetCore() {
//		MobileCore.setCore(null);
//		MobileCore.setApplication(this.defaultApplication);
//		Analytics.resetAnalyticsCore();
//	}
//}
