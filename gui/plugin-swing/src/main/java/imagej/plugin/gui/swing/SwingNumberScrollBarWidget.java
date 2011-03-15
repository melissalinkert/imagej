//
// SwingNumberScrollBarWidget.java
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

package imagej.plugin.gui.swing;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JScrollBar;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Swing implementation of number chooser widget, using a scroll bar.
 *
 * @author Curtis Rueden
 */
public class SwingNumberScrollBarWidget extends SwingNumberWidget
	implements AdjustmentListener, DocumentListener
{

	private JScrollBar scrollBar;
	private JTextField textField;

	public SwingNumberScrollBarWidget(final Number initialValue,
		final Number min, final Number max, final Number stepSize)
	{
		scrollBar = new JScrollBar(Adjustable.HORIZONTAL,
			initialValue.intValue(), 1, min.intValue(), max.intValue() + 1);
		scrollBar.setUnitIncrement(stepSize.intValue());
		scrollBar.addAdjustmentListener(this);
		add(scrollBar, BorderLayout.CENTER);

		textField = new JTextField(initialValue.toString(), 6);
		textField.getDocument().addDocumentListener(this);
		add(textField, BorderLayout.EAST);
	}

	@Override
	public Number getValue() {
		return scrollBar.getValue();
	}

	@Override
	public void adjustmentValueChanged(final AdjustmentEvent e) {
		scrollBar.removeAdjustmentListener(this);
		textField.setText("" + scrollBar.getValue());
		scrollBar.addAdjustmentListener(this);
	}

	@Override
	public void changedUpdate(final DocumentEvent e) {
		documentUpdate();
	}

	@Override
	public void insertUpdate(final DocumentEvent e) {
		documentUpdate();
	}

	@Override
	public void removeUpdate(final DocumentEvent e) {
		documentUpdate();
	}

	private void documentUpdate() {
		try {
			scrollBar.setValue(Integer.parseInt(textField.getText()));
		}
		catch (NumberFormatException e) {
			// invalid number in text field; do not update scroll bar
		}
	}

}