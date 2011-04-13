package saarbrooklyn;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;

import rita.RiText;

//fuck this...maybe use later

public class Conversation {

	Saarbrooklyn sb;
	float x,y;
	float tWid,lWid,rWid;
	ArrayList<RiText[]> lines;
	Color lC,rC;
	
	public Conversation(Saarbrooklyn sb, float x, float y, float tWid, float lWid, float rWid, Color lC, Color rC) {
		this.sb = sb;
		this.x = x;
		this.y = y;
		this.lWid = lWid;
		this.rWid = rWid;
		this.lC = lC;
		this.rC = rC;
	}
	
	public void addLine(String s, boolean left) {
		RiText.setDefaultFont("lucida10");
		float mWid = lWid;
		if(left) {
			RiText.setDefaultColor(lC.getRed(), lC.getGreen(), lC.getBlue());
		} else {
			RiText.setDefaultColor(rC.getRed(), rC.getGreen(), rC.getBlue());
			mWid = rWid;
		}
		RiText[] rt = RiText.createLines(sb, s, new Rectangle(0,0,(int)mWid,1000));
		float totalH = 0;
		for(RiText r : rt) {
			totalH += r.getBoundingBox().getHeight();
			r.setAutoDraw(false);
		}
		
		for(RiText[] rr : lines) {
			for(RiText x : rr) {
				x.y -= totalH;
			}
		}
		lines.add(rt);
	}
	
	public void draw() {
		float sX = x;
		float sY = y;
		for(int i=lines.size() - 1;i>=0;--i) {
			
		}
	}
}
