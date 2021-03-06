/*
 * Copyright (c) 2014 Cristian Vrabie, Evelina Vrabie.
 *
 * This file is part of android-promise.
 * android-deferred-object is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License,or (at your option)
 * any later version.
 *
 * android-promise is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with android-promise
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.codeandmagic.promise.tests;

import org.codeandmagic.promise.*;
import org.codeandmagic.promise.Either.Left;
import org.codeandmagic.promise.Either.Right;
import org.codeandmagic.promise.impl.DeferredObject3;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by cristian on 10/02/2014.
 */
@RunWith(JUnit4.class)
public class TransformationTests {

    private Transformation<Integer, String> intToString = new Transformation<Integer, String>() {
        @Override
        public String transform(Integer success) {
            return (success * 2) + "!";
        }
    };

    private Transformation<String, Either<Throwable, Integer>> stringToInt = new Transformation<String, Either<Throwable,Integer>>() {
        @Override
        public Either<Throwable, Integer> transform(String value) {
            try {
                return new Right<Throwable, Integer>(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                return new Left<Throwable, Integer>(e);
            }
        }
    };

    @Test
    public void testMapSuccess() {
        Callback<String> onSuccess = mock(Callback.class);
        DeferredObject3<Integer, Void, Void> promise = new DeferredObject3<Integer, Void, Void>();
        Promise3<String, Void, Void> promise2 = promise.map(intToString).onSuccess(onSuccess);

        assertFalse(promise2.isSuccess());

        promise.success(1);

        assertTrue(promise2.isSuccess());
        verify(onSuccess, only()).onCallback("2!");
    }

    @Test
    public void testMapFailure() {
        DeferredObject3<Integer, Throwable, Void> promise = new DeferredObject3<Integer, Throwable, Void>();
        Promise3<String, Throwable, Void> promise2 = promise.map(intToString);

        Callback<String> onSuccess = mock(Callback.class);
        Callback<Throwable> onFailure = mock(Callback.class);
        Callback<Either<Throwable, String>> onComplete = mock(Callback.class);
        promise2.onSuccess(onSuccess).onFailure(onFailure).onComplete(onComplete);

        assertFalse(promise2.isSuccess());

        promise.failure(new Exception());

        assertFalse(promise2.isSuccess());
        verify(onSuccess, never()).onCallback(anyString());
        verify(onFailure, only()).onCallback(any(Throwable.class));
        verify(onComplete, only()).onCallback(any(Either.class));
        assertTrue(promise2.isFailure());
    }

    @Test
    public void testFlatMap() {
        DeferredObject3<String, Throwable, Void> promise = new DeferredObject3<String, Throwable, Void>();
        Promise3<Integer, Throwable, Void> promise2 = promise.flatMap(stringToInt);

        Callback<Integer> onSuccess = mock(Callback.class);
        Callback<Throwable> onFailure = mock(Callback.class);
        Callback<Either<Throwable, Integer>> onComplete = mock(Callback.class);
        promise2.onSuccess(onSuccess).onFailure(onFailure).onComplete(onComplete);

        assertFalse(promise2.isSuccess());

        // 'abc' is not convertible to Integer
        promise.success("abc");

        assertFalse(promise2.isSuccess());
        verify(onSuccess, never()).onCallback(anyInt());
        verify(onFailure, only()).onCallback(any(Throwable.class));
        verify(onComplete, only()).onCallback(any(Either.class));
        assertTrue(promise2.isFailure());
    }
}
