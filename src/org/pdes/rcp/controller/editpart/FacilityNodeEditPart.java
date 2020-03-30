package org.pdes.rcp.controller.editpart;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;
import org.pdes.rcp.controller.editpart.base.NodeElementEditPart;
import org.pdes.rcp.model.FacilityNode;
import org.pdes.rcp.view.figure.FigureConstants;
import org.pdes.rcp.view.figure.NodeFigure;

public class FacilityNodeEditPart extends NodeElementEditPart{
	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		NodeFigure figure = new NodeFigure(FigureConstants.FACILITY_IMAGE_PATH);
		FacilityNode facility = (FacilityNode) getModel();
		figure.setName(facility.getName());
		facility.setFigure(figure);
		return figure;
	}

	/* (non-Javadoc)
	 * @see org.pdes.rcp.controller.editpart.base.NodeElementEditPart#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("name")) refreshNameLabel();
		super.propertyChange(evt);
	}

	/* (non-Javadoc)
	 * @see org.pdes.rcp.controller.editpart.base.NodeElementEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
	}
	
	/**
	 * Refresh the name label of FacilityNode.
	 */
	private void refreshNameLabel(){
		NodeFigure figure = (NodeFigure) getFigure();
		FacilityNode facility = (FacilityNode) getModel();
		figure.setName(facility.getName());
	}
	
}
