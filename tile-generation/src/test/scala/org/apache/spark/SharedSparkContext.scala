/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.sql.SQLContext

import org.scalatest.Suite
import org.scalatest.BeforeAndAfterAll

/** Shares a local `SparkContext` between all tests in a suite and closes it at the end */
trait SharedSparkContext extends BeforeAndAfterAll { self: Suite =>

	@transient private var _sc: SparkContext = _
	@transient private var _sqlc: SQLContext = _

	def sc: SparkContext = _sc
	def sqlc: SQLContext = _sqlc

	var conf = new SparkConf(false)

	override def beforeAll() {
		// Make sure to allow multiple contexts for testing
		conf.set("spark.driver.allowMultipleContexts", "true")

		_sc = new SparkContext("local", "test", conf)
		_sqlc = new SQLContext(_sc)
		super.beforeAll()
	}

	override def afterAll() {
		LocalSparkContext.stop(_sc)
		_sqlc = null
		_sc = null
		super.afterAll()
	}
}
