/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http.impl.conn;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NoHttpResponseException;
import org.apache.http.ProtocolException;
import org.apache.http.conn.params.ConnConnectionPNames;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.io.HttpMessageParser;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.message.BasicLineParser;
import org.apache.http.mockup.SessionInputBufferMockup;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for <code>DefaultResponseParser</code>.
 */
public class TestDefaultResponseParser extends TestCase {

    public TestDefaultResponseParser(String testName) {
        super(testName);
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestDefaultResponseParser.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    public static Test suite() {
        return new TestSuite(TestDefaultResponseParser.class);
    }
    
    public void testResponseParsingWithSomeGarbage() throws Exception {
    	String s = 
    		"garbage\r\n" + 
    		"garbage\r\n" + 
    		"more garbage\r\n" + 
    		"HTTP/1.1 200 OK\r\n" + 
    		"header1: value1\r\n" + 
    		"header2: value2\r\n" + 
    		"\r\n"; 
    	
    	HttpParams params = new BasicHttpParams();
    	SessionInputBuffer inbuffer = new SessionInputBufferMockup(s, "US-ASCII", params); 
    	HttpMessageParser parser = new DefaultResponseParser(
    			inbuffer, 
    			BasicLineParser.DEFAULT,
    			new DefaultHttpResponseFactory(),
    			params);
    	
    	HttpResponse response = (HttpResponse) parser.parse();
    	assertNotNull(response);
    	assertEquals(HttpVersion.HTTP_1_1, response.getProtocolVersion());
    	assertEquals(200, response.getStatusLine().getStatusCode());
    	
    	Header[] headers = response.getAllHeaders();
    	assertNotNull(headers);
    	assertEquals(2, headers.length);
    	assertEquals("header1", headers[0].getName());
    	assertEquals("header2", headers[1].getName());
    }

    public void testResponseParsingWithTooMuchGarbage() throws Exception {
    	String s = 
    		"garbage\r\n" + 
    		"garbage\r\n" + 
    		"more garbage\r\n" + 
    		"HTTP/1.1 200 OK\r\n" + 
    		"header1: value1\r\n" + 
    		"header2: value2\r\n" + 
    		"\r\n"; 
    	
    	HttpParams params = new BasicHttpParams();
    	params.setParameter(ConnConnectionPNames.MAX_STATUS_LINE_GARBAGE, Integer.valueOf(2));
    	SessionInputBuffer inbuffer = new SessionInputBufferMockup(s, "US-ASCII", params); 
    	HttpMessageParser parser = new DefaultResponseParser(
    			inbuffer, 
    			BasicLineParser.DEFAULT,
    			new DefaultHttpResponseFactory(),
    			params);
    	
    	try {
        	parser.parse();
        	fail("ProtocolException should have been thrown");
    	} catch (ProtocolException ex) {
    	}
    }

    public void testResponseParsingNoResponse() throws Exception {
    	HttpParams params = new BasicHttpParams();
    	SessionInputBuffer inbuffer = new SessionInputBufferMockup("", "US-ASCII", params); 
    	HttpMessageParser parser = new DefaultResponseParser(
    			inbuffer, 
    			BasicLineParser.DEFAULT,
    			new DefaultHttpResponseFactory(),
    			params);
    	
    	try {
        	parser.parse();
        	fail("NoHttpResponseException should have been thrown");
    	} catch (NoHttpResponseException ex) {
    	}
    }
    
    public void testResponseParsingOnlyGarbage() throws Exception {
    	String s = 
    		"garbage\r\n" + 
    		"garbage\r\n" + 
    		"more garbage\r\n" + 
    		"a lot more garbage\r\n";
    	HttpParams params = new BasicHttpParams();
    	SessionInputBuffer inbuffer = new SessionInputBufferMockup(s, "US-ASCII", params); 
    	HttpMessageParser parser = new DefaultResponseParser(
    			inbuffer, 
    			BasicLineParser.DEFAULT,
    			new DefaultHttpResponseFactory(),
    			params);
    	
    	try {
        	parser.parse();
        	fail("ProtocolException should have been thrown");
    	} catch (ProtocolException ex) {
    	}
    }
    
}
