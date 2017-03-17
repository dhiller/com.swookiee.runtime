package com.swookiee.runtime.core.internal.shutdown;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.service.component.ComponentContext;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ShutdownExecutorTest {

    @Mock
    private ComponentContext componentContext;

    @Mock
    private org.osgi.framework.BundleContext bundleContext;

    @Mock
    private org.osgi.framework.Bundle systemBundle;

    @Mock
    private org.osgi.framework.Bundle bundle1;

    @Mock
    private org.osgi.framework.Bundle bundle2;

    @Mock
    private Framework framework;

    private ShutdownExecutor underTest;

    @Before
    public void setUpMocks() {
        when(componentContext.getBundleContext()).thenReturn(bundleContext);
        when(bundleContext.getBundle(0)).thenReturn(systemBundle);
        when(systemBundle.adapt(eq(Framework.class))).thenReturn(framework);

        when(bundleContext.getBundles()).thenReturn(new Bundle[]{systemBundle, bundle1, bundle2});
        when(systemBundle.getBundleId()).thenReturn(0L);
        when(bundle1.getBundleId()).thenReturn(1L);
        when(bundle1.getState()).thenReturn(Bundle.RESOLVED);
        when(bundle2.getBundleId()).thenReturn(2L);
        when(bundle2.getState()).thenReturn(Bundle.RESOLVED);

        underTest = new ShutdownExecutor(componentContext.getBundleContext());
    }

    @Test
    public void hookStopsSystemBundle() throws BundleException {
        underTest.run();

        verify(framework).stop();
    }

    @Test
    public void hookFiltersBundlesByBundleId() throws BundleException {
        underTest.run();

        verify(bundle1).getBundleId();
        verify(bundle1).getState();
    }

    @Test
    public void hookFilterSkipsSystemBundle() throws BundleException {
        underTest.run();

        verify(systemBundle).getBundleId();
        verify(systemBundle, never()).getState();
    }

    @Test
    public void doesntRunTwice() throws BundleException {
        underTest.run();
        underTest.run();

        verify(systemBundle).getBundleId();
        verify(systemBundle, never()).getState();
    }

}
