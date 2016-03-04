/*
 * Copyright 2015-2016 DevCon5 GmbH, info@devcon5.ch
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

package io.devcon5.pageobjects.tx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used declare a page or operation on a page transactional. If a page is annotated with this annotation,
 * the loading of the page is considered a transaction (using the value as transaction name). If a method
 * is annotated with this annotation, the execution of the method is considered a transaction.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Transaction {

    /**
     * The name of the transaction. If the value is ommitted, the transaction will be derived from
     * the classname (for {@link io.devcon5.pageobjects.Page}s or from the method name and the name of the
     * class or the transaction of the class.
     * @return
     *  the name of the transaction
     */
    String value() default "";
}
