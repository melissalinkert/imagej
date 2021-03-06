//
// UploadableFile.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.updater.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * A class used for uploading local files.
 * 
 * @author Johannes Schindelin
 * @author Yap Chin Kiet
 */
public class UploadableFile implements Uploadable {

	protected FileObject file;
	protected String permissions, filename;
	protected File source;
	protected long filesize;

	public UploadableFile(final FilesCollection files, final FileObject file) {
		this(files.prefix(file), file.getFilename() + "-" + file.getTimestamp());
		this.file = file;
	}

	public UploadableFile(final File source, final String target) {
		this(source, target, "C0644");
	}

	public UploadableFile(final File source, final String target,
		final String permissions)
	{
		this.source = source;
		this.filename = target;
		this.permissions = permissions;
		filesize = source.exists() ? source.length() : 0;
	}

	protected void updateFilesize() {
		filesize = source.length();
	}

	// ********** Implemented methods for SourceFile **********
	@Override
	public long getFilesize() {
		return filesize;
	}

	@Override
	public String getFilename() {
		return filename;
	}

	@Override
	public String getPermissions() {
		return permissions;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		try {
			return new FileInputStream(source);
		}
		catch (final FileNotFoundException e) {
			return new ByteArrayInputStream(new byte[0]);
		}
	}

	@Override
	public String toString() {
		return filename;
	}
}
