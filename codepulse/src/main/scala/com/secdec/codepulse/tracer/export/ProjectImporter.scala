/*
 * Code Pulse: A real-time code coverage testing tool. For more information
 * see http://code-pulse.com
 *
 * Copyright (C) 2014 Applied Visions - http://securedecisions.avi.com
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

package com.secdec.codepulse.tracer.export

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.Properties
import java.util.zip.ZipFile

import com.secdec.codepulse.data.model.ProjectData
import com.secdec.codepulse.data.storage.{InputStore, StorageManager}
import net.liftweb.util.Helpers.AsDouble

/** The actual guts of the import process. Handles the actual reading. This is
  * split up for versioning reasons.
  *
  * @author robertf
  */
trait ProjectImportReader {
	def doImport(inputStore: InputStore, zip: ZipFile, destination: ProjectData)
}

/** Helpers for reading project export files.
  *
  * @author robertf
  */
trait ProjectImportHelpers {
	protected def read(zip: ZipFile, name: String)(f: InputStream => Unit) {
		Option(zip.getEntry(name)).map(zip.getInputStream(_)).foreach(f)
	}

	protected def read[T](zip: ZipFile, name: String, defVal: T)(f: InputStream => T): T = {
		Option(zip.getEntry(name)).map(zip.getInputStream(_)).map(f).getOrElse(defVal)
	}
}

class ProjectImportException(msg: String, cause: Exception = null) extends IOException(msg, cause)

/** Responsible for importing projects from exported .pulse files. Handles file
  * versioning by looking at the .version manifest file, and delegates to the
  * appropriate importer.
  *
  * Trace data is imported into the given `TraceData` object; in most cases,
  * this should probably be a blank, newly created trace.
  *
  * Uses `ZipFile` internally, so input is expected to be a file on disk.
  *
  * @author robertf
  */
object ProjectImporter {
	val Importers = Map[Double, ProjectImportReader](
		1d -> new ProjectImportReaderV1,
		2d -> new ProjectImportReaderV2,
		2.1d -> new ProjectImportReaderV2)

	def canImportFrom(file: File) = {
		val zip = new ZipFile(file)
		try {
			getImporter(zip).isDefined
		} finally zip.close
	}

	def getImporter(zip: ZipFile) = {
		Option(zip.getEntry(".manifest")) flatMap { entry =>
			val in = zip.getInputStream(entry)
			val props = new Properties
			try props.load(in) finally in.close

			AsDouble.unapply(props getProperty "version") flatMap Importers.get
		}
	}

	def importFrom(file: File, destination: ProjectData) {
		val zip = new ZipFile(file)

		try {
			val importer = getImporter(zip) getOrElse { throw new IOException(s"Cannot import ${file.getName}") }
			importer.doImport(StorageManager, zip, destination)
		} finally zip.close
	}
}