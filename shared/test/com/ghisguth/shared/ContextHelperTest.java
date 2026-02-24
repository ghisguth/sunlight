package com.ghisguth.shared;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.os.Build;
import org.junit.Test;
import org.mockito.Mockito;

public class ContextHelperTest {

    @Test
    public void testDeviceProtectedContext_Api24AndAbove_UsesCreate() {
        Context mockContext = Mockito.mock(Context.class);
        Context mockProtectedContext = Mockito.mock(Context.class);

        when(mockContext.createDeviceProtectedStorageContext()).thenReturn(mockProtectedContext);

        // Pass API 24 (Nougat) directly for testing
        Context result =
                ContextHelper.getDeviceProtectedContext(mockContext, Build.VERSION_CODES.N);

        // Verify that it attempted to upgrade the context
        verify(mockContext).createDeviceProtectedStorageContext();
        assertEquals(mockProtectedContext, result);
    }

    @Test
    public void testDeviceProtectedContext_BelowApi24_ReturnsOriginal() {
        Context mockContext = Mockito.mock(Context.class);

        // Pass API 23 (Marshmallow) directly for testing
        Context result =
                ContextHelper.getDeviceProtectedContext(mockContext, Build.VERSION_CODES.M);

        // Verify that on API < 24, it just returns the same standard Context
        Mockito.verifyNoInteractions(mockContext);
        assertEquals(mockContext, result);
    }
}
