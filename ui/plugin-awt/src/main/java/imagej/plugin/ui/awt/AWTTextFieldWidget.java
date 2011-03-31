//
// AWTTextFieldWidget.java
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

package imagej.plugin.ui.awt;

import imagej.plugin.ui.ParamDetails;
import imagej.plugin.ui.TextFieldWidget;

import java.awt.BorderLayout;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

/**
 * AWT implementation of text field widget.
 *
 * @author Curtis Rueden
 */
public class AWTTextFieldWidget extends Panel implements TextFieldWidget,
	TextListener
{

	private ParamDetails details;
	private TextField textField;

	public AWTTextFieldWidget(final ParamDetails details, final int columns) {
		this.details = details;

		textField = new TextField("", columns);
		textField.addTextListener(this);
		add(textField, BorderLayout.CENTER);

		refresh();
	}

	// -- TextFieldWidget methods --

	@Override
	public String getText() {
		return textField.getText();
	}

	// -- InputWidget methods --

	@Override
	public void refresh() {
		textField.setText(details.getValue().toString());
	}

	// -- TextListener methods --

	@Override
	public void textValueChanged(final TextEvent e) {
		details.setValue(textField.getText());
	}

}