

/******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.dawb.common.ui.svg;


import java.awt.Dimension;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.ext.awt.image.codec.imageio.ImageIOJPEGImageWriter;
import org.apache.batik.ext.awt.image.codec.imageio.ImageIOJPEGRegistryEntry;
import org.apache.batik.ext.awt.image.codec.imageio.ImageIOPNGImageWriter;
import org.apache.batik.ext.awt.image.codec.imageio.ImageIOPNGRegistryEntry;
import org.apache.batik.ext.awt.image.codec.imageio.ImageIOTIFFImageWriter;
import org.apache.batik.ext.awt.image.codec.imageio.ImageIOTIFFRegistryEntry;
import org.apache.batik.ext.awt.image.spi.ImageTagRegistry;
import org.apache.batik.ext.awt.image.spi.ImageWriterRegistry;
import org.apache.batik.svggen.SVGGraphics2D;
import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Objects of this class can be used with draw2d to create an SVG DOM.
 * 
 * @author jschofie / sshaw
 */
public class GraphicsSVG extends GraphicsToGraphics2DAdaptor {

	private Document doc;

	static {
		// workaround bad packaging of version 1.9.1.v20190730-1743 of
		// org.apache.batik.ext.awt and org.apache.batik.codec
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=522740#c46
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=177986#c9
		ImageWriterRegistry.getInstance().register(new ImageIOPNGImageWriter());
		ImageWriterRegistry.getInstance().register(new ImageIOTIFFImageWriter());
		ImageWriterRegistry.getInstance().register(new ImageIOJPEGImageWriter());

		ImageTagRegistry.getRegistry().register(new ImageIOJPEGRegistryEntry());
		ImageTagRegistry.getRegistry().register(new ImageIOPNGRegistryEntry());
		ImageTagRegistry.getRegistry().register(new ImageIOTIFFRegistryEntry());
	}

	/**
	 * Static initializer that will return an instance of <code>GraphicsSVG</code>
	 * 
	 * @param viewPort the <code>Rectangle</code> area that is to be rendered.
	 * @return a new <code>GraphicsSVG</code> object.
	 */
	public static GraphicsSVG getInstance(Rectangle viewPort) {
		SVGGraphics2D svgGraphics;
		
		// Get the DOM implementation and create the document
		DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
		String svgNameSpace = SVGDOMImplementation.SVG_NAMESPACE_URI;
		Document doc = impl.createDocument(svgNameSpace, "svg", null); //$NON-NLS-1$

		// Create the SVG Graphics Object
		svgGraphics = new SVGGraphics2D(doc);

		// Set the precision level to avoid NPEs (issue with Batik 1.5)
		svgGraphics.getGeneratorContext().setPrecision( 3 );

		// Set the Width and Height Attributes on the Root Element
		svgGraphics.setSVGCanvasSize(
			new Dimension(viewPort.width, viewPort.height));
		
		return new GraphicsSVG(svgGraphics, doc, svgNameSpace, viewPort);
	}
	
	/**
	 * @return <code>SVGGraphics2D</code> object
	 */
	public SVGGraphics2D getSVGGraphics2D() {
		return (SVGGraphics2D)getGraphics2D();
	}
	
	/**
	 * @param graphics
	 * @param doc
	 * @param svgNameSpace
	 * @param viewPort
	 */
	private GraphicsSVG( SVGGraphics2D graphics, Document doc, String svgNameSpace, Rectangle viewPort ) {
		
		this( graphics, doc, svgNameSpace, new org.eclipse.swt.graphics.Rectangle( viewPort.x,
						viewPort.y,
						viewPort.width,
						viewPort.height) );
	}

	/**
	 * @param graphics
	 * @param doc
	 * @param svgNameSpace
	 * @param viewPort
	 */
	private GraphicsSVG(SVGGraphics2D graphics, Document doc, String svgNameSpace, org.eclipse.swt.graphics.Rectangle viewPort) {

		super(graphics, viewPort );
		this.doc = doc;
		paintNotCompatibleStringsAsBitmaps = false;
	}

	/**
	 * Method used to get the SVG DOM from the Graphics
	 * 
	 * @return SVG document
	 */
	public Document getDocument() {
		return doc;
	}

	/**
	 * Method used to get the SVG Root element from the document
	 * 
	 * @return DOM Root element
	 */
	public Element getRoot() {
		return getSVGGraphics2D().getRoot();		
	}
	

}
