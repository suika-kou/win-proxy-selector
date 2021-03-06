package jp.co.fusions.win_proxy_selector.selector.pac;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

import org.junit.Test;

import jp.co.fusions.win_proxy_selector.TestUtil;

/*****************************************************************************
 * Tests for the Pac script parser and proxy selector.
 * 
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class PacProxySelectorTest {

	/*************************************************************************
	 * Test method
	 * 
	 * @
	 *             on proxy detection error.
	 * @throws MalformedURLException
	 *             on URL erros
	 ************************************************************************/
	@Test
	public void testScriptExecution() throws MalformedURLException {
		List<Proxy> result = new PacProxySelector(new UrlPacScriptSource(toUrl("test1.pac")))
		        .select(TestUtil.HTTP_TEST_URI);

		assertEquals(TestUtil.HTTP_TEST_PROXY, result.get(0));
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @
	 *             on proxy detection error.
	 * @throws MalformedURLException
	 *             on URL erros
	 ************************************************************************/
	@Test
	public void testScriptExecution2() throws MalformedURLException {
		PacProxySelector pacProxySelector = new PacProxySelector(new UrlPacScriptSource(toUrl("test2.pac")));
		List<Proxy> result = pacProxySelector.select(TestUtil.HTTP_TEST_URI);
		assertEquals(Proxy.NO_PROXY, result.get(0));

		result = pacProxySelector.select(TestUtil.HTTPS_TEST_URI);
		assertEquals(Proxy.NO_PROXY, result.get(0));
	}

	/*************************************************************************
	 * Test download fix to prevent infinite loop.
	 * 
	 * @
	 *             on proxy detection error.
	 * @throws MalformedURLException
	 *             on URL erros
	 ************************************************************************/
	@Test
	public void pacDownloadFromURLShouldNotUseProxy() throws MalformedURLException {
		ProxySelector oldOne = ProxySelector.getDefault();
		try {
			ProxySelector.setDefault(new ProxySelector() {
				@Override
				public List<Proxy> select(URI uri) {
					throw new IllegalStateException("Should not download via proxy");
				}

				@Override
				public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
					// Not used
				}
			});

			PacProxySelector pacProxySelector = new PacProxySelector(
			        new UrlPacScriptSource("http://www.test.invalid/wpad.pac"));
			pacProxySelector.select(TestUtil.HTTPS_TEST_URI);
		} finally {
			ProxySelector.setDefault(oldOne);
		}
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @
	 *             on proxy detection error.
	 * @throws MalformedURLException
	 *             on URL erros
	 ************************************************************************/
	@Test
	public void testScriptMultiProxy() throws MalformedURLException {
		PacProxySelector pacProxySelector = new PacProxySelector(new UrlPacScriptSource(toUrl("testMultiProxy.pac")));
		List<Proxy> result = pacProxySelector.select(TestUtil.HTTP_TEST_URI);
		assertEquals(4, result.size());
        assertEquals(new Proxy(Type.HTTP, new InetSocketAddress("my-proxy.com", 80)), result.get(0));
        assertEquals(new Proxy(Type.HTTP, new InetSocketAddress("my-proxy2.com", 8080)), result.get(1));
        assertEquals(new Proxy(Type.HTTP, new InetSocketAddress("my-proxy3.com", 8080)), result.get(2));
        assertEquals(new Proxy(Type.HTTP, new InetSocketAddress("my-proxy4.com", 80)), result.get(3));
	}

	/*************************************************************************
	 * Test method for building a proxy from PAC result
	 *
	 * @
	 *
	 ************************************************************************/
	@Test
	public void testBuildProxyFromPacResult() throws Exception{
		Proxy proxy;
		proxy = PacProxySelector.buildProxyFromPacResult("PROXY my-proxy.com : 8080");
		assertEquals(Type.HTTP, proxy.type());
		assertEquals("my-proxy.com", ((InetSocketAddress)proxy.address()).getHostName());
		assertEquals(8080, ((InetSocketAddress)proxy.address()).getPort());
//		assertEquals("my-proxy.com:8080", proxy.address().toString());

		proxy = PacProxySelector.buildProxyFromPacResult("HTTP my-proxy-xvsds.com : 8080");
		assertEquals(Type.HTTP, proxy.type());
		assertEquals("my-proxy-xvsds.com", ((InetSocketAddress)proxy.address()).getHostName());
		assertEquals(8080, ((InetSocketAddress)proxy.address()).getPort());
//		assertEquals("my-proxy.com:8080", proxy.address().toString());

		proxy = PacProxySelector.buildProxyFromPacResult("SOCKS4 123.22.84.56:3128");
		assertEquals(Type.SOCKS, proxy.type());
		assertEquals("123.22.84.56", ((InetSocketAddress)proxy.address()).getHostName());
		assertEquals(3128, ((InetSocketAddress)proxy.address()).getPort());

		proxy = PacProxySelector.buildProxyFromPacResult("HTTPS 123.22.84.56");
		assertEquals(Type.HTTP, proxy.type());
		assertEquals("123.22.84.56", ((InetSocketAddress)proxy.address()).getHostName());
		assertEquals(80, ((InetSocketAddress)proxy.address()).getPort());


		proxy = PacProxySelector.buildProxyFromPacResult("HTTP [2001:db8:85a3:8d3:1319:8a2e:370:7348]:3128");
		assertEquals(Type.HTTP, proxy.type());
		System.out.println("HOST: " + ((InetSocketAddress)proxy.address()).getHostName());
		System.out.println("ADDR: " + ((InetSocketAddress)proxy.address()).getAddress().getHostAddress());
		assertEquals("2001:db8:85a3:8d3:1319:8a2e:370:7348", ((InetSocketAddress)proxy.address()).getHostName());
		assertEquals(3128, ((InetSocketAddress)proxy.address()).getPort());

		proxy = PacProxySelector.buildProxyFromPacResult("HTTP [2001:db8:85a3:8d3:1319:8a2e:370:7348]");
		assertEquals(Type.HTTP, proxy.type());
		System.out.println("HOST: " + ((InetSocketAddress)proxy.address()).getHostName());
		System.out.println("ADDR: " + ((InetSocketAddress)proxy.address()).getAddress().getHostAddress());
		assertEquals("2001:db8:85a3:8d3:1319:8a2e:370:7348", ((InetSocketAddress)proxy.address()).getHostName());
		assertEquals(80, ((InetSocketAddress)proxy.address()).getPort());
	}

	/*************************************************************************
	 * Test method for the override local IP feature.
	 * 
	 * @
	 *             on proxy detection error.
	 * @throws MalformedURLException
	 *             on URL erros
	 ************************************************************************/
	@Test
	public void testLocalIPOverride() throws MalformedURLException {
		System.setProperty(PacScriptMethods.OVERRIDE_LOCAL_IP, "123.123.123.123");
		try {
			PacProxySelector pacProxySelector = new PacProxySelector(new UrlPacScriptSource(toUrl("testLocalIP.pac")));
			List<Proxy> result = pacProxySelector.select(TestUtil.HTTP_TEST_URI);
			assertEquals(result.get(0),
			        new Proxy(Type.HTTP, new InetSocketAddress("123.123.123.123", 8080)));
		} finally {
			System.setProperty(PacScriptMethods.OVERRIDE_LOCAL_IP, "");
		}

	}

	/*************************************************************************
	 * Helper method to build the url to the given test file
	 * 
	 * @param testFile
	 *            the name of the test file.
	 * @return the URL.
	 * @throws MalformedURLException
	 ************************************************************************/

	private String toUrl(String testFile) throws MalformedURLException {
		return new File(TestUtil.TEST_DATA_FOLDER + "pac", testFile).toURI().toURL().toString();
	}

}
