/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ratpackframework.groovy.test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Cookie;
import com.jayway.restassured.response.Cookies;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import org.ratpackframework.test.ApplicationUnderTest;
import org.ratpackframework.util.Action;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class RequestingSupport {

  private final ApplicationUnderTest applicationUnderTest;
  private final Action<RequestSpecification> requestConfigurer;

  public RequestingSupport(ApplicationUnderTest applicationUnderTest, Action<RequestSpecification> requestConfigurer) {
    this.applicationUnderTest = applicationUnderTest;
    this.requestConfigurer = requestConfigurer;
  }

  private RequestSpecification request;
  private Response response;
  private List<Cookie> cookies = new LinkedList<>();

  public RequestSpecification getRequest() {
    return request;
  }

  public Response getResponse() {
    return response;
  }

  public RequestSpecification resetRequest() {
    request = createRequest();
    return request;
  }

  public Response head() {
    return head("");
  }

  public Response head(String path) {
    response = request.head(toAbsolute(path));
    return postRequest();
  }

  public Response get() {
    return get("");
  }

  public Response get(String path) {
    preRequest();
    response = request.get(toAbsolute(path));
    return postRequest();
  }

  public String getText() {
    return getText("");
  }

  public String getText(String path) {
    get(path);
    return response.asString();
  }

  public Response post() {
    return post("");
  }

  public Response post(String path) {
    preRequest();
    response = request.post(toAbsolute(path));
    return postRequest();
  }

  public String postText() {
    return postText("");
  }

  public String postText(String path) {
    post(path);
    return response.asString();
  }

  public Response put() {
    return put("");
  }

  public Response put(String path) {
    preRequest();
    response = request.put(toAbsolute(path));
    return postRequest();
  }

  public String putText() {
    return putText("");
  }

  public String putText(String path) {
    return put(path).asString();
  }

  public Response delete() {
    return delete("");
  }

  public Response delete(String path) {
    preRequest();
    response = request.delete(toAbsolute(path));
    return postRequest();
  }

  public String deleteText() {
    return deleteText("");
  }

  public String deleteText(String path) {
    return delete(path).asString();
  }

  public RequestSpecification createRequest() {
    RequestSpecification request = RestAssured.with().urlEncodingEnabled(false);
    requestConfigurer.execute(request);
    return request;
  }

  private void preRequest() {
    if (request == null) {
      request = createRequest();
    }
    try {
      Field field = request.getClass().getDeclaredField("cookies");
      field.setAccessible(true);
      field.set(request, new Cookies(cookies));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Response postRequest() {
    for (Cookie setCookie : response.getDetailedCookies()) {
      Date date = setCookie.getExpiryDate();
      for (Cookie priorCookie : new LinkedList<>(cookies)) {
        if (priorCookie.getName().equals(setCookie.getName())) {
          cookies.remove(priorCookie);
        }
      }
      if (date == null || date.compareTo(new Date()) > 0) {
        cookies.add(setCookie);
      }
    }

    return response;
  }

  private String toAbsolute(String path) {
    return applicationUnderTest.getAddress() + "/" + path;
  }

}