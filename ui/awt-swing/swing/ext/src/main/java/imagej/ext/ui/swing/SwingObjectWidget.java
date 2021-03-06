//
// SwingObjectWidget.java
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

package imagej.ext.ui.swing;

import imagej.ext.module.ui.ObjectWidget;
import imagej.ext.module.ui.WidgetModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

/**
 * Swing implementation of object selector widget.
 * 
 * @author Curtis Rueden
 */
public class SwingObjectWidget extends SwingInputWidget
	implements ActionListener, ObjectWidget
{

	private final JComboBox comboBox;

	public SwingObjectWidget(final WidgetModel model, final Object[] items) {
		super(model);

		comboBox = new JComboBox(items);
		setToolTip(comboBox);
		add(comboBox);
		comboBox.addActionListener(this);

		refreshWidget();
	}

	// -- ActionListener methods --

	@Override
	public void actionPerformed(final ActionEvent e) {
		updateModel();
	}

	// -- InputWidget methods --

	@Override
	public Object getValue() {
		return comboBox.getSelectedItem();
	}

	@Override
	public void refreshWidget() {
		comboBox.setSelectedItem(getValidValue());
	}

	// -- Helper methods --

	private Object getValidValue() {
		final int itemCount = comboBox.getItemCount();
		if (itemCount == 0) return null; // no valid values exist

		final Object value = getModel().getValue();
		for (int i = 0; i < itemCount; i++) {
			final Object item = comboBox.getItemAt(i);
			if (value == item) return value;
		}

		// value was invalid; reset to first choice on the list
		final Object validValue = comboBox.getItemAt(0);
		// CTR FIXME should not update model in getter!
		getModel().setValue(validValue);
		return validValue;
	}

}
