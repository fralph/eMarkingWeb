package cl.uai.client.buttons;

import cl.uai.client.EMarkingWeb;

import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.event.dom.client.ClickEvent;

public class ShowHelpButton extends BubbleButton {

	public ShowHelpButton(int _left, int _top) {
		super(IconType.H_SIGN, _left, _top);
	}
	
	@Override
	public void onButtonClick(ClickEvent event) {
		EMarkingWeb.markingInterface.help.show();
	}
}
