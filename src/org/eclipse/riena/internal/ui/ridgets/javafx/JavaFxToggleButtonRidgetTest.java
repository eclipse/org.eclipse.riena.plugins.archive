package org.eclipse.riena.internal.ui.ridgets.javafx;

import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;

import org.eclipse.riena.ui.ridgets.IRidget;
import org.eclipse.riena.ui.ridgets.IToggleButtonRidget;
import org.eclipse.riena.ui.ridgets.javafx.uibinding.JavaFxControlRidgetMapper;
import org.eclipse.riena.ui.ridgets.swt.uibinding.SwtControlRidgetMapper;
import org.eclipse.riena.internal.ui.ridgets.javafx.*;
import org.eclipse.riena.internal.ui.ridgets.swt.ToggleButtonRidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

public class JavaFxToggleButtonRidgetTest {
	
	
	private final static String ICON_ECLIPSE = "eclipse.gif"; //$NON-NLS-1$
	private final static String LABEL = "testlabel"; //$NON-NLS-1$
	private final static String LABEL2 = "testlabel2"; //$NON-NLS-1$
	// private IRidget ridget;
	private IToggleButtonRidget ridget;
	private Object control;
	

	// create ridget
	protected IToggleButtonRidget createRidget() {
		return new JavaFxToggleButtonRidget();
	}
	
	// create control
	protected void createControl(final Pane parent) {
		parent.getChildren().add(new ToggleButton());
	}

	// get ridget
	protected IToggleButtonRidget getRidget() {
	    // ridget = createRidget();
	    return this.ridget;
	}

	// get control
	protected ToggleButton getControl() {
		return (ToggleButton) this.control;
	 }
	
	
//	public void testRidgetMapping() {
//		final Pane pane = new Pane();
//
//		final JavaFxControlRidgetMapper mapper = JavaFxControlRidgetMapper.getInstance();
//		
//		createControl(pane);
//		
//		final ToggleButton toggleButton =  (ToggleButton) pane.getChildren().get(0);
//		
//		assertSame(JavaFxToggleButtonRidget.class, mapper.getRidgetClass(toggleButton));
//		
//
//		
// 		final Button buttonToggle = new Button(shell, SWT.TOGGLE);
//		assertSame(ToggleButtonRidget.class, mapper.getRidgetClass(buttonToggle));
//
//		final Button buttonCheck = new Button(shell, SWT.CHECK);
//		assertSame(ToggleButtonRidget.class, mapper.getRidgetClass(buttonCheck));
//
//		final Button buttonPush = new Button(shell, SWT.PUSH);
//		assertNotSame(ToggleButtonRidget.class, mapper.getRidgetClass(buttonPush));
//
//		final Button aButton = new Button(shell, SWT.NONE);
//		assertNotSame(ToggleButtonRidget.class, mapper.getRidgetClass(aButton));
//		
//		
//	}
	
	
	

}
