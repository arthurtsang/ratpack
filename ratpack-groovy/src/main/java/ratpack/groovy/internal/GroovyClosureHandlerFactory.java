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

package ratpack.groovy.internal;

import com.google.inject.Injector;
import com.google.inject.Module;
import groovy.lang.Closure;
import ratpack.func.Function;
import ratpack.groovy.server.internal.GroovyKitAppFactory;
import ratpack.guice.Guice;
import ratpack.guice.GuiceBackedHandlerFactory;
import ratpack.handling.Handler;
import ratpack.launch.HandlerFactory;
import ratpack.registry.Registry;
import ratpack.server.ServerConfig;

public class GroovyClosureHandlerFactory implements HandlerFactory {

  private final Closure<?> ratpackClosure;

  public GroovyClosureHandlerFactory(Closure<?> ratpackClosure) {
    this.ratpackClosure = ratpackClosure;
  }

  @Override
  public Handler create(Registry registry) throws Exception {
    FullRatpackDslBacking dslBacking = new FullRatpackDslBacking();
    ClosureUtil.configureDelegateFirst(dslBacking, ratpackClosure);
    RatpackDslClosures closures = dslBacking.getClosures();

    ServerConfig serverConfig = registry.get(ServerConfig.class);
    GuiceBackedHandlerFactory guiceHandlerFactory = new GroovyKitAppFactory(registry);
    Function<Module, Injector> moduleInjectorTransformer = Guice.newInjectorFactory(serverConfig);

    return new RatpackDslClosureToHandlerTransformer(serverConfig, guiceHandlerFactory, moduleInjectorTransformer).apply(closures);
  }

}