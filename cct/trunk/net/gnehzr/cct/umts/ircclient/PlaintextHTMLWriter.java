package net.gnehzr.cct.umts.ircclient;

import java.io.IOException;
import java.io.StringWriter;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLWriter;

public class PlaintextHTMLWriter extends HTMLWriter {
	public PlaintextHTMLWriter(StringWriter w, HTMLDocument doc, int start, int length) {
		super(w, doc, start, length);
	}

	protected void startTag(Element elem) throws IOException, BadLocationException {}

	protected void endTag(Element elem) throws IOException {}
	
	protected void indent() throws IOException {}
	
	protected void emptyTag(Element elem) throws BadLocationException, IOException {
		if(elem.getName().equals(HTML.Tag.BR.toString())) {
			isText = true;
			super.write(getLineSeparator());
			isText = false;
		} else
			super.emptyTag(elem);
	}
	
	private boolean isText = false;

	protected void write(char[] chars, int startIndex, int length) throws IOException {
		if(isText)
			super.write(chars, startIndex, length);
	}

	protected void text(Element elem) throws BadLocationException, IOException {
		isText = true;
		super.text(elem);
		isText = false;
	}
}
