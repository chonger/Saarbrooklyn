package saarbrooklyn;
import java.awt.Color;

import processing.core.PConstants;



public class FadeNote {

	String msg;
	float alpha;
	Color back,stroke,text;
	boolean doFade;
	
	public FadeNote(String msg,Color back,Color stroke, Color text, boolean doFade) {
		this.msg = msg;
		this.doFade = doFade;
		alpha = 255;
		this.back = back;
		this.stroke = stroke;
		this.text = text;
		
	}
	
	public boolean draw(Saarbrooklyn sb) {
		if(doFade) {
			alpha -= 1;
		}
		sb.fill(back.getRed(),back.getGreen(),back.getBlue(),alpha);
		sb.stroke(stroke.getRed(),stroke.getGreen(),stroke.getBlue(),alpha);
		sb.strokeWeight(8);
		sb.textFont(sb.type20);
		sb.textAlign(PConstants.CENTER);
		sb.rect(sb.width/2-200, sb.height/2-100, 400, 200);
		sb.fill(text.getRed(),text.getGreen(),text.getBlue(),alpha);
		sb.text(msg,sb.width/2,sb.height/2);
		if(alpha == 0)
			return true;
		else
			return false;
	}
	
}
